package com.vpn.android;

import static com.vpn.android.constants.RemoteConstants.ADD_TIME_MAX_DURATION_VALUE_DEFAULT;
import static com.vpn.android.constants.RemoteConstants.COLD_START_MAX_DURATION_VALUE_DEFAULT;
import static com.vpn.android.report.ReportConstants.AppReport.SOURCE_ADD_TIME_MAIN_PAGE_2;
import static com.vpn.tahiti.constants.ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_ADD_TIME;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.ViewTreeObserver;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.vpn.android.base.net.NetworkManager;
import com.vpn.android.base.utils.LogUtils;
import com.vpn.android.base.utils.ThreadUtils;
import com.vpn.android.base.utils.ToastUtils;
import com.vpn.android.ads.AdPresenterWrapper;
import com.vpn.android.ads.constant.AdConstant;
import com.vpn.android.ads.constant.AdFormat;
import com.vpn.android.ads.proxy.AdLoadListener;
import com.vpn.android.ads.proxy.AdShowListener;
import com.vpn.android.comboads.network.UserProfileRetrofit;
import com.vpn.android.coreservice.CoreServiceManager;
import com.vpn.android.coreservice.ProgressListener;
import com.vpn.android.coreservice.FakeConnectingProgressManager;
import com.vpn.android.report.AppReport;
import com.vpn.android.report.ReportConstants;
import com.vpn.android.report.VpnReporter;
import com.vpn.android.ui.ConnectedReportActivity;
import com.vpn.android.ui.SplashActivity;
import com.vpn.android.ui.common.CommonAppCompatActivity;
import com.vpn.android.ui.common.CommonDialog;
import com.vpn.android.ui.dialog.CongratulationsDialog;
import com.vpn.android.ui.dialog.ExitAppDialog;
import com.vpn.android.ui.dialog.OpenNetworkSettingsDialog;
import com.vpn.android.ui.helper.NavHelper;
import com.vpn.android.ui.home.ConnectedFragment;
import com.vpn.android.ui.home.DisconnectFragment;
import com.vpn.android.ui.home.HomeViewModel;
import com.vpn.android.ui.home.OnAddTimeClickListener;
import com.vpn.android.ui.home.OnClickDisconnectListener;
import com.vpn.android.ui.home.OnConnectedReportClickListener;
import com.vpn.android.ui.home.OnReconnectClickListener;
import com.vpn.android.ui.home.OnRemainTimeZeroListener;
import com.vpn.android.ui.home.OnSlideClickListener;
import com.vpn.android.ui.regionselector.card.ConnectedViewModel;
import com.vpn.android.ui.regionselector2.ServerListViewModel;
import com.vpn.android.ui.splash.AddTimeLoadingFragment;
import com.vpn.android.ui.support.SupportUtils;
import com.vpn.android.utils.FragmentUtils;
import com.vpn.tahiti.TahitiCoreServiceStateInfoManager;
import com.vpn.tahiti.constants.ActionActivityConstants;
import com.vpn.tahiti.constants.CoreServiceStateConstants;
import com.sdk.ssmod.IMSDK;
import com.sdk.ssmod.api.http.beans.FetchResponse;
import com.sdk.ssmod.beans.TrafficStats;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends CommonAppCompatActivity implements OnClickDisconnectListener,
        OnReconnectClickListener,
        OnAddTimeClickListener,
        OnRemainTimeZeroListener,
        OnSlideClickListener,
        OnConnectedReportClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_UPDATE_LANGUAGE_FROM_RESTART = "extra_update_language_from_restart";

    private Class<? extends Fragment> currentFragmentClass;
    private HomeViewModel mHomeViewModel;

    private ConnectedViewModel mConnectedViewModel;
    private boolean isCreate = true;
    private ActivityResultLauncher<Intent> splashActivityResultLauncher;

    private ActivityResultLauncher activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            // disable disconnected interstitial ads
            if (!TahitiCoreServiceStateInfoManager.getInstance(MainApplication.Companion.getContext()).getCoreServiceConnected()) {
                return;
            }
            String adPlacement = AdConstant.AdPlacement.I_BACK_HOME_CONNECTED;
            AdPresenterWrapper.Companion.getInstance().logToShow(AdFormat.INTERSTITIAL, adPlacement);
            mHomeViewModel.startShowLoadingAd();
            Runnable runnable = () -> {
                if (mHomeViewModel.getShowAddTimeLoadingAsLiveData().getValue() == true) {
                    mHomeViewModel.stopShowLoadingAd();
                }
            };
            ThreadUtils.delayRunOnMainThread(runnable, ADD_TIME_MAX_DURATION_VALUE_DEFAULT);
            AdPresenterWrapper.Companion.getInstance().loadAdExceptNative(AdFormat.INTERSTITIAL, adPlacement, new AdLoadListener() {
                @Override
                public void onAdLoaded() {
                    if (mHomeViewModel.getShowAddTimeLoadingAsLiveData().getValue() == true) {
                        ThreadUtils.removeCallback(runnable);
                        mHomeViewModel.stopShowLoadingAd();
                    }
                    AdPresenterWrapper.Companion.getInstance().showAdExceptNative(MainActivity.this, AdFormat.INTERSTITIAL, adPlacement, null);
                }

                @Override
                public void onFailure(int errorCode, @NonNull String errorMessage) {
                    if (mHomeViewModel.getShowAddTimeLoadingAsLiveData().getValue() == true) {
                        ThreadUtils.removeCallback(runnable);
                        mHomeViewModel.stopShowLoadingAd();
                    }
                }
            }, "second page back");
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 设置沉浸式状态栏
        setTransparentStatusBar();
        
        registerActivityForResult();
        initNavSlidePage();
        initView();
        initViewModel();
        performViewModel();
        handleIntent(true);
        updateLanguage();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mLifecycleObserver);
        AdPresenterWrapper.Companion.getInstance().init(() -> {
            String adPlacementI = TahitiCoreServiceStateInfoManager.getInstance(getApplicationContext()).getCoreServiceConnected() ? AdConstant.AdPlacement.I_APP_START_CONNECT : AdConstant.AdPlacement.I_APP_START_DISCONNECT;
            String adPlacementN = TahitiCoreServiceStateInfoManager.getInstance(getApplicationContext()).getCoreServiceConnected() ? AdConstant.AdPlacement.N_CONNECTED : AdConstant.AdPlacement.N_DISCONNECT;
            // disable disconnected interstitial ads
            if (TahitiCoreServiceStateInfoManager.getInstance(getApplicationContext()).getCoreServiceConnected()) {
                AdPresenterWrapper.Companion.getInstance().loadAdExceptNative(AdFormat.INTERSTITIAL, adPlacementI, null, "after presenter init");
                AdPresenterWrapper.Companion.getInstance().loadNativeAd(adPlacementN, null, "after presenter init");
            }
        });
        mConnectedViewModel =
                new ViewModelProvider(this).get(ConnectedViewModel.class);
        mConnectedViewModel.getUsedUpRemainSecondsAsLiveData().observe(this, connectedSeconds -> {
            if (connectedSeconds == null) {
                return;
            }
            if (connectedSeconds <= 0) {
                onRemainTimeZero();
            }
        });
        GDPR_CMP();
    }

    private void GDPR_CMP() {
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .build();
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () ->
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(MainActivity.this, formError -> {}),
                formError -> {});
    }

    private boolean launchFromNotification = false;

    private LifecycleObserver mLifecycleObserver = (LifecycleEventObserver) (source, event) -> {
        if (event == Lifecycle.Event.ON_START) {
            AdPresenterWrapper.Companion.getInstance().setAppForeground(true);
            ThreadUtils.delayRunOnMainThread(() -> {
                if (!AdPresenterWrapper.Companion.getInstance().isFullScreenAdShown() && !launchFromNotification) {
                    showSplashActivity();
                }
                isCreate = false;
                launchFromNotification = false;
            }, 150);
        } else if (event == Lifecycle.Event.ON_STOP) {
            launchFromNotification = false;
            AdPresenterWrapper.Companion.getInstance().setAppForeground(false);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FakeConnectingProgressManager.Companion.getInstance().destroy();
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(mLifecycleObserver);
    }

    private void registerActivityForResult() {
        splashActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        });
    }

    private void updateLanguage() {
        if (getIntent().getBooleanExtra(EXTRA_UPDATE_LANGUAGE_FROM_RESTART, false)) {
            ServerListViewModel model = new ViewModelProvider(this).get(ServerListViewModel.class);
            model.refreshServersLanguage(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
        handleIntent(false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void handleIntent(boolean onCreate) {
        Intent intent = getIntent();
        int action = intent == null ? -1 : intent.getIntExtra(ActionActivityConstants.KEY_EXTRA_ACTION, -1);
        if (action == ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_TO_CONNECT) {
            String source = intent.getStringExtra(ReportConstants.Param.SOURCE);
            AppReport.setConnectionSource(TextUtils.isEmpty(source) ? ReportConstants.AppReport.SOURCE_CONNECTION_NOTIFICATION : source);
            AppReport.reportToConnection();
            connect();
        } else if (action == ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_CORE_SERVICE_DISCONNECTED) {
        } else if (action == KEY_EXTRA_ACTION_VALUE_ADD_TIME) {
            launchFromNotification = true;
            onAddOneHourClick(ReportConstants.AppReport.SOURCE_ADD_TIME_NOTIFICATION);
        }
    }

    private void connect() {
        new Handler().postDelayed(() -> {
            DisconnectFragment fragment = (DisconnectFragment) getSupportFragmentManager().findFragmentById(R.id.content_fragment_container);
            if (fragment != null && fragment.isAdded()) {
                fragment.connect(VpnReporter.PARAM_VALUE_FROM_NOTIFICATION);
            }
        }, 1000);
    }

    private void showSplashActivity() {
        // disable disconnected interstitial ads
        boolean vpnConnected = TahitiCoreServiceStateInfoManager.getInstance(MainApplication.Companion.getContext()).getCoreServiceConnected();
        if (!vpnConnected && !isCreate) {
            return;
        }
        Intent intent = new Intent(this, SplashActivity.class);
        String adLocation;
        if (isCreate) {
            adLocation = vpnConnected ?
                    AdConstant.AdPlacement.I_APP_START_CONNECT :
                    AdConstant.AdPlacement.I_APP_START_DISCONNECT;
            if (!vpnConnected) {
                intent.putExtra(SplashActivity.LOADING_TIME_MAX, COLD_START_MAX_DURATION_VALUE_DEFAULT);
            }
        } else {
            adLocation = vpnConnected ?
                    AdConstant.AdPlacement.I_HOME_RESTART_CONNECT :
                    AdConstant.AdPlacement.I_HOME_RESTART_DISCONNECT;
        }
        intent.putExtra(SplashActivity.AD_PLACEMENT, adLocation);
        intent.putExtra(SplashActivity.IS_COLD_START, isCreate);
        if (vpnConnected) {
            AdPresenterWrapper.Companion.getInstance().loadAdExceptNative(AdFormat.INTERSTITIAL, adLocation, null, "cold start");
        }
        splashActivityResultLauncher.launch(intent);
    }

    /**
     * view begin
     **/

    private void initView() {
        boolean connected = TahitiCoreServiceStateInfoManager.getInstance(MainApplication.Companion.getContext()).getCoreServiceConnected();
        if (connected) {
            showConnectedFragment();
        } else {
            showDisconnectFragment(false);
        }
    }
    /** view begin **/

    /**
     * view model begin
     **/
    private MainActivityViewModel mMainActivityViewModel;

    private void initViewModel() {
        mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mMainActivityViewModel.getTrafficStatsAsLiveData().observe(this, trafficStats -> {
            mTrafficStats = trafficStats;
        });
        mHomeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mHomeViewModel.getShowAddTimeLoadingAsLiveData().observe(this, showAddTimeLoadingResult -> {
            if (showAddTimeLoadingResult != null && showAddTimeLoadingResult) {
                showAddTimeLoading();
                return;
            }
            cancelAddTimeLoading();
        });
    }

    private AddTimeLoadingFragment mAddTimeLoadingFragment;

    private void showAddTimeLoading() {
        cancelAddTimeLoading();
        mAddTimeLoadingFragment = new AddTimeLoadingFragment();
        mAddTimeLoadingFragment.show(getSupportFragmentManager(), "");
    }

    private void cancelAddTimeLoading() {
        if (mAddTimeLoadingFragment != null && mAddTimeLoadingFragment.isAdded()) {
            mAddTimeLoadingFragment.dismiss();
            mAddTimeLoadingFragment = null;
        }
    }

    private void performViewModel() {
        TahitiCoreServiceStateInfoManager.getInstance(this).getCoreServiceStateAsLiveData().observe(this, serviceState -> {
            switch (serviceState) {
                case Connecting:
                    if (FakeConnectingProgressManager.Companion.getInstance().isWaitingForConnecting() || FakeConnectingProgressManager.Companion.getInstance().isProgressingAfterConnected()) {
                        return;
                    }
                    FakeConnectingProgressManager.Companion.getInstance().start(new ProgressListener() {
                        @Override
                        public void onStart() {}

                        @Override
                        public void onWaitingForConnecting(float progress) {}

                        @Override
                        public void onProgressAfterConnected(float progress) {}

                        @Override
                        public void onFinish() {
                            AdPresenterWrapper.Companion.getInstance().logToShow(AdFormat.INTERSTITIAL, AdConstant.AdPlacement.I_CONNECTED);
                            if (AdPresenterWrapper.Companion.getInstance().isLoadedExceptNative(AdFormat.INTERSTITIAL, AdConstant.AdPlacement.I_CONNECTED)) {
                                AdPresenterWrapper.Companion.getInstance().showAdExceptNative(MainActivity.this, AdFormat.INTERSTITIAL, AdConstant.AdPlacement.I_CONNECTED, new AdShowListener() {
                                    @Override
                                    public void onAdShown() {}

                                    @Override
                                    public void onAdFailToShow(int errorCode, @NotNull String errorMessage) {
                                        checkToShowNextPageAfterCountingDown();
                                    }

                                    @Override
                                    public void onAdClosed() {
                                        checkToShowNextPageAfterCountingDown();
                                    }

                                    @Override
                                    public void onAdClicked() {}
                                });
                            } else {
                                checkToShowNextPageAfterCountingDown();
                            }
                        }
                    });
                    break;
                case Connected: {
                    UserProfileRetrofit.getInstance().reportBeat(MainApplication.Companion.getContext(), new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {}

                        @Override
                        public void onFailure(Call call, Throwable t) {}
                    });
                    if (FakeConnectingProgressManager.Companion.getInstance().isWaitingForConnecting()) {
                        FakeConnectingProgressManager.Companion.getInstance().notifyVPNConnected();
                    }
                    AdPresenterWrapper.Companion.getInstance().loadAdExceptNative(AdFormat.INTERSTITIAL, AdConstant.AdPlacement.I_CONNECTED, new AdLoadListener() {
                        @Override
                        public void onAdLoaded() {
                            if (FakeConnectingProgressManager.Companion.getInstance().isWaitingForConnecting() || FakeConnectingProgressManager.Companion.getInstance().isProgressingAfterConnected()) {
                                FakeConnectingProgressManager.Companion.getInstance().notifyFinish();
                            }
                        }

                        @Override
                        public void onFailure(int errorCode, @NonNull String errorMessage) {

                        }
                    }, "vpn connected");
                    AdPresenterWrapper.Companion.getInstance().loadNativeAd(AdConstant.AdPlacement.N_CONNECTED, null, "vpn connected");
                    break;
                }
                case Stopped: {
                    checkToShowDisconnectReportActivity();
                    if (FakeConnectingProgressManager.Companion.getInstance().isWaitingForConnecting() || FakeConnectingProgressManager.Companion.getInstance().isProgressingAfterConnected()) {
                        FakeConnectingProgressManager.Companion.getInstance().notifyFinish();
                    }
                    break;
                }
            }
        });
        mMainActivityViewModel.getInLegalRegionAsLiveData().observe(this, inLegalRegionResult -> {
            if (inLegalRegionResult == null || inLegalRegionResult) {
                return;
            }
            showLegalNoticeDialog();
        });
    }

    private void checkToShowNextPageAfterCountingDown() {
        if (TahitiCoreServiceStateInfoManager.getInstance(MainApplication.Companion.getContext()).getCoreServiceConnected()) {
            showConnectedReportActivity();
            showConnectedFragment();
        } else if (currentFragmentClass != DisconnectFragment.class){
            showDisconnectFragment(false);
        }
    }

    private boolean mNeedShowDisconnectReport = false;

    private void checkToShowDisconnectReportActivity() {
        // disable disconnected interstitial ads
        showDisconnectFragment(false);
    }

    private void showDisconnectFragment(boolean autoConnect) {
        Class<? extends Fragment> fragmentClass = DisconnectFragment.class;
        currentFragmentClass = fragmentClass;
        Bundle bundle = new Bundle();
        bundle.putBoolean(DisconnectFragment.EXTRA_AUTO_CONNECT, autoConnect);
        FragmentUtils.INSTANCE.safeCommitReplace(getSupportFragmentManager(), fragmentClass, bundle, R.id.content_fragment_container, R.anim.page_in, R.anim.page_out);
        drawerLayout.setBackgroundResource(R.drawable.bg_common);
    }

    private void showConnectedFragment() {
        Class<? extends Fragment> fragmentClass = ConnectedFragment.class;
        currentFragmentClass = fragmentClass;
        FragmentUtils.INSTANCE.safeCommitReplace(getSupportFragmentManager(), fragmentClass, null, R.id.content_fragment_container, R.anim.page_in, R.anim.page_out);
        drawerLayout.setBackgroundResource(R.drawable.bg_connected);
    }

    public void showConnectedReportActivity() {
        startActivity(new Intent(this, ConnectedReportActivity.class));
        overridePendingTransition(R.anim.page_in, R.anim.page_out);
    }

    /** view model end **/

    @Override
    public void onBackPressed() {
        Context context = this;
        IMSDK.VpnState coreServiceState = TahitiCoreServiceStateInfoManager.getInstance(context).getCoreServiceState();
        if (CoreServiceStateConstants.isConnecting(coreServiceState) ||
                CoreServiceStateConstants.isStarted(coreServiceState) ||
                CoreServiceStateConstants.isTesting(coreServiceState) ||
                FakeConnectingProgressManager.Companion.getInstance().isWaitingForConnecting() ||
                FakeConnectingProgressManager.Companion.getInstance().isProgressingAfterConnected()) {
            ToastUtils.showToast(context, context.getResources().getString(R.string.vs_core_service_state_connecting2));
            return;
        }
        ExitAppDialog dialog = new ExitAppDialog(this);
        dialog.setCancelable(true);
        dialog.setDialogOnClickListener(() -> {
            super.onBackPressed();
        });
        dialog.show();
    }

    private CommonDialog mLegalNoticeDialog;

    private void showLegalNoticeDialog() {
        if (mLegalNoticeDialog != null) {
            mLegalNoticeDialog.cancel();
            mLegalNoticeDialog = null;
        }
        CommonDialog mLegalNoticeDialog = new CommonDialog(this);
        mLegalNoticeDialog.setCancelable(false);
        mLegalNoticeDialog.setOnlyOKButton(true);
        mLegalNoticeDialog.setTitle(getString(R.string.vs_legal_notices_dialog_title));
        mLegalNoticeDialog.setMessage(getString(R.string.vs_legal_notices_dialog_content));
        mLegalNoticeDialog.setOKButton(getString(R.string.vs_common_dialog_ok_button));
        mLegalNoticeDialog.setOkOnclickListener(() -> {
            if (MainActivity.this.isFinishing()) {
                return;
            }
            MainActivity.this.finish();
        });
        mLegalNoticeDialog.show();
    }
    /** support end **/

    private BroadcastReceiver mBroadcastReceiver = null;

    private void clearUserPresentBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            return;
        }
        unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }

    /**
     * init nag slid page start
     **/
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private void initNavSlidePage() {
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.setStatusBarBackground(R.color.status_bar_bg);
        navigationView = findViewById(R.id.nav_view);
        
        // 移除状态栏高度的边距，实现沉浸式效果
        DrawerLayout.LayoutParams layoutParams = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        navigationView.setLayoutParams(layoutParams);
        
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                navigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                NavHelper.getInstance(getApplicationContext()).initNavigationMenu(MainActivity.this, navigationView);
            }
        };
        navigationView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(true);
            drawerLayout.closeDrawer(navigationView);
            NavHelper.getInstance(this).navToAnother(MainActivity.this, menuItem.getItemId());
            return false;
        });
    }


    @Override
    protected int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private FetchResponse.Host mVpnServer;
    private long mConnectedMilliSeconds;
    private TrafficStats mTrafficStats;

    @Override
    public void onClickDisconnect() {
        mNeedShowDisconnectReport = true;
        try {
            mVpnServer = IMSDK.INSTANCE.getConnectedServer().getValue().getHost();
        } catch (Exception e) {}
        mConnectedMilliSeconds = System.currentTimeMillis() - IMSDK.INSTANCE.getUptimeLimit().getOngoing().getStartedAt();
        CoreServiceManager.getInstance(this).disconnect();
    }

    @Override
    public void onReconnectClick() {
        showDisconnectFragment(true);
    }

    @Override
    public void onAddOneHourClick(@NotNull String source) {
        addTime(AdConstant.AdPlacement.I_ADD_TIME_MAIN_PAGE_1, source, 60);
    }

    private void addTime(String adPlacement, String source, int addedMinutes) {
        AppReport.reportClickAddTime1(source);
        mHomeViewModel.startShowLoadingAd();
        final boolean[] canceled = {false};
        ThreadUtils.delayRunOnMainThread(() -> {
            if (canceled[0] == false && mHomeViewModel.getShowAddTimeLoadingAsLiveData().getValue() == true) {
                mHomeViewModel.stopShowLoadingAd();
                ToastUtils.showToast(MainActivity.this, getResources().getString(R.string.add_time_fail));
            }
        }, ADD_TIME_MAX_DURATION_VALUE_DEFAULT);
        AdPresenterWrapper.Companion.getInstance().loadAdExceptNative(AdFormat.INTERSTITIAL, adPlacement, new AdLoadListener() {
            @Override
            public void onAdLoaded() {
                canceled[0] = true;
                mHomeViewModel.stopShowLoadingAd();
                mHomeViewModel.addTimeA(addedMinutes).observe(MainActivity.this, aBoolean -> {
                    if (aBoolean == false) {
                        return;
                    }
                    AdPresenterWrapper.Companion.getInstance().logToShow(AdFormat.INTERSTITIAL, adPlacement);
                    if (AdPresenterWrapper.Companion.getInstance().isLoadedExceptNative(AdFormat.INTERSTITIAL, adPlacement)) {
                        AdPresenterWrapper.Companion.getInstance().showAdExceptNative(MainActivity.this, AdFormat.INTERSTITIAL, adPlacement, new AdShowListener() {
                            @Override
                            public void onAdShown() {}
                            @Override
                            public void onAdFailToShow(int errorCode, @NotNull String errorMessage) {}
                            @Override
                            public void onAdClosed() {
                                showAddTimeSuccessDialog(addedMinutes);
                            }
                            @Override
                            public void onAdClicked() {}
                        });
                    } else {
                        showAddTimeSuccessDialog(addedMinutes);
                    }
                });
            }

            @Override
            public void onFailure(int errorCode, @NonNull String errorMessage) {
                canceled[0] = true;
                mHomeViewModel.stopShowLoadingAd();
                ToastUtils.showToast(MainActivity.this, getResources().getString(R.string.add_time_fail));
            }
        }, (adPlacement == AdConstant.AdPlacement.I_ADD_TIME_MAIN_PAGE_1) ? "add time 1[main]" : "add time 2[main]");
    }

    @Override
    public void onAddTwoHourClick() {
        addTime(AdConstant.AdPlacement.I_ADD_TIME_MAIN_PAGE_2, SOURCE_ADD_TIME_MAIN_PAGE_2, new Random().nextInt(60) + 60);
    }

    private void showAddTimeSuccessDialog(int addedMinutes) {
        CongratulationsDialog congratulationsDialog = new CongratulationsDialog(this);
        congratulationsDialog.setCancelable(false);
        congratulationsDialog.setDialogOnClickListener(() -> {
            congratulationsDialog.cancel();
            SupportUtils.checkToShowRating(this);
        });
        congratulationsDialog.setMessage(getString(R.string.increased_x_minutes, addedMinutes));
        congratulationsDialog.show();
    }

    @Override
    public void onRemainTimeZero() {
        mNeedShowDisconnectReport = true;
        if (TahitiCoreServiceStateInfoManager.getInstance(this).getCoreServiceConnected()) {
            CoreServiceManager.getInstance(this).disconnect();
        }
    }

    @Override
    public void onSlideClick() {
        drawerLayout.openDrawer(navigationView);
    }

    @Override
    public void onConnectedReportClick() {
        launchActivityForShowingAds(new Intent(this, ConnectedReportActivity.class));
    }

    public void launchActivityForShowingAds(@NonNull Intent intent) {
        activityLauncher.launch(intent);
        overridePendingTransition(R.anim.page_in, R.anim.page_out);
    }

    public void checkShowOpenNetworkDialog() {
        if (!NetworkManager.getInstance(this).getConnected()) {
            showOpenNetworkDialog();
        }
    }

    private boolean mNetworkSettingsDialogShow = false;

    private void showOpenNetworkDialog() {
        if (mNetworkSettingsDialogShow) {
            return;
        }
        OpenNetworkSettingsDialog dialog = new OpenNetworkSettingsDialog(this);
        dialog.setCancelable(false);
        dialog.setDialogOnClickListener(new OpenNetworkSettingsDialog.DialogListener() {
            @Override
            public void onOpenClick() {
                openNetworkSettings();
                AppReport.reportOpenNetworkSettings();
                dialog.dismiss();
            }

            @Override
            public void onCloseClick() {
                AppReport.reportCloseNetworkSettings();
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(dialog1 -> mNetworkSettingsDialogShow = false);
        dialog.show();
        mNetworkSettingsDialogShow = true;
        AppReport.reportNetworkSettingsDialogShow();
    }

    private void openNetworkSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        } catch (Exception e) {
            LogUtils.logException(new Exception("Open system wireless settings error.", e));
        }
    }
}
