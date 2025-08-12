package com.github.shadowsocks.imsvc.connection

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Thread-safe: yes, immutable.
 */
@Parcelize
class ConnectedTo(
    val zoneId: String? = null,
    val country: String? = null,
    val host: Host? = null
) : Parcelable {

    /**
     * Thread-safe: yes, immutable.
     */
    @Parcelize
    class Host(val host: String? = null, val port: Int? = null) : Parcelable
}
