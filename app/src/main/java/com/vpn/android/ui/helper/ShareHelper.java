package com.vpn.android.ui.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.vpn.android.R;
import com.vpn.android.base.utils.ActivityUtils;
import com.vpn.android.base.utils.ToastUtils;
import com.vpn.android.ui.bean.ShareItem;

import java.util.ArrayList;
import java.util.List;

import static com.vpn.android.constants.ShareConstants.EXTRAS_FOR_SMS_BODY_OF_SHARE_BY_SMS;
import static com.vpn.android.constants.ShareConstants.LABEL_SHARE_BY_COPY_LINK;
import static com.vpn.android.constants.ShareConstants.PACKAGE_NAME_OF_WHATS_APP;

public class ShareHelper {
    private static ShareHelper sShareHelper = null;
    private Context context;
    String contentShared;


    private ShareHelper(Context context) {
        this.context = context;
        contentShared = context.getResources().getString(R.string.vs_feature_share_content_text) + " "
                + context.getResources().getString(R.string.vs_feature_share_content_link);
    }

    public static ShareHelper getInstance(Context context) {
        if (sShareHelper == null) {
            synchronized (ShareHelper.class) {
                if (sShareHelper == null) {
                    sShareHelper = new ShareHelper(context);
                }
            }
        }
        return sShareHelper;
    }

    public List<ShareItem> generate() {
        List<ShareItem> shareItemList = new ArrayList<>();
        ShareItem shareItem;
        Intent shareIntent;

        shareIntent = new Intent(Intent.ACTION_SEND).setPackage(PACKAGE_NAME_OF_WHATS_APP).setType("text/plain");
        if (checkToWhetherInstalled(shareIntent)) {
            shareItem = new ShareItem();
            shareItem.setItemIcon(R.mipmap.ic_what_app);
            shareItem.setItemName(context.getString(R.string.vs_feature_share_item_name_whats_app));
            shareItemList.add(shareItem);
        }

        shareIntent = new Intent(Intent.ACTION_VIEW).setType("vnd.android-dir/mms-sms");
        if (checkToWhetherInstalled(shareIntent)) {
            shareItem = new ShareItem();
            shareItem.setItemIcon(R.mipmap.ic_sms);
            shareItem.setItemName(context.getString(R.string.vs_feature_share_item_name_sms));
            shareItemList.add(shareItem);
        }

        shareIntent = new Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:"));
        if (checkToWhetherInstalled(shareIntent)) {
            shareItem = new ShareItem();
            shareItem.setItemIcon(R.mipmap.ic_email);
            shareItem.setItemName(context.getString(R.string.vs_feature_share_item_name_email));
            shareItemList.add(shareItem);
        }

        shareItem = new ShareItem();
        shareItem.setItemIcon(R.mipmap.ic_copy_link);
        shareItem.setItemName(context.getString(R.string.vs_feature_share_item_name_copy_link));
        shareItemList.add(shareItem);

        shareIntent = new Intent().setAction(Intent.ACTION_SEND).setType("text/plain");
        if (checkToWhetherInstalled(shareIntent)) {
            shareItem = new ShareItem();
            shareItem.setItemIcon(R.mipmap.ic_more);
            shareItem.setItemName(context.getString(R.string.vs_feature_share_item_name_more));
            shareItemList.add(shareItem);
        }

        return shareItemList;
    }

    public void shareByWhatsApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .setPackage(PACKAGE_NAME_OF_WHATS_APP)
                .putExtra(Intent.EXTRA_TEXT, contentShared);

        checkToStartActivity(shareIntent, context.getResources().getString(R.string.vs_feature_share_item_tip_whats_app));
    }

    public void shareBySMS() {
        Intent shareIntent = new Intent(Intent.ACTION_VIEW)
                .putExtra(EXTRAS_FOR_SMS_BODY_OF_SHARE_BY_SMS, contentShared)
                .setType("vnd.android-dir/mms-sms");
        checkToStartActivity(shareIntent, context.getResources().getString(R.string.vs_feature_share_item_tip_sms));
    }

    public void shareByEmail() {
        Intent shareIntent = new Intent(Intent.ACTION_SENDTO)
                .setData(Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.vs_feature_share_content_email_subject))
                .putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.vs_feature_share_content_email_body));

        if (!ActivityUtils.safeStartActivityWithIntent(context, shareIntent)) {
            ToastUtils.showToast(context, context.getResources().getString(R.string.vs_feature_share_item_tip_email));
        }
    }

    public void shareByCopyLink() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(LABEL_SHARE_BY_COPY_LINK, context.getResources().getString(R.string.vs_feature_share_content_link));
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        ToastUtils.showToast(context, context.getResources().getString(R.string.vs_feature_share_item_tip_copy_link));
    }

    public void ShareByMore() {
        Intent shareIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, contentShared)
                .setType("text/plain");

        checkToStartActivity(shareIntent, context.getResources().getString(R.string.vs_feature_share_item_tip_more));
    }

    private void checkToStartActivity(Intent intent, String errorTip) {
        if (!ActivityUtils.safeStartActivityWithIntent(context,
                Intent.createChooser(intent, context.getResources().getString(R.string.vs_feature_share_title_for_chooser)))) {
            ToastUtils.showToast(context, errorTip);
        }
    }

    private Boolean checkToWhetherInstalled(Intent intent) {
        return intent.resolveActivity(context.getPackageManager()) != null;
    }
}
