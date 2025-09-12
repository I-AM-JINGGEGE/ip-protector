package com.sdk.ssmod

import com.github.shadowsocks.Core
import com.github.shadowsocks.acl.Acl
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.net.Subnet.Companion.fromString

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
    private val enableIPv6Support: Boolean = false,
    /**
     * Domain names to bypass proxy (direct connection)
     */
    private val bypassDomains: List<String> = emptyList()
) {

    fun run() {
        // 如果有需要绕过的域名，创建自定义规则
        if (bypassDomains.isNotEmpty()) {
            createCustomAclRules(bypassDomains)
        }
        
        ProfileManager.clear()
        var profile = Profile(
            name = name,
            // below are 4 obvious parameters.
            host = host,
            remotePort = port,
            password = password,
            method = "aes-256-gcm",
            proxyApps = true, // We proxy apps, yes we do.
            // 如果有自定义域名，使用自定义规则，否则保持原有设置
            route = if (bypassDomains.isNotEmpty()) Acl.CUSTOM_RULES else Acl.BYPASS_LAN,
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

    private fun createCustomAclRules(bypassDomains: List<String>) {
        val customRules = buildString {
            // 保持原有的 BYPASS_LAN 行为
            appendLine("[proxy_all]")
            appendLine()
            appendLine("[bypass_list]")

            // 完全复制 bypass-lan.acl 的所有IP段
            appendLine("0.0.0.0/8")
            appendLine("10.0.0.0/8")
            appendLine("100.64.0.0/10")
            appendLine("127.0.0.0/8")
            appendLine("169.254.0.0/16")
            appendLine("172.16.0.0/12")
            appendLine("192.0.0.0/24")
            appendLine("192.0.2.0/24")
            appendLine("192.31.196.0/24")
            appendLine("192.52.193.0/24")
            appendLine("192.88.99.0/24")
            appendLine("192.168.0.0/16")
            appendLine("192.175.48.0/24")
            appendLine("198.18.0.0/15")
            appendLine("198.51.100.0/24")
            appendLine("203.0.113.0/24")
            appendLine("224.0.0.0/3")
            
            // 添加您要绕过的域名
            bypassDomains.forEach { domain ->
                appendLine(domain)
            }
        }
        
        // 保存自定义规则
        Acl.customRules = Acl().apply {
            fromReader(customRules.reader(), true)
        }
    }

}
