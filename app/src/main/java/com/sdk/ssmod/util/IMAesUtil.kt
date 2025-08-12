package com.sdk.ssmod.util

import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object IMAesUtil {
    private const val transformation: String = "AES/CBC/PKCS5Padding"
    const val PWD_BINARY_SYS: String =
        "1010111 1000110 110101 1110010 1001111 1010001 1011010 1000111 1100001 1110011 1100101 110011 1110001 1010011 110110 1000101 1010011 1100110 1001110 1010000 111000 1100001 1010110 1100001 1011010 1001100 1101010 1101111 1010101 1101010 1001011 1100111"

    private const val algorithm: String = "AES"


    fun encrypt(iv: ByteArray, inputStream: InputStream, outputStream: OutputStream): Boolean {
        checkIvSize(iv)
        return try {
            val pwd = readPsd2Str(PWD_BINARY_SYS)
            val secretKey = SecretKeySpec(pwd.toByteArray(Charsets.UTF_8), algorithm)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                val output = cipher.update(buffer, 0, bytesRead)
                if (output != null) {
                    outputStream.write(output)
                }
            }

            val finalBytes = cipher.doFinal()
            if (finalBytes != null) {
                outputStream.write(finalBytes)
            }

            outputStream.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun decrypt(iv: ByteArray, inputStream: InputStream, outputStream: OutputStream): Boolean {
        checkIvSize(iv)
        return try {
            val pwd = readPsd2Str(PWD_BINARY_SYS)
            Log.d("decrypt", pwd)
            val secretKey = SecretKeySpec(pwd.toByteArray(Charsets.UTF_8), algorithm)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                val output = cipher.update(buffer, 0, bytesRead)
                if (output != null) {
                    outputStream.write(output)
                }
            }

            val finalBytes = cipher.doFinal()
            if (finalBytes != null) {
                outputStream.write(finalBytes)
            }

            outputStream.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace() // 处理异常，输出错误信息
            false // 解密失败
        }
    }

    fun readPsd2Str(psd: String): String {
        return psd.split(" ")
            .joinToString("") { binStrToChar(it).toString() }
    }

    private fun binStrToChar(binStr: String): Char {
        return binStr.fold(0) { acc, c -> (acc shl 1) + (c - '0') }.toChar()
    }


    private fun checkIvSize(iv: ByteArray) {
        if (iv.size != 16) throw AssertionError("iv must be exactly 16 bytes long.")
    }

}