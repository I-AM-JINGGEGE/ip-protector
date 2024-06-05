package com.ironmeta.one.utils;

import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;

import ai.datatower.analytics.ROIQueryAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SystemPropertyUtils {

    private static final String USER_PROPERTY_SYSTEM_GP_VERSION_CODE = "gp_version_code";
    private static final String USER_PROPERTY_SYSTEM_GP_VERSION_NAME = "gp_version_name";
    private static final String USER_PROPERTY_SYSTEM_DIMS_SIZE = "dims_size";
    private static final String USER_PROPERTY_SYSTEM_DIMS_X_DP = "dims_x_dp";
    private static final String USER_PROPERTY_SYSTEM_DIMS_Y_DP = "dims_y_dp";
    private static final String USER_PROPERTY_SYSTEM_DIMS_X_PX = "dims_x_px";
    private static final String USER_PROPERTY_SYSTEM_DIMS_Y_PX = "dims_y_px";
    private static final String USER_PROPERTY_SYSTEM_DIMS_D_DPI = "dims_d_dpi";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_ARCH = "bp_ro_arch";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_CHIPNAME = "bp_ro_chipname";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_NATIVE_BRIDGE = "bp_ro_dalvik_vm_native_bridge";
    private static final String USER_PROPERTY_SYSTEM_BP_PERSIST_SYS_NATIVEBRIDGE = "bp_persist_sys_nativebridge";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_ENABLE_NATIVE_BRIDGE_EXEC = "bp_ro_enable_native_bridge_exec";
    private static final String USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_FEATURES = "bp_dalvik_vm_isa_x86_features";
    private static final String USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_VARIANT = "bp_dalvik_vm_isa_x86_variant";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_ZYGOTE = "bp_ro_zygote";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_ALLOW_MOCK_LOCATION = "bp_ro_allow_mock_location";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_ISA_ARM = "bp_ro_dalvik_vm_isa_arm";
    private static final String USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_FEATURES = "bp_dalvik_vm_isa_arm_features";
    private static final String USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_VARIANT = "bp_dalvik_vm_isa_arm_variant";
    private static final String USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_FEATURES = "bp_dalvik_vm_isa_arm64_features";
    private static final String USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_VARIANT = "bp_dalvik_vm_isa_arm64_variant";
    private static final String USER_PROPERTY_SYSTEM_BP_BUILD_DISPLAY_ID = "bp_ro_build_display_id";
    private static final String USER_PROPERTY_SYSTEM_BP_VZW_OS_ROOTED = "bp_vzw_os_rooted";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_BUILD_USER = "bp_ro_build_user";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_KERNEL_QEMU = "bp_ro_kernel_qemu";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_HARDWARE = "bp_ro_hardware";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABI = "bp_ro_product_cpu_abi";
    private static final String USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABILIST = "bp_ro_product_cpu_abilist";
    private static final String USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST32 = "bp_product_cpu_abilist32";
    private static final String USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST64 = "bp_product_cpu_abilist64";
    private static final String USER_PROPERTY_SYSTEM_LAUNCHER_PKG = "launcher_pkg";
    private static final String USER_PROPERTY_SYSTEM_DEVICE_DISPLAY = "device_display";
    private static final String USER_PROPERTY_SYSTEM_DEVICE_PRODUCT = "device_product";
    private static final String USER_PROPERTY_SYSTEM_DEVICE_BRAND = "device_brand";
    private static final String USER_PROPERTY_SYSTEM_DEVICE_DEVICE = "device_device";
    private static final String USER_PROPERTY_SYSTEM_OS_SDK_VERSION = "os_sdk_version";
    private static final String USER_PROPERTY_SYSTEM_DATA_FOLDER_USED = "data_folder_used";
    private static final String USER_PROPERTY_SYSTEM_DEVICE_SENSOR = "device_sensor";
    //new 0518
    private static final String USER_PROPERTY_SYSTEM_BUILD_INCREMENTAL = "build_incremental";
    private static final String USER_PROPERTY_SYSTEM_BUILD_SDK = "build_sdk";
    private static final String USER_PROPERTY_SYSTEM_USER_AGENT_WEBVIEW = "user_agent_webview";
    private static final String USER_PROPERTY_SYSTEM_BUILD_HOST = "build_host";
    private static final String USER_PROPERTY_SYSTEM_BUILD_FINGERPRINT = "build_fingerprint";
    private static final String USER_PROPERTY_SYSTEM_BASEBAND = "baseband";
    private static final String USER_PROPERTY_SYSTEM_BUILD_BOARD = "build_board";
    private static final String USER_PROPERTY_SYSTEM_BUILD_ID = "build_id";


    public static void track(Context context) {
        ROIQueryAnalytics.userSetOnce(getSystemPropertiesForUserSet(context));
    }

    private static JSONObject getSystemPropertiesForUserSet(Context context) {
        JSONObject properties = new JSONObject();
        try {
            properties.put(USER_PROPERTY_SYSTEM_GP_VERSION_CODE, getAppVersionCode(context, "com.android.vending"));
            properties.put(USER_PROPERTY_SYSTEM_GP_VERSION_NAME, getAppVersionName(context, "com.android.vending"));
            properties.put(USER_PROPERTY_SYSTEM_DIMS_SIZE, getScreenSize(context));
            properties.put(USER_PROPERTY_SYSTEM_DIMS_X_DP, getScreenWidthWithDp(context));
            properties.put(USER_PROPERTY_SYSTEM_DIMS_Y_DP, getScreenHeightWithDp(context));
            properties.put(USER_PROPERTY_SYSTEM_DIMS_X_PX, getScreenWidth(context));
            properties.put(USER_PROPERTY_SYSTEM_DIMS_Y_PX, getScreenHeight(context));
            properties.put(USER_PROPERTY_SYSTEM_DIMS_D_DPI, getDensityDpi(context));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_ARCH, getProperty("ro.arch"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_CHIPNAME, getProperty("ro.chipname"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_NATIVE_BRIDGE, getProperty("ro.dalvik.vm.native.bridge"));
            properties.put(USER_PROPERTY_SYSTEM_BP_PERSIST_SYS_NATIVEBRIDGE, getProperty("persist.sys.nativebridge"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_ENABLE_NATIVE_BRIDGE_EXEC, getProperty("ro.enable.native.bridge.exec"));
            properties.put(USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_FEATURES, getProperty("dalvik.vm.isa.x86.features"));
            properties.put(USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_VARIANT, getProperty("dalvik.vm.isa.x86.variant"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_ZYGOTE, getProperty("ro.zygote"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_ALLOW_MOCK_LOCATION, getProperty("ro.allow.mock.location"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_ISA_ARM, getProperty("ro.dalvik.vm.isa.arm"));
            properties.put(USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_FEATURES, getProperty("dalvik.vm.isa.arm.features"));
            properties.put(USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_VARIANT, getProperty("dalvik.vm.isa.arm.variant"));
            properties.put(USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_FEATURES, getProperty("dalvik.vm.isa.arm64.features"));
            properties.put(USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_VARIANT, getProperty("dalvik.vm.isa.arm64.variant"));
            properties.put(USER_PROPERTY_SYSTEM_BP_VZW_OS_ROOTED, getProperty("vzw.os.rooted"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_BUILD_USER, getProperty("ro.build.user"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_KERNEL_QEMU, getProperty("ro.kernel.qemu"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_HARDWARE, getProperty("ro.hardware"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABI, getProperty("ro.product.cpu.abi"));
            properties.put(USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABILIST, getProperty("ro.product.cpu.abilist"));
            properties.put(USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST32, getProperty("ro.product.cpu.abilist32"));
            properties.put(USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST64, getProperty("ro.product.cpu.abilist64"));
            properties.put(USER_PROPERTY_SYSTEM_BP_BUILD_DISPLAY_ID, getProperty("ro.build.display.id"));
            properties.put(USER_PROPERTY_SYSTEM_LAUNCHER_PKG, getLauncherPackageName(context));
            properties.put(USER_PROPERTY_SYSTEM_OS_SDK_VERSION, Build.VERSION.SDK_INT);
            properties.put(USER_PROPERTY_SYSTEM_DATA_FOLDER_USED, dataFolderUsed());
            properties.put(USER_PROPERTY_SYSTEM_DEVICE_SENSOR, getSensor(context));
            properties.put(USER_PROPERTY_SYSTEM_DEVICE_DISPLAY, Build.DISPLAY);
            properties.put(USER_PROPERTY_SYSTEM_DEVICE_PRODUCT, Build.PRODUCT);
            properties.put(USER_PROPERTY_SYSTEM_DEVICE_BRAND, Build.BRAND);
            properties.put(USER_PROPERTY_SYSTEM_DEVICE_DEVICE, Build.DEVICE);
            properties.put(USER_PROPERTY_SYSTEM_BUILD_INCREMENTAL, Build.VERSION.INCREMENTAL);
            properties.put(USER_PROPERTY_SYSTEM_BUILD_SDK, Build.VERSION.SDK);
            properties.put(USER_PROPERTY_SYSTEM_BUILD_HOST, Build.HOST);
            properties.put(USER_PROPERTY_SYSTEM_BUILD_FINGERPRINT, Build.FINGERPRINT);
            properties.put(USER_PROPERTY_SYSTEM_BASEBAND, getProperty("gsm.version.baseband"));
            properties.put(USER_PROPERTY_SYSTEM_BUILD_BOARD, Build.BOARD);
            properties.put(USER_PROPERTY_SYSTEM_BUILD_ID, Build.ID);
            properties.put(USER_PROPERTY_SYSTEM_USER_AGENT_WEBVIEW, getUserAgent(context));
        } catch (Exception e) {
        }
        return properties;
    }


    /**
     * 获取userAgent
     */
    private static String getUserAgent(Context context) {
        String ua = "";
        try {
            ua = WebSettings.getDefaultUserAgent(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ua;
    }


    private static String getSensor(Context context) {
        try {
            SensorManager sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
            List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
            JSONArray jsonArray = new JSONArray();
            for (Sensor sensor : sensors) {
                int sT = sensor.getType();
                String sN = sensor.getName();
                String sV = sensor.getVendor();
                if (sT == 1 || sT == 2 || sT == 4) {
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("sensor_type", sT);
                    jsonObject1.put("sensor_name", sN);
                    jsonObject1.put("sensor_vendor", sV);
                    jsonArray.put(jsonObject1);
                }
            }
            JSONObject jsonobject = new JSONObject();
            jsonobject.put("sensors", jsonArray);
            return jsonobject.toString();
        } catch (Exception e) {
            return "";
        }

    }

    private static String dataFolderUsed() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long availableBlocksLong = statFs.getAvailableBlocksLong() * blockSizeLong;
        long blockCountLong = statFs.getBlockCountLong() * blockSizeLong;
        double pow = Math.pow(2.0d, 20.0d);
        double d = (double) availableBlocksLong;
        Double.isNaN(d);
        long j = (long) (d / pow);
        double d2 = (double) blockCountLong;
        Double.isNaN(d2);
        return new StringBuilder().append(j).append("/").append((long) (d2 / pow)).toString();
    }

    @SuppressLint("PrivateApi")
    private static String getProperty(String propName) {
        Object roSecureObj;
        try {
            roSecureObj = Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class)
                    .invoke(null, propName);
            if (roSecureObj != null) {
                return (String) roSecureObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取 dpi
     *
     * @param context
     */
    private static int getDensityDpi(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.densityDpi;
    }

    /**
     * 获取屏幕高度，单位 px
     *
     * @param context
     * @return
     */
    private static int getScreenHeight(Context context) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        return display.heightPixels;
    }

    /**
     * 获取屏幕宽度，单位 px
     *
     * @param context
     * @return
     */
    private static int getScreenWidth(Context context) {
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        return display.widthPixels;
    }

    /**
     * 获取屏幕高度，单位 dp
     *
     * @param context
     * @return
     */
    private static int getScreenHeightWithDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        return (int) (displayMetrics.heightPixels / density + 0.5f);
    }


    /**
     * 获取屏幕宽度，单位 dp
     *
     * @param context
     * @return
     */
    private static int getScreenWidthWithDp(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        return (int) (displayMetrics.widthPixels / density + 0.5f);
    }

    /**
     * 获取屏幕大小
     *
     * @param context
     * @return
     */
    private static int getScreenSize(Context context) {
        return context.getResources().getConfiguration().screenLayout & 0xf;
    }


    private static int getAppVersionCode(Context context, String pkg) {
        int vc = 1;
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(pkg, 0);
            vc = info.versionCode;
        } catch (Exception e) {

        }
        return vc;
    }

    private static String getAppVersionName(Context context, String pkg) {
        String vn = "";
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(pkg, 0);
            vn = info.versionName;
        } catch (Exception e) {

        }
        return vn;
    }

    /**
     * 桌面应用
     *
     * @param context
     * @return
     */
    private static String getLauncherPackageName(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res == null || res.activityInfo == null) return "unknown";
        return res.activityInfo.packageName;
    }

    public static float dp2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (dp * scale + 0.1f);
    }
}
