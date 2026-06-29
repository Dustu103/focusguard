package com.focusguard.services

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.focusguard.data.AppDatabase
import com.focusguard.utils.BlockedDomains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class FocusVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private lateinit var db: AppDatabase
    private var blockedDomains = setOf<String>()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(this)
        loadBlockedDomains()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            stopSelf()
            return START_NOT_STICKY
        }
        
        loadBlockedDomains()
        
        if (!isRunning && blockedDomains.isNotEmpty()) {
            startVpn()
        }
        return START_STICKY
    }

    private fun loadBlockedDomains() {
        serviceScope.launch {
            combine(
                db.blockedAppDao().getBlockedApps(),
                db.blockedDomainDao().getBlockedDomains()
            ) { apps, customDomains ->
                val domains = mutableSetOf<String>()
                apps.filter { it.isBlocked }.forEach { app ->
                    domains.addAll(BlockedDomains.getDomainsForPackage(app.packageName))
                }
                domains.addAll(customDomains.map { it.domain })
                domains
            }.collect { combinedDomains ->
                blockedDomains = combinedDomains
                if (combinedDomains.isEmpty() && isRunning) {
                    isRunning = false
                    try {
                        vpnInterface?.close()
                    } catch (e: Exception) {
                        Log.e("VPN", "Error closing vpn interface", e)
                    }
                    vpnInterface = null
                } else if (combinedDomains.isNotEmpty() && !isRunning) {
                    startVpn()
                }
            }
        }
    }

    private fun startVpn() {
        val builder = Builder()
            .setSession("FocusGuard")
            .addAddress("10.0.0.2", 32)
            .addDnsServer("10.0.0.1")
            .addRoute("10.0.0.1", 32)
            .setBlocking(false)

        vpnInterface = builder.establish()
        isRunning = true

        Thread { handleDnsPackets() }.start()
    }

    private fun handleDnsPackets() {
        val vpnFd = vpnInterface?.fileDescriptor ?: return
        val inputStream = FileInputStream(vpnFd)
        val outputStream = FileOutputStream(vpnFd)
        val buffer = ByteArray(32767)

        while (isRunning) {
            try {
                val length = inputStream.read(buffer)
                if (length <= 0) continue

                val packet = buffer.copyOf(length)

                val dnsQuery = parseDnsQuery(packet)

                if (dnsQuery != null && isDomainBlocked(dnsQuery.domain)) {
                    val response = buildBlockedDnsResponse(packet, dnsQuery)
                    outputStream.write(response)
                } else {
                    val response = forwardDnsQuery(packet)
                    if (response != null) outputStream.write(response)
                }
            } catch (e: Exception) {
                if (isRunning) Log.e("VPN", "Packet handling error", e)
            }
        }
    }

    private fun isDomainBlocked(domain: String): Boolean {
        return blockedDomains.any { blocked ->
            domain == blocked || domain.endsWith(".$blocked")
        }
    }

    private fun parseDnsQuery(packet: ByteArray): DnsQuery? {
        try {
            if (packet.size < 32) return null
            val dnsOffset = 28
            var pos = dnsOffset + 12
            val domainParts = mutableListOf<String>()
            while (pos < packet.size && packet[pos] != 0.toByte()) {
                val len = packet[pos].toInt() and 0xFF
                pos++
                if (pos + len > packet.size) break
                domainParts.add(String(packet, pos, len))
                pos += len
            }
            val domain = domainParts.joinToString(".")
            return if (domain.isNotEmpty()) DnsQuery(domain) else null
        } catch (e: Exception) {
            return null
        }
    }

    private fun buildBlockedDnsResponse(query: ByteArray, dnsQuery: DnsQuery): ByteArray {
        val response = query.copyOf()
        response[28 + 2] = 0x81.toByte()
        response[28 + 3] = 0x83.toByte()
        return response
    }

    private fun forwardDnsQuery(packet: ByteArray): ByteArray? {
        return try {
            val socket = DatagramSocket()
            protect(socket)
            val dnsBytes = packet.copyOfRange(28, packet.size)
            val googleDns = InetAddress.getByName("8.8.8.8")
            val sendPacket = DatagramPacket(dnsBytes, dnsBytes.size, googleDns, 53)
            socket.soTimeout = 3000
            socket.send(sendPacket)
            val responseBuffer = ByteArray(512)
            val receivePacket = DatagramPacket(responseBuffer, responseBuffer.size)
            socket.receive(receivePacket)
            socket.close()
            wrapInIpUdp(packet, responseBuffer.copyOf(receivePacket.length))
        } catch (e: Exception) {
            null
        }
    }

    private fun wrapInIpUdp(originalPacket: ByteArray, dnsResponse: ByteArray): ByteArray {
        val totalLen = 28 + dnsResponse.size
        val result = ByteArray(totalLen)
        System.arraycopy(originalPacket, 0, result, 0, 28)
        System.arraycopy(dnsResponse, 0, result, 28, dnsResponse.size)
        result[12] = originalPacket[16]; result[13] = originalPacket[17]
        result[14] = originalPacket[18]; result[15] = originalPacket[19]
        result[16] = originalPacket[12]; result[17] = originalPacket[13]
        result[18] = originalPacket[14]; result[19] = originalPacket[15]
        result[20] = originalPacket[22]; result[21] = originalPacket[23]
        result[22] = originalPacket[20]; result[23] = originalPacket[21]
        return result
    }

    private fun stopVpn() {
        isRunning = false
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e("VPN", "Error closing vpn interface", e)
        }
        vpnInterface = null
    }

    override fun onRevoke() {
        stopVpn()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    data class DnsQuery(val domain: String)

    companion object {
        const val ACTION_STOP = "STOP_VPN"

        fun start(context: Context) {
            val intent = Intent(context, FocusVpnService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusVpnService::class.java)
                .apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }
}
