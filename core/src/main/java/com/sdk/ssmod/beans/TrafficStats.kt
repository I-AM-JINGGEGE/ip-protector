package com.sdk.ssmod.beans

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.github.shadowsocks.aidl.TrafficStats as TrafficStatsSs

@Parcelize
data class TrafficStats(
    // Bytes per second
    var txRate: Long = 0L,
    var rxRate: Long = 0L,

    // Bytes for the current session
    var txTotal: Long = 0L,
    var rxTotal: Long = 0L
) : Parcelable {
    operator fun plus(other: TrafficStats) = TrafficStats(
        txRate + other.txRate, rxRate + other.rxRate,
        txTotal + other.txTotal, rxTotal + other.rxTotal
    )

    companion object {
        fun fromTheSsOne(stats: TrafficStatsSs) =
            TrafficStats(stats.txRate, stats.rxRate, stats.txTotal, stats.rxTotal)
    }
}

fun TrafficStatsSs.toImsdkOne(): TrafficStats =
    TrafficStats(this.txRate, this.rxRate, this.txTotal, this.rxTotal)
