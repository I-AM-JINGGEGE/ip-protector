package com.sdk.ssmod.api.http.beans

import com.google.gson.annotations.SerializedName

/**
 * TODO: Consider a better & descriptive name.
 */
class FetchResponse(
    @SerializedName("error")
    var error: Int? = null,
    @SerializedName("cfg")
    var config: Config? = null,
    @SerializedName("isNewUser")
    var isNewUser: Boolean? = null,
    @SerializedName("ss")
    var serverZones: List<ServerZone>? = null
) {
    data class Config(
        var upForce: Int? = null,
        var upSuggest: Int? = null,
        var testReadTimeout: Int? = null,
        var servs: List<String>? = null,
        @SerializedName("testConnTimeout")
        var testConnectionTimeout: Int? = null,
        var rankDelay: Int? = null
    )

    class ServerZone(
        @SerializedName("i")
        var id: String? = null,
        @Deprecated("Use isVip instead.", ReplaceWith("isVip"))
        var vip: Int? = null,
        @Deprecated("Use continent instead.", ReplaceWith("continent"))
        var co: String? = null,
        @SerializedName("c")
        var country: String? = null,
        @SerializedName("n")
        var city: String? = null,
        @SerializedName("h")
        var hosts: List<Host>? = null,
        @SerializedName("sl")
        @Deprecated("Use signalStrength instead", ReplaceWith("signalStrength"))
        var signalLevel: Int? = null
    ) {
        @Suppress("DEPRECATION")
        val isVip: Boolean
            get() = vip != 0

        @Suppress("DEPRECATION")
        val continent: Continent
            get() = co?.let {
                Continent.fromTextRepresentation(it)
            } ?: Continent.Unknown

        @Suppress("DEPRECATION")
        val signalStrength: SignalStrength
            get() = signalLevel?.let {
                SignalStrength.fromSignalLevel(it)
            } ?: SignalStrength.Lost
    }

    enum class Continent(val textRepresentation: String) {
        Unknown("Unknown"),
        Africa("Africa"),
        Asia("Asia"),
        Europe("Europe"),
        NorthAmerica("North America"),
        SouthAmerica("South America"),
        Antarctica("Antarctica"),
        Australia("Australia");

        companion object {
            fun fromTextRepresentation(text: String) =
                values().firstOrNull { it.textRepresentation == text }
        }
    }

    enum class SignalStrength(val signalLevel: Int) {
        Excellent(5), Good(4), Okay(3),
        Poor(2), Crap(1), Lost(0);

        companion object {
            fun fromSignalLevel(value: Int) =
                values().firstOrNull { it.signalLevel == value }
        }
    }

    data class Host(
        @SerializedName("a")
        var host: String? = null,
        @SerializedName("r")
        var port: Int? = null,
        @SerializedName("p")
        var password: String? = null,
        @SerializedName("k")
        var rankingFactor: Int? = null,
        /*
         * Note: Fields `adUnitIdPrimary` and `adUnitIdBackup` are subject to change because they
         * are used by NoCardVPN app only for now.
         */
        @SerializedName("d_p")
        var adUnitIdPrimary: String? = null,
        @SerializedName("d_b")
        var adUnitIdBackup: String? = null,
        /**
         * The lower the value, the higher the income it generates.
         */
        @SerializedName("e")
        var ecpmLevel: Int? = null,
        @Deprecated("Reserved")
        var s1: Any? = null,
        @Deprecated("Reserved")
        var s2: Any? = null
    ) {
        /**
         * Possible value between `[0, Int.MAX_VALUE]`.
         * The host is unreachable if the value is [Int.MAX_VALUE].
         */
        @Transient
        @Volatile
        var latency: Int = Int.MAX_VALUE

        val isRemoteReachable: Boolean get() = latency != Int.MAX_VALUE && latency >= 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Host

            if (host != other.host) return false
            if (port != other.port) return false
            if (password != other.password) return false
            if (rankingFactor != other.rankingFactor) return false
            if (adUnitIdPrimary != other.adUnitIdPrimary) return false
            if (adUnitIdBackup != other.adUnitIdBackup) return false

            return true
        }

        override fun hashCode(): Int {
            var result = host?.hashCode() ?: 0
            result = 31 * result + (port ?: 0)
            result = 31 * result + (password?.hashCode() ?: 0)
            result = 31 * result + (rankingFactor ?: 0)
            result = 31 * result + (adUnitIdPrimary?.hashCode() ?: 0)
            result = 31 * result + (adUnitIdBackup?.hashCode() ?: 0)
            return result
        }
    }
}
