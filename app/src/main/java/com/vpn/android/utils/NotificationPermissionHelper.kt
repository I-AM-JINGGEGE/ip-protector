package com.vpn.android.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * 通知权限请求工具类
 * 支持Android 13+的通知权限请求
 */
class NotificationPermissionHelper(private val activity: FragmentActivity) {
    
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    
    init {
        setupPermissionLauncher()
    }
    
    private fun setupPermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onPermissionResult?.invoke(isGranted)
            if (!isGranted) {
                showPermissionDeniedDialog()
            }
        }
    }
    
    /**
     * 检查通知权限是否已授予
     */
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要明确请求通知权限
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 13以下，检查通知是否启用
            NotificationManagerCompat.from(activity).areNotificationsEnabled()
        }
    }
    
    /**
     * 请求通知权限
     * @param onResult 权限请求结果回调
     */
    fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult
        
        if (isNotificationPermissionGranted()) {
            onResult(true)
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 请求通知权限
            permissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Android 13以下，引导用户到设置页面
            showNotificationSettingsDialog()
        }
    }
    
    /**
     * 显示权限被拒绝的对话框
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("通知权限")
            .setMessage("需要通知权限来显示VPN连接状态。请在设置中开启通知权限。")
            .setPositiveButton("去设置") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 显示通知设置对话框（Android 13以下）
     */
    private fun showNotificationSettingsDialog() {
        AlertDialog.Builder(activity)
            .setTitle("通知权限")
            .setMessage("需要开启通知权限来显示VPN连接状态。请在设置中开启通知。")
            .setPositiveButton("去设置") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 打开通知设置页面
     */
    private fun openNotificationSettings() {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                    }
                    else -> {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", activity.packageName)
                        putExtra("app_uid", activity.applicationInfo.uid)
                    }
                }
            }
            activity.startActivity(intent)
        } catch (e: Exception) {
            // 如果无法打开应用通知设置，则打开系统设置
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
        }
    }
    
    /**
     * 检查是否应该显示权限请求说明
     */
    fun shouldShowRequestPermissionRationale(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }
    }
    
    /**
     * 显示权限请求说明对话框
     */
    fun showPermissionRationaleDialog(onPositiveClick: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("通知权限说明")
            .setMessage("为了提供更好的VPN服务体验，我们需要通知权限来显示连接状态、流量统计等信息。")
            .setPositiveButton("确定") { _, _ ->
                onPositiveClick()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    companion object {
        /**
         * 静态方法：检查通知权限是否已授予
         */
        fun isNotificationPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
    }
}
