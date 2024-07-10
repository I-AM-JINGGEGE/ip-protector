package com.ironmeta.one.app

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.ironmeta.one.MainApplication

object AppSignature {
    private val packageManager get() = MainApplication.instance.packageManager!!
    private val appInfo get() = packageManager.getApplicationInfo("com.nocardteam.take.off", 0)
    private val packageInfo: PackageInfo
        @SuppressLint("PackageManagerGetSignatures")
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val flags = PackageManager.GET_SIGNING_CERTIFICATES
            packageManager.getPackageInfo(appInfo.packageName, flags)
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(appInfo.packageName, PackageManager.GET_SIGNATURES)
        }
    fun signatureByteArray(): ByteArray {
        return packageInfo.signaturesCompat.first().toByteArray()
    }
    private val PackageInfo.signaturesCompat
        get() = if (Build.VERSION.SDK_INT >= 28) {
            signingInfo.apkContentsSigners
        } else {
            @Suppress("DEPRECATION") signatures
        }
}