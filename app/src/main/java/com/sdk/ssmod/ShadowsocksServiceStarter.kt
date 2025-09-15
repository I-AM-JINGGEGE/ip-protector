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
    private val enableIPv6Support: Boolean = false,
    /**
     * Domain names to bypass proxy (direct connection)
     */
    private val bypassDomains: List<String> = emptyList()
) {

    fun run() {
        // 如果有需要绕过的域名，修改 BYPASS_LAN 规则
        if (bypassDomains.isNotEmpty()) {
            createModifiedBypassLanRules(bypassDomains)
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
            // 始终使用 BYPASS_LAN，但规则可能被修改
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

    private fun createModifiedBypassLanRules(bypassDomains: List<String>) {
        val modifiedRules = buildString {
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
                when {
                    // 如果是 IP 地址或 IP 段，直接添加
                    domain.matches(Regex("^\\d+\\.\\d+\\.\\d+\\.\\d+(/\\d+)?$")) -> {
                        appendLine(domain)
                    }
                    // 如果已经是正则表达式格式，直接添加
                    domain.startsWith("(?:") -> {
                        appendLine(domain)
                    }
                    // 如果是通配符格式 *.domain.com，转换为正则表达式
                    domain.startsWith("*.") -> {
                        val realDomain = domain.substring(2) // 移除 *.
                        val escapedDomain = realDomain.replace(".", "\\.")
                        appendLine("(?:^|\\.)$escapedDomain$")
                    }
                    // 普通域名，添加精确匹配和子域名匹配
                    else -> {
                        val escapedDomain = domain.replace(".", "\\.")
                        // 精确匹配
                        appendLine("^$escapedDomain$")
                        // 子域名匹配
                        appendLine("(?:^|\\.)$escapedDomain$")
                    }
                }
            }
        }

        // 直接修改 bypass-lan.acl 文件
        try {
            val bypassLanFile = Acl.getFile(Acl.BYPASS_LAN)
            bypassLanFile.writeText(modifiedRules)
        } catch (e: Exception) { }
    }
}
