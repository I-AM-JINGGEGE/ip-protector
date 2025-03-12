package com.sdk.ssmod.util

import android.os.IInterface

inline fun <T> tryIgnoreException(block: () -> T?): T? =
    try {
        block()
    } catch (ignore: Exception) {
        null
    }

fun AutoCloseable.closeQuietly() {
    tryIgnoreException { close() }
}

fun <T : IInterface> T.pingOrNull(): T? =
    if (this.asBinder().pingBinder()) this else null
