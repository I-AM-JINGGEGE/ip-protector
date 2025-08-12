package com.vpn.android.ui.support;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewException;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;
import com.vpn.base.vstore.VstoreManager;
import com.vpn.android.R;
import com.vpn.android.base.utils.LogUtils;
import com.vpn.android.base.utils.OSUtils;
import com.vpn.android.constants.KvStoreConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SupportUtils {
    private static final String TAG = SupportUtils.class.getSimpleName();

    public static String getGPLink(Context context) {
        return "https://play.google.com/store/apps/details?id=" + context.getPackageName();
    }

    public static void setPrivacyPolicyConfirmed(@NonNull Context context) {
        VstoreManager.getInstance(context).encode(true, KvStoreConstants.KEY_SUPPORT_PRIVACY_POLICY_CONFIRMED, true);
    }

    private static boolean getPrivacyPolicyConfirmed(@NonNull Context context) {
        return VstoreManager.getInstance(context).decode(true, KvStoreConstants.KEY_SUPPORT_PRIVACY_POLICY_CONFIRMED, false);
    }
    /** privacy policy confirm end **/

    /**
     * rating begin
     **/
    public static boolean checkToShowRating(@NonNull Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            LogUtils.i(TAG, "checkToShowRating@activity isFinishing");
            return false;
        }
        Context appContext = activity.getApplicationContext();

        if (isMarkedDoNotShowRateDialog(appContext)) {
            return false;
        }
        int appColdStartCount = getAppColdStartCount(appContext);
        if (appColdStartCount < 2) {
            return false;
        }
        //show once everyday
        boolean shownToday = ratingShownToday(appContext);
        if (shownToday) {
            return false;
        }

        try {
            ReviewManager manager = ReviewManagerFactory.create(appContext);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                    flow.addOnCompleteListener(task1 -> {
                    });
                } else {
                    // There was some problem, log or handle the error code.
                    @ReviewErrorCode int reviewErrorCode = ((ReviewException) task.getException()).getErrorCode();
                }
            });
            setRatingShownDay(appContext);
        } catch (Exception e) {
            LogUtils.logException(new RuntimeException("Exception when rating dialog show.", e));
        }
        return true;
    }

    public static void logAppColdStart(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        int count = getAppColdStartCount(context);
        VstoreManager.getInstance(appContext).encode(true, KvStoreConstants.KEY_APP_COLD_START_COUNT, count + 1);
    }

    private static int getAppColdStartCount(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        return VstoreManager.getInstance(appContext).decode(true, KvStoreConstants.KEY_APP_COLD_START_COUNT, 0);
    }

    public static void markDoNotShowRateDialog(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        VstoreManager.getInstance(appContext).encode(true, KvStoreConstants.KEY_RATING_MARK_DO_NOT_SHOW_AGAIN, true);
    }

    private static boolean isMarkedDoNotShowRateDialog(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        return VstoreManager.getInstance(appContext).decode(true, KvStoreConstants.KEY_RATING_MARK_DO_NOT_SHOW_AGAIN, false);
    }

    private static void setRatingShownDay(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        VstoreManager.getInstance(context.getApplicationContext()).encode(true, KvStoreConstants.KEY_RATING_LAST_SHOWING_DAY, today);
    }

    private static boolean ratingShownToday(Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        String lastShownDay = VstoreManager.getInstance(context.getApplicationContext()).decode(true, KvStoreConstants.KEY_RATING_LAST_SHOWING_DAY, "");
        return TextUtils.equals(today, lastShownDay);
    }
    /** rating end **/

    /**
     * feedback begin
     **/
    public static void sendFeedback(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        OSUtils.sendEmail(appContext, "one.support@ironmeta.com", context.getResources().getString(R.string.vs_rating_feedback_send));
    }
    /** feedback end **/
}