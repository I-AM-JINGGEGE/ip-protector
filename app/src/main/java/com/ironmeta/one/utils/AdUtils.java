package com.ironmeta.one.utils;

import android.app.Activity;

import com.ironmeta.one.MainApplication;
import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;

/**
 * @author Tom J
 * @package com.ironmeta.one.utils
 * @description <p></p>
 * @date 2021/10/22 2:14 下午
 */
public class AdUtils {

    /**
     * <p>判断是否需要展示开屏广告</p>
     *
     * @param activity activity
     * @return true，需要展示，false 不需要展示
     */
    public static boolean allowOpenAdToShow(Activity activity) {
        if (TahitiCoreServiceStateInfoManager.getInstance(activity).getCoreServiceConnected()) {
            return false;
        } else {
            if (activity.getApplication() instanceof MainApplication) {
                MainApplication application = (MainApplication) activity.getApplication();
                return application.isCold();
            } else {
                return false;
            }
        }
    }

}
