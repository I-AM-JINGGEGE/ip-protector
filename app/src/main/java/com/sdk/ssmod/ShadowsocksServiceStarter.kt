package com.sdk.ssmod

import com.github.shadowsocks.Core
import com.github.shadowsocks.acl.Acl
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager

internal class ShadowsocksServiceStarter(
    /**
     * Profile name, will be displayed in system notification.
     */
    private val name: String,
    private val host: String,
    private val port: Int,
    private val password: String,
    /**
     * Application IDs to exclude from being proxied.
     */
    private val packageNames: List<String>,
    private val enableIPv6Support: Boolean = false
) {

    fun run() {
        ProfileManager.clear()
        var profile = Profile(
            name = name,
            // below are 4 obvious parameters.
            host = host,
            remotePort = port,
            password = password,
            method = "aes-256-gcm",
            proxyApps = true, // We proxy apps, yes we do.
            // Use BYPASS_LAN because our target market is world wide exclude Mainland China.
            route = Acl.BYPASS_LAN,
            // Bypass Mode: any app not excluded are proxied by VPN.
            bypass = true,
            // Apps you want to bypass/connect to VPN, separated by '\n'.
            individual = packageNames.joinToString("\n"),
            ipv6 = enableIPv6Support
        )
        profile = ProfileManager.createProfile(profile)
        Core.switchProfile(profile.id)
        Core.startService()
    }

}
