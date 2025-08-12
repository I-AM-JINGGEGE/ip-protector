package com.sdk.ssmod.api.http.beans

import com.sdk.ssmod.net.TcpPing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.selectUnbiased
import java.net.InetSocketAddress
import java.util.SortedSet

/*
 * New ping idea:
 * 1. Pick 1 host from each server zone that matches the following criteria:
 *   a. Highest ranked, the smaller the value the higher the rank;
 *   b. If there's more than one host available in the same rank, pick 1 randomly.
 * 2. Ping them, let's say the US-Atalanta one has the lowest latency;
 * 3. Ping all hosts from US-Atalanta server zone by ranking(applies the same ranking algorithm as above);
 * 4. If any host is reachable, stop pinging, return the one with lowest latency as the best host.
 */

suspend fun FetchResponse.Host.tcpPing(): Deferred<Int> = coroutineScope {
    val address = InetSocketAddress(host!!, port!!)
    val latency = TcpPing(address, 4000).ping().await()
    this@tcpPing.latency = latency
    CompletableDeferred(latency)
}

val FetchResponse.ServerZone.bestHost: FetchResponse.Host?
    get() = hosts?.filter { it.isRemoteReachable }?.minByOrNull { it.latency }

suspend fun FetchResponse.ServerZone.pingAllOrderByRankingFirstReachableAsync(
): Deferred<FetchResponse.Host?> = coroutineScope {
    async {
        val hosts = hosts ?: return@async null
        val tiers = hosts.mapNotNull { it.rankingFactor }.toSortedSet().toList()
        for (tier in tiers) {
            val host = selectUnbiased<FetchResponse.Host?> {
                hosts.filter { it.rankingFactor == tier }.forEach { host ->
                    async { host.tcpPing().await() }.onAwait { _ ->
                        if (host.isRemoteReachable) host else null
                    }
                }
            }
            if (host != null) return@async host
        }
        null
    }
}
