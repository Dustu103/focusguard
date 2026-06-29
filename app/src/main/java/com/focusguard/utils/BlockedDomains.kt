package com.focusguard.utils

object BlockedDomains {

    private val domainMap = mapOf(
        "com.instagram.android" to listOf(
            "instagram.com", "www.instagram.com", "i.instagram.com",
            "cdninstagram.com", "static.cdninstagram.com"
        ),
        "com.google.android.youtube" to listOf(
            "youtube.com", "www.youtube.com", "m.youtube.com",
            "youtu.be", "youtube-nocookie.com"
        ),
        "com.zhiliaoapp.musically" to listOf(
            "tiktok.com", "www.tiktok.com", "vm.tiktok.com",
            "musical.ly"
        ),
        "com.snapchat.android" to listOf(
            "snapchat.com", "www.snapchat.com"
        ),
        "com.facebook.katana" to listOf(
            "facebook.com", "www.facebook.com", "m.facebook.com",
            "fb.com"
        ),
        "com.twitter.android" to listOf(
            "twitter.com", "www.twitter.com", "x.com", "www.x.com",
            "t.co"
        ),
        "com.reddit.frontpage" to listOf(
            "reddit.com", "www.reddit.com", "old.reddit.com",
            "redd.it"
        ),
        "com.activision.callofduty.shooter" to listOf<String>(),
        "com.pubg.imobile" to listOf<String>()
    )

    fun getDomainsForPackage(packageName: String): List<String> {
        return domainMap[packageName] ?: emptyList()
    }

    fun getAllBlockedDomains(blockedPackages: List<String>): Set<String> {
        return blockedPackages.flatMap { getDomainsForPackage(it) }.toSet()
    }
}
