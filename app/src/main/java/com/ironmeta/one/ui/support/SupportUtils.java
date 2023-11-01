package com.ironmeta.one.ui.support;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ironmeta.base.vstore.VstoreManager;
import com.ironmeta.one.R;
import com.ironmeta.one.base.utils.AppStoreUtils;
import com.ironmeta.one.base.utils.LogUtils;
import com.ironmeta.one.base.utils.OSUtils;
import com.ironmeta.one.base.utils.ToastUtils;
import com.ironmeta.one.constants.KvStoreConstants;
import com.ironmeta.one.report.AppReport;
import com.ironmeta.one.report.ReportConstants;
import com.ironmeta.one.ui.dialog.RatingDialog;
import com.ironmeta.one.ui.rating.RatingGuideToast;
import com.ironmeta.one.ui.widget.RatingBar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SupportUtils {
    private static final String TAG = SupportUtils.class.getSimpleName();
    public static final String GP_LINK = "https://play.google.com/store/apps/details?id=com.ironmeta.one";

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

        RatingDialog ratingDialog = new RatingDialog(activity);
        ratingDialog.setConfirmBtnClickListener(() -> {
            RatingBar.Type ratingGrade = ratingDialog.getRatingGrade();
            if (ratingGrade == null) {
                ToastUtils.showToast(appContext, appContext.getResources().getString(R.string.vs_rating_select_none_tips));
                return;
            } else if (ratingGrade == RatingBar.Type.TERRIBLE || ratingGrade == RatingBar.Type.BAD || ratingGrade == RatingBar.Type.OK) {
                sendFeedback(appContext);
            } else if (ratingGrade == RatingBar.Type.GREAT || ratingGrade == RatingBar.Type.GOOD) {
                if (AppStoreUtils.openPlayStoreOnlyWithUrl(appContext, GP_LINK)) {
                    RatingGuideToast.go(appContext);
                }
            } else {
                ToastUtils.showToast(appContext, appContext.getResources().getString(R.string.vs_rating_thanks_tips));
            }
            AppReport.reportClickRate(ReportConstants.AppReport.SOURCE_RATE_DIALOG, ratingGrade);
            ratingDialog.dismiss();
        });
        try {
            ratingDialog.show();
            setRatingShownDay(appContext);
        } catch (Exception e) {
            //https://console.firebase.google.com/u/0/project/ironmeta-one/crashlytics/app/android:com.ironmeta.one/issues/6e70bcbdd57fabd84ac11a42e8d2d12a?time=last-seven-days&sessionEventKey=63DC305D01AC000146FEC252E7B46FC1_1774110435651949943
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