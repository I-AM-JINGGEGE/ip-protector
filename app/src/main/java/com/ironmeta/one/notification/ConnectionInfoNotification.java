package com.ironmeta.one.notification;

import static com.ironmeta.one.notification.NotificationConstants.NOTIFICATION_ID_NETWORK_CONNECTED;
import static com.ironmeta.one.notification.NotificationConstants.NOTIFICATION_ID_VPN_INFO;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.format.Formatter;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.ironmeta.one.MainApplication;
import com.ironmeta.one.NotificationLauncherActivity;
import com.ironmeta.one.R;
import com.ironmeta.one.base.utils.LogUtils;
import com.ironmeta.one.coreservice.FakeConnectingProgressManager;
import com.ironmeta.one.region.RegionUtils;
import com.ironmeta.one.report.ReportConstants;
import com.ironmeta.one.utils.TimeUtils;
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;
import com.ironmeta.tahiti.constants.ActionActivityConstants;
import com.ironmeta.tahiti.constants.CoreServiceStateConstants;
import com.sdk.ssmod.IIMSDKApplication;
import com.sdk.ssmod.IMSDK;
import com.sdk.ssmod.beans.TrafficStats;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionInfoNotification implements IIMSDKApplication.CustomNotification {
    private static final String TAG = ConnectionInfoNotification.class.getSimpleName();

    private NotificationCompat.Builder mBuilder;
    private Context context;

    private LiveData<IMSDK.VpnState> mCoreServiceState;
    private LiveData<TrafficStats> mTrafficStatsLiveData;
    private TrafficStats mTrafficStats;
    private long mRemainingSeconds;
    private Timer mCountdownTimer;
    private MutableLiveData<Long> mRemainingSecondsLiveData = new MutableLiveData<>();
    private Observer<IMSDK.VpnState> mCoreServiceStateObserver = coreServiceState -> {
        if (coreServiceState == null) {
            return;
        }
        updateNotification(coreServiceState);

        if (CoreServiceStateConstants.isConnected(coreServiceState)) {
            startTimer();
            cancelConnectGuideNotification(context.getApplicationContext());
        } else {
            stopTimer();
            if (CoreServiceStateConstants.isDisconnected(coreServiceState)) {
                showConnectGuideNotification(context);
            }
        }
    };

    private void updateNotification(IMSDK.VpnState vpnState) {
        mBuilder.setWhen(System.currentTimeMillis());
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        if (CoreServiceStateConstants.isDisconnected(vpnState)) {
            notificationLayout.setTextViewText(R.id.title, context.getString(R.string.vs_core_service_notification_title_ready));
            notificationLayout.setViewVisibility(R.id.button, View.VISIBLE);
            notificationLayout.setTextViewText(R.id.button, context.getString(R.string.vs_common_button_connect));
            notificationLayout.setOnClickPendingIntent(R.id.button, createPendingIntentConnect());
            notificationLayout.setOnClickPendingIntent(R.id.parent, createPendingIntentConnect());
            notificationLayout.setViewVisibility(R.id.content_1, View.VISIBLE);
            notificationLayout.setTextViewText(R.id.content_1, context.getString(R.string.notification_app_description));
        } else if (CoreServiceStateConstants.isStarted(vpnState) ||
                CoreServiceStateConstants.isTesting(vpnState) ||
                CoreServiceStateConstants.isConnecting(vpnState) ||
                FakeConnectingProgressManager.Companion.getInstance().isWaitingForConnecting() ||
                FakeConnectingProgressManager.Companion.getInstance().isProgressingAfterConnected()) {
            notificationLayout.setTextViewText(R.id.title, context.getString(R.string.vs_core_service_state_testing) + " " + vpnState + "%");
        } else if (CoreServiceStateConstants.isConnected(vpnState)) {
            notificationLayout.setTextViewText(R.id.title, context.getString(R.string.vs_core_service_state_connected));
            // add time button
            notificationLayout.setViewVisibility(R.id.button, View.VISIBLE);
            notificationLayout.setTextViewText(R.id.button, context.getString(R.string.add_time));
            notificationLayout.setOnClickPendingIntent(R.id.button, createPendingIntentAddTime());
            //country info
            IMSDK.WithResponseBuilder.ConnectedTo vpnServer = TahitiCoreServiceStateInfoManager.getInstance(context).getCoreServiceConnectedServerAsLiveData().getValue();
            if (vpnServer != null) {
                notificationLayout.setViewVisibility(R.id.contentImage, View.VISIBLE);
                notificationLayout.setViewVisibility(R.id.content, View.VISIBLE);
                notificationLayout.setImageViewResource(R.id.contentImage, RegionUtils.getRegionFlagImageResource(context, vpnServer.getCountry()));
                notificationLayout.setTextViewText(R.id.content, RegionUtils.getRegionName(context, vpnServer.getZoneId()));
            }
            //time count down
            long connectedSeconds = mRemainingSeconds;
            if (connectedSeconds > 0) {
                notificationLayout.setViewVisibility(R.id.time, View.VISIBLE);
                long hour = 0;
                long minute = (connectedSeconds / 60);
                long second;
                if (minute < 60) {
                    second = (connectedSeconds % 60);
                } else {
                    hour = minute / 60;
                    minute %= 60;
                    second = (connectedSeconds - hour * 3600 - minute * 60);
                }
                notificationLayout.setTextViewText(R.id.time, TimeUtils.INSTANCE.leastTwoDigitsFormat((int) hour) + ":" + TimeUtils.INSTANCE.leastTwoDigitsFormat((int) minute) + ":" + TimeUtils.INSTANCE.leastTwoDigitsFormat((int) second));
                notificationLayout.setOnClickPendingIntent(R.id.time, createPendingIntentAddTime());
            }
            //traffic
            if (mTrafficStats != null) {
                notificationLayout.setViewVisibility(R.id.content_1, View.VISIBLE);
                notificationLayout.setTextViewText(R.id.content_1, context.getString(R.string.vs_core_service_notification_traffic,
                        context.getString(R.string.vs_core_service_notification_speed, Formatter.formatFileSize(context, mTrafficStats.getTxRate())),
                        context.getString(R.string.vs_core_service_notification_speed, Formatter.formatFileSize(context, mTrafficStats.getRxRate()))));
                mBuilder.setSubText(
                        context.getString(R.string.vs_core_service_notification_traffic,
                                Formatter.formatFileSize(context, mTrafficStats.getTxTotal()),
                                Formatter.formatFileSize(context, mTrafficStats.getRxTotal())));
            }
        } else if (CoreServiceStateConstants.isDisconnecting(vpnState)) {
            notificationLayout.setTextViewText(R.id.title, context.getString(R.string.vs_core_service_state_disconnecting));
            notificationLayout.setOnClickPendingIntent(R.id.parent, createPendingIntentIdle());
        } else {
            notificationLayout.setTextViewText(R.id.title, context.getString(R.string.vs_core_service_notification_title_ready));
            notificationLayout.setViewVisibility(R.id.content_1, View.VISIBLE);
            notificationLayout.setTextViewText(R.id.content_1, context.getString(R.string.notification_app_description));
            notificationLayout.setOnClickPendingIntent(R.id.parent, createPendingIntentIdle());
        }
        mBuilder.setCustomContentView(notificationLayout);
        show();
    }

    private PendingIntent createPendingIntentConnect() {
        Intent intent = new Intent(context, ((MainApplication) context).getNotificationActivityClass())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ActionActivityConstants.KEY_EXTRA_ACTION, ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_TO_CONNECT);
        return PendingIntent.getActivity(context, ActionActivityConstants.PENDING_INTENT_REQUEST_CODE_CONNECTION_INFO_NOTIFICATION, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createPendingIntentAddTime() {
        Intent intent = new Intent(context, ((MainApplication) context).getNotificationActivityClass())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ActionActivityConstants.KEY_EXTRA_ACTION, ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_ADD_TIME);
        return PendingIntent.getActivity(context, ActionActivityConstants.PENDING_INTENT_REQUEST_CODE_CONNECTION_INFO_NOTIFICATION, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createPendingIntentIdle() {
        return PendingIntent.getActivity(
                context,
                ActionActivityConstants.PENDING_INTENT_REQUEST_CODE_CONNECTION_INFO_NOTIFICATION,
                new Intent(context, ((MainApplication) context).getNotificationActivityClass())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(ActionActivityConstants.KEY_EXTRA_ACTION, ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_IDLE), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Observer<TrafficStats> mTrafficStatsObserver = trafficStats -> {
        mTrafficStats = trafficStats;
    };

    private static ConnectionInfoNotification instance;

    public static ConnectionInfoNotification getInstance(Context context) {
        if (instance == null) {
            synchronized (ConnectionInfoNotification.class) {
                if (instance == null) {
                    instance = new ConnectionInfoNotification(context);
                }
            }
        }
        return instance;
    }

    private ConnectionInfoNotification(Context vpnService) {
        context = vpnService;
        mBuilder = new NotificationCompat.Builder(context, NotificationConstants.NOTIFICATION_CHANNEL_INFO_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        mCoreServiceState = TahitiCoreServiceStateInfoManager.getInstance(null).getCoreServiceStateAsLiveData();
        mTrafficStatsLiveData = TahitiCoreServiceStateInfoManager.getInstance(null).getTrafficStatsAsLiveData();
        mCoreServiceState.observeForever(mCoreServiceStateObserver);
        mTrafficStatsLiveData.observeForever(mTrafficStatsObserver);

        mRemainingSecondsLiveData.observeForever(remainingSecondsObserver);

        show();
    }

    private Observer<Long> remainingSecondsObserver = remainingSeconds -> {
        mRemainingSeconds = remainingSeconds;
        if (mRemainingSeconds > 0) {
            updateNotification(IMSDK.VpnState.Connected);
        }
    };

    private void startTimer() {
        stopTimer();
        mCountdownTimer = new Timer();
        mCountdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long remaining = IMSDK.INSTANCE.getUptimeLimit().getOngoing().getRemaining();
                if (remaining > 0) {
                    mRemainingSecondsLiveData.postValue(remaining / 1000L);
                }
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
            mCountdownTimer = null;
        }
    }

    public void destroy() {
        LogUtils.i(TAG, "destroy");
        mTrafficStatsLiveData.removeObserver(mTrafficStatsObserver);
        mCoreServiceState.removeObserver(mCoreServiceStateObserver);
        mRemainingSecondsLiveData.removeObserver(remainingSecondsObserver);
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.deleteNotificationChannel(NotificationConstants.NOTIFICATION_CHANNEL_INFO_ID);
        } catch (Exception e) {
            LogUtils.logException(e);
        }
        stopTimer();
    }

    public void show() {
        LogUtils.i(TAG, "show");
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(NOTIFICATION_ID_VPN_INFO, mBuilder.build());
        } catch (Exception e) {
            LogUtils.logException(e);
        }
    }

    public static void showConnectGuideNotification(Context context) {
        int random = new Random().nextInt(3);
        String title, content;
        switch (random) {
            case 0:
                title = context.getString(R.string.notification_network_title_1);
                content = context.getString(R.string.notification_network_content_1);
                break;
            case 1:
                title = context.getString(R.string.notification_network_title_2);
                content = context.getString(R.string.notification_network_content_2);
                break;
            default:
                title = context.getString(R.string.notification_network_title_3);
                content = context.getString(R.string.notification_network_content_3);
                break;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationConstants.NOTIFICATION_CHANNEL_INFO_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createPendingIntentToConnect(context));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID_NETWORK_CONNECTED, builder.build());
    }

    private static PendingIntent createPendingIntentToConnect(Context context) {
        Intent intent = new Intent(context, NotificationLauncherActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ActionActivityConstants.KEY_EXTRA_ACTION, ActionActivityConstants.KEY_EXTRA_ACTION_VALUE_TO_CONNECT)
                .putExtra(ReportConstants.Param.SOURCE, ReportConstants.AppReport.SOURCE_CONNECTION_NOTIFICATION_NETWORK);
        return PendingIntent.getActivity(context, ActionActivityConstants.PENDING_INTENT_REQUEST_CODE_NOTIFICATION_NETWORK, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void cancelConnectGuideNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID_NETWORK_CONNECTED);
    }

    @Nullable
    @Override
    public NotificationCompat.Builder getBuilder() {
        return null;
    }

    @Override
    public boolean getHasToDismissAfterDisconnection() {
        return true;
    }

    @Nullable
    @Override
    public Integer getIconId() {
        return R.mipmap.ic_notification;
    }

    @Override
    public boolean isLiveSpeedEnabled() {
        return false;
    }

    @Nullable
    @Override
    public Integer getNotificationId() {
        return NotificationConstants.NOTIFICATION_ID;
    }

    @Override
    public void onProfileChange(@Nullable String s) {

    }

    @Override
    public void onTrafficStatsUpdate(long l, @NonNull TrafficStats trafficStats) {

    }

    @Override
    public void onTrafficStatsUpdate(long l, @NonNull String s, @NonNull String s1) {

    }
}
