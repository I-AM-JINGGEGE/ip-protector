package com.vpn.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.webkit.WebView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.vpn.base.vstore.VstoreManager
import com.vpn.android.base.net.NetworkManager
import com.vpn.android.base.utils.BuildConfigUtils
import com.vpn.android.base.utils.LogUtils
import com.vpn.android.base.utils.ProcessUtils
import com.vpn.android.base.utils.ThreadUtils
import com.vpn.android.comboads.network.UserProfileRetrofit
import com.vpn.android.config.RemoteConfigManager
import com.vpn.android.constants.KvStoreConstants
import com.vpn.android.constants.RemoteConstants
import com.vpn.android.coreservice.CoreSDKResponseManager
import com.vpn.android.coreservice.CoreServiceManager
import com.vpn.android.notification.ConnectionInfoNotification
import com.vpn.android.notification.NotificationConstants
import com.vpn.android.ui.support.SupportUtils
import com.vpn.android.utils.SystemPropertyUtils
import com.vpn.android.vlog.VlogManager
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager
import ai.datatower.analytics.DT
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.OnDataTowerIdListener
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerLib
import com.vpn.android.region.RegionConstants.KEY_PROFILE_VPN_IP
import com.sdk.ssmod.IIMSDKApplication
import com.sdk.ssmod.IMSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class MainApplication : Application(), IIMSDKApplication {
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
    override val app: Application = this
    override val configureClass: KClass<out Any> = MainActivity::class
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val notification: IIMSDKApplication.CustomNotification get() = ConnectionInfoNotification.getInstance(this@MainApplication)
    override fun onServiceDisconnected() {

    }

    override fun onBinderDied() {

    }

    var isCold = false
    override fun onCreate() {
        super.onCreate()
        instance = this
        context = this.applicationContext
        isCold = true
        initApp()
    }

    val appName: String
        get() = getString(R.string.app_name)
    val actionActivityClass: Class<*>
        get() = MainActivity::class.java
    val notificationActivityClass: Class<*>
        get() = NotificationLauncherActivity::class.java
    val cnl: String
        get() = BuildConfig.CNL
    val publishSeconds: Int
        get() = Integer.valueOf(BuildConfig.PUBLISH_SENCONDS)
    val debug: Boolean
        get() = BuildConfig.DEBUG

    /** app begin  */
    private fun initApp() {
        allProcessesOnCreate()
        if (ProcessUtils.isInMainProcess(this)) {
            mainProcessOnCreate()
        } else if (ProcessUtils.isInServiceProcess(this)) {
            serviceProcessOnCreate()
        }
    }

    private fun allProcessesOnCreate() {
        IMSDK.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val name = ProcessUtils.getProcessName(this)
            if (name != null) {
                WebView.setDataDirectorySuffix(name)
            }
        }
        try {
            WorkManager.initialize(this, Configuration.Builder().build())
        } catch (ignore: Exception) {
        }
        //ROIQuery
        initROIQuery()
        // log
        FirebaseApp.initializeApp(this)

        // analytics
        VlogManager.getInstance(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        initNotification()
        RemoteConfigManager.getInstance()
        IMSDK.vpnState.observeForever {
            if (it == IMSDK.VpnState.Stopped || it == IMSDK.VpnState.Idle) {
                VstoreManager.getInstance(instance).encode(false, KEY_PROFILE_VPN_IP, "")
            }
        }
    }

    private fun initAttribution() {
        DTAnalytics.getDataTowerId(object : OnDataTowerIdListener {
            override fun onDataTowerIdCompleted(dataTowerId: String) {
                AppsFlyerLib.getInstance().setLogLevel(if (BuildConfig.DEBUG) AFLogger.LogLevel.INFO else AFLogger.LogLevel.WARNING)
                AppsFlyerLib.getInstance().init("QEMmCYxhFSbGVipALHZWLH", null, this@MainApplication)
                AppsFlyerLib.getInstance().start(this@MainApplication)
                val appsFlyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(this@MainApplication)
                DTAnalytics.setAppsFlyerId(appsFlyerUID)
            }
        })
    }

    private fun mainProcessOnCreate() {
        val installationTime = VstoreManager.getInstance(this)
            .decode(true, KvStoreConstants.KEY_APP_INSTALLATION_TIME, 0L)
        if (installationTime <= 0L) {
            VstoreManager.getInstance(this).encode(
                true,
                KvStoreConstants.KEY_APP_INSTALLATION_TIME,
                System.currentTimeMillis()
            )
        }
        //系统级别的属性，业务侧调用
        SystemPropertyUtils.track(this)

        //ads
        initAd()

        SupportUtils.logAppColdStart(this)
        initAttribution()
        ProcessLifecycleOwner.get().lifecycle.addObserver(mLifecycleObserver)
        CoreSDKResponseManager.initNetworkObserver()
    }

    private val mLifecycleObserver: LifecycleObserver =
        LifecycleEventObserver { source: LifecycleOwner?, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_START) {
                if (!TahitiCoreServiceStateInfoManager.getInstance(this).coreServiceConnected
                ) {
                    // show notification when disconnected
                    try {
                        CoreServiceManager.getInstance(this).disconnect()
                        removeObserver()
                    } catch (e: Exception) {
                        LogUtils.logException(
                            IllegalStateException(
                                "Exception when try to show notification",
                                e
                            )
                        )
                    }
                }
            }
        }

    private fun removeObserver() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(mLifecycleObserver)
    }

    private fun serviceProcessOnCreate() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                ThreadUtils.runOnMainThread {
                    UserProfileRetrofit.instance.reportBeat(
                        context, object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                        })
                }
            }
        }, 1000, RemoteConstants.REPORT_BEAT_DURATION_VALUE_DEFAULT)
        NetworkManager.getInstance(this).connectedAsLiveData
            .observeForever { connected: Boolean? ->
                if (connected != true || TahitiCoreServiceStateInfoManager.getInstance(this)
                        .coreServiceConnected
                ) {
                    return@observeForever
                }
                ConnectionInfoNotification.showConnectGuideNotification(this)
            }
    }

    private fun initNotification() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val nm = getSystemService(NotificationManager::class.java)
            ?: return
        val ncs: MutableList<NotificationChannel> = ArrayList<NotificationChannel>()
        ncs.add(
            NotificationChannel(
                NotificationConstants.NOTIFICATION_CHANNEL_INFO_ID,
                NotificationConstants.NOTIFICATION_CHANNEL_INFO_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
        )
        nm.createNotificationChannels(ncs)
    }

    private fun initAd() {
        MobileAds.initialize(this)
    }

    private fun initROIQuery() {
        DT.initSDK(
            this,
            "dt_526bf4b6e996cac1",
            "https://report.roiquery.com",
            BuildConfigUtils.getCnl(this),
            debug
        )
    }

    /** app end  */
    companion object {
        lateinit var context: Context
            private set
        lateinit var instance: MainApplication
            private set
    }
}