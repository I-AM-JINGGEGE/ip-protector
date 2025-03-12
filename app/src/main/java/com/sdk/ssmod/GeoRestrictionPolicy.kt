package com.sdk.ssmod

interface IGeoRestrictionPolicy {
    var blockChina: Boolean
    var blockCountriesSanctionedByUS: Boolean

    fun isGeoRestricted(): Boolean
}

class GeoRestrictionPolicyImpl(
    override var blockChina: Boolean = false,
    override var blockCountriesSanctionedByUS: Boolean = false
) : IGeoRestrictionPolicy {
    override fun isGeoRestricted(): Boolean =
        isChinaBlocked() || isCountriesSanctionedByUSBlocked()

    fun isChinaBlocked(): Boolean {
        val simMcc = IMSDK.device.simMcc
        val netMcc = IMSDK.device.netMcc
        val countryCode = IMSDK.device.os.country
        val isChina = simMcc == 460 || netMcc == 460 || countryCode.equals("cn", true)
        return blockChina && isChina
    }

    fun isCountriesSanctionedByUSBlocked(): Boolean {
        val simMcc = IMSDK.device.simMcc
        val netMcc = IMSDK.device.netMcc
        val countryCode = IMSDK.device.os.country

        val hasSanctionedMcc = COUNTRIES_SANCTIONED_BY_US.values
            .firstOrNull { simMcc == it || netMcc == it } != null
        if (hasSanctionedMcc) return true

        val hasSanctionedCountryCode = COUNTRIES_SANCTIONED_BY_US.keys
            .firstOrNull { countryCode.equals(it, true) } != null
        if (hasSanctionedCountryCode) return true

        return false
    }

    companion object {
        private val COUNTRIES_SANCTIONED_BY_US = mapOf(
            Pair("cu", 368), // Cuba
            Pair("ir", 432), // Iran
            Pair("kp", 467), // North Korea
            Pair("sy", 417), // Syria
        )
    }
}
