package com.ironmeta.one

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
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ironmeta.base.vstore.VstoreManager
import com.ironmeta.one.base.net.NetworkManager
import com.ironmeta.one.base.utils.BuildConfigUtils
import com.ironmeta.one.base.utils.LogUtils
import com.ironmeta.one.base.utils.ProcessUtils
import com.ironmeta.one.base.utils.ThreadUtils
import com.ironmeta.one.comboads.network.UserProfileRetrofit
import com.ironmeta.one.config.RemoteConfigManager
import com.ironmeta.one.constants.KvStoreConstants
import com.ironmeta.one.constants.RemoteConstants
import com.ironmeta.one.coreservice.CoreSDKResponseManager
import com.ironmeta.one.coreservice.CoreServiceManager
import com.ironmeta.one.notification.ConnectionInfoNotification
import com.ironmeta.one.notification.NotificationConstants
import com.ironmeta.one.region.RegionConstants.KEY_CONNECTED_VPN_IP
import com.ironmeta.one.ui.support.SupportUtils
import com.ironmeta.one.utils.SystemPropertyUtils
import com.ironmeta.one.vlog.VlogManager
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager
import ai.datatower.analytics.DT
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.OnDataTowerIdListener
import com.ironmeta.one.app.AppSignature
import com.sdk.ssmod.IIMSDKApplication
import com.sdk.ssmod.IMSDK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    override val certBytes: ByteArray
        get() = AppSignature.signatureByteArray()
    override val notification: IIMSDKApplication.CustomNotification get() = ConnectionInfoNotification.getInstance(this@MainApplication)
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
                VstoreManager.getInstance(instance).encode(false, KEY_CONNECTED_VPN_IP, "")
            }
        }
    }

    private fun initAdjust() {
        DTAnalytics.getDataTowerId(object : OnDataTowerIdListener {
            override fun onDataTowerIdCompleted(dataTowerId: String) {
                Adjust.addSessionCallbackParameter("dt_id", dataTowerId)
                val appToken = "genwl7nmrbb4"
                val environment: String =
                    if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
                val config = AdjustConfig(this@MainApplication, appToken, environment)
                Adjust.onCreate(config)
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
        initAdjust()
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
                    UserProfileRetrofit.getInstance().reportBeat(
                        context, object : Callback<Any?> {
                            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {}
                            override fun onFailure(call: Call<Any?>, t: Throwable) {}
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
            "dt_d9d8cbb0dc11ad36",
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