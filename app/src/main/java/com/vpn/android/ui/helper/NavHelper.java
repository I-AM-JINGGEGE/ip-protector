package com.vpn.android.ui.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.vpn.android.MainActivity;
import com.vpn.android.R;
import com.vpn.android.ui.regionselector2.ServerListActivity;
import com.vpn.android.ui.settings.appsbypass.AppsBypassSettingsActivity;
import com.vpn.android.ui.support.LanguageSettingActivity;
import com.vpn.android.ui.support.PrivacyPolicyActivity;
import com.vpn.android.ui.support.SupportUtils;

public class NavHelper {
    private static NavHelper sNavHelper = null;
    private Context context;

    private NavHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public static NavHelper getInstance(Context context) {

        if (sNavHelper == null) {
            synchronized (NavHelper.class) {
                if (sNavHelper == null) {
                    sNavHelper = new NavHelper(context);
                }
            }
        }
        return sNavHelper;
    }

    public void navToAnother(MainActivity activity, int itemId) {
        if (itemId == R.id.item_apps_bypass_settings) {
            activity.launchActivityForShowingAds(new Intent(activity, AppsBypassSettingsActivity.class));
        } else if (itemId == R.id.item_server_list) {
            activity.launchActivityForShowingAds(new Intent(activity, ServerListActivity.class));
        } else if (itemId == R.id.item_language) {
            activity.launchActivityForShowingAds(new Intent(activity, LanguageSettingActivity.class));
        } else if (itemId == R.id.item_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.vs_feature_share_content_text) + "\r\n" + SupportUtils.getGPLink(context));
            activity.launchActivityForShowingAds(Intent.createChooser(intent, context.getString(R.string.share_to)));
        } else if (itemId == R.id.item_privacy_policy) {
            activity.launchActivityForShowingAds(new Intent(activity, PrivacyPolicyActivity.class));
        }
    }

    public void initNavigationMenu(Activity activity, NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            View view = menu.getItem(i).getActionView();
            if (view == null) {
                return;
            }
            ImageView itemIV = view.findViewById(R.id.ic_item);
            TextView itemTV = view.findViewById(R.id.text_item);
            int itemIcon = -1;
            String itemTitle = null;
            int itemId = menu.getItem(i).getItemId();
            if (itemId == R.id.item_apps_bypass_settings) {
                itemIcon = R.mipmap.ic_apps_bypass_settings;
                itemTitle = context.getApplicationContext().getString(R.string.vs_feature_apps_bypass_title);
            } else if (itemId == R.id.item_server_list) {
                itemIcon = R.mipmap.ic_server_list;
                itemTitle = context.getApplicationContext().getString(R.string.vs_feature_region_selector_title);
            } else if (itemId == R.id.item_language) {
                itemIcon = R.mipmap.ic_language;
                itemTitle = context.getApplicationContext().getString(R.string.vs_feature_language_title);
            } else if (itemId == R.id.item_share) {
                itemIcon = R.mipmap.ic_share;
                itemTitle = context.getApplicationContext().getString(R.string.vs_feature_share_title);
            } else if (itemId == R.id.item_privacy_policy) {
                itemIcon = R.mipmap.ic_privacy_policy;
                itemTitle = context.getApplicationContext().getString(R.string.vs_feature_privacy_policy_title);
            } else if (itemId == R.id.item_feedback) {
                itemIcon = R.mipmap.ic_faq;
                itemTitle = context.getApplicationContext().getString(R.string.vs_feature_feedback_title);
            }

            itemIV.setImageResource(itemIcon);
            itemTV.setText(itemTitle);
        }
    }

}
