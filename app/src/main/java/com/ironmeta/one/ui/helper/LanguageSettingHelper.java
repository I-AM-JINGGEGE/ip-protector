package com.ironmeta.one.ui.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.ironmeta.base.vstore.VstoreManager;
import com.ironmeta.one.MainActivity;
import com.ironmeta.one.R;
import com.ironmeta.one.ui.bean.LanguageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ironmeta.one.MainActivity.EXTRA_UPDATE_LANGUAGE_FROM_RESTART;
import static com.ironmeta.one.constants.LanguageSelectedConstant.KEY_LANGUAGE_ZONE_CODE_SELECTED;

import me.jessyan.autosize.AutoSizeCompat;

public class LanguageSettingHelper {
    @SuppressLint("StaticFieldLeak")
    private static LanguageSettingHelper sLanguageSettingHelper = null;
    private Context context;

    private LanguageSettingHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static LanguageSettingHelper getInstance(Context context) {
        if (sLanguageSettingHelper == null) {
            synchronized (LanguageSettingHelper.class) {
                if (sLanguageSettingHelper == null) {
                    sLanguageSettingHelper = new LanguageSettingHelper(context);
                }
            }
        }
        return sLanguageSettingHelper;
    }

    private static final String[] LANGUAGES = new String[]{"en", "ar", "bn", "de", "es", "fr", "hi", "id", "ja", "ko", "ms", "my", "pt", "ru", "th"};

    public List<LanguageItem> generate() {
        List<LanguageItem> languageItemList = new ArrayList<>();

        for (String key : LANGUAGES) {
            LanguageItem item = new LanguageItem();
            Locale locale = new Locale(key);
            item.setLanguageName(locale.getDisplayName(locale));
            item.setZoneCode(key);
            if (item.getZoneCode() != null && item.getZoneCode().equals(obtainLanguageZoneCodeSelected())) {
                item.setSelected(true);
            }
            languageItemList.add(item);
        }
        return languageItemList;
    }

    public String getDefaultLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    public String obtainLanguageZoneCodeSelected() {
        return VstoreManager.getInstance(context).decode(true, KEY_LANGUAGE_ZONE_CODE_SELECTED, getDefaultLanguage(context));
    }

    public void setLanguageZoneCodeSelected(String zoneCodeSelected) {
        VstoreManager.getInstance(context).encode(true, KEY_LANGUAGE_ZONE_CODE_SELECTED, zoneCodeSelected);
    }

    public void changeAppLanguage(Activity activity, String languageZoneCode) {
        setLanguageZoneCodeSelected(languageZoneCode);

        initLanguageLocale(activity);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_UPDATE_LANGUAGE_FROM_RESTART, true);
        try {
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.page_in, R.anim.page_out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean isNeedToChangeDirection() {
        return obtainLanguageZoneCodeSelected().equals("ar") || obtainLanguageZoneCodeSelected().equals("fa");
    }

    public void initLanguageLocale(Activity activity) {
        String zoneCodeSelected = obtainLanguageZoneCodeSelected();
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(zoneCodeSelected, "");
        configuration.setLocale(locale);
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
        activity.getResources().updateConfiguration(configuration, dm);
        AutoSizeCompat.autoConvertDensityOfGlobal(resources);
    }
}
