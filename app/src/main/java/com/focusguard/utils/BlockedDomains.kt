package com.focusguard.utils

object BlockedDomains {

    private val domainMap = mapOf(
        // Social Media
        "com.instagram.android" to listOf(
            "instagram.com", "www.instagram.com", "i.instagram.com",
            "cdninstagram.com", "static.cdninstagram.com"
        ),
        "com.facebook.katana" to listOf(
            "facebook.com", "www.facebook.com", "m.facebook.com",
            "fb.com", "fbcdn.net"
        ),
        "com.twitter.android" to listOf(
            "twitter.com", "www.twitter.com", "x.com", "www.x.com",
            "t.co", "twimg.com"
        ),
        "com.snapchat.android" to listOf(
            "snapchat.com", "www.snapchat.com", "snap.com"
        ),
        "com.pinterest" to listOf(
            "pinterest.com", "www.pinterest.com", "pin.it"
        ),

        // Professional Networks
        "com.linkedin.android" to listOf(
            "linkedin.com", "www.linkedin.com", "api.linkedin.com",
            "media.licdn.com", "static.licdn.com", "lnkd.in"
        ),

        // Video / Entertainment
        "com.google.android.youtube" to listOf(
            "youtube.com", "www.youtube.com", "m.youtube.com",
            "youtu.be", "youtube-nocookie.com", "googlevideo.com",
            "yt3.ggpht.com", "ytimg.com"
        ),
        "com.netflix.mediaclient" to listOf(
            "netflix.com", "www.netflix.com", "nflximg.net",
            "nflxso.net", "nflxvideo.net", "fast.com"
        ),
        "tv.twitch.android.app" to listOf(
            "twitch.tv", "www.twitch.tv", "twitchapps.com",
            "jtvnw.net", "twitchsvc.net", "twitchstatic.com"
        ),

        // Short Video
        "com.zhiliaoapp.musically" to listOf(
            "tiktok.com", "www.tiktok.com", "vm.tiktok.com",
            "musical.ly", "tiktokcdn.com", "tiktokv.com"
        ),

        // Messaging (optional blocking)
        "com.whatsapp" to listOf(
            "whatsapp.com", "www.whatsapp.com", "whatsapp.net",
            "wa.me"
        ),
        "org.telegram.messenger" to listOf(
            "telegram.org", "t.me", "telegram.me",
            "core.telegram.org"
        ),
        "com.discord" to listOf(
            "discord.com", "www.discord.com", "discordapp.com",
            "discord.gg", "discord.media", "discordcdn.com"
        ),

        // News / Forums
        "com.reddit.frontpage" to listOf(
            "reddit.com", "www.reddit.com", "old.reddit.com",
            "redd.it", "redditmedia.com", "reddituploads.com"
        ),

        // Gaming
        "com.activision.callofduty.shooter" to listOf(
            "callofduty.com", "www.callofduty.com"
        ),
        "com.pubg.imobile" to listOf(
            "pubg.com", "www.pubg.com"
        ),
        "com.garena.game.freefire" to listOf(
            "freefiremobile.com", "garena.com"
        ),
        "com.roblox.client" to listOf(
            "roblox.com", "www.roblox.com", "rbxcdn.com"
        )
    )

    fun getDomainsForPackage(packageName: String): List<String> {
        return domainMap[packageName] ?: emptyList()
    }

    fun getAllBlockedDomains(blockedPackages: List<String>): Set<String> {
        return blockedPackages.flatMap { getDomainsForPackage(it) }.toSet()
    }
}
