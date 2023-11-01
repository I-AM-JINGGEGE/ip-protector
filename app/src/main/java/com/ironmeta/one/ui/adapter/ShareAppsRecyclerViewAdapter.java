package com.ironmeta.one.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ironmeta.one.R;
import com.ironmeta.one.ui.bean.ShareItem;
import com.ironmeta.one.ui.helper.LanguageSettingHelper;
import com.ironmeta.one.ui.helper.ShareHelper;

import java.util.List;

public class ShareAppsRecyclerViewAdapter extends RecyclerView.Adapter<ShareAppsRecyclerViewAdapter.ShareAppsViewHolder> {
    Context appContext;
    private List<ShareItem> shareItemList;

    public ShareAppsRecyclerViewAdapter(Context context) {
        appContext = context.getApplicationContext();
        shareItemList = ShareHelper.getInstance(appContext).generate();
    }

    @NonNull
    @Override
    public ShareAppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ShareAppsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_share, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShareAppsViewHolder holder, int position) {
        ShareItem shareItem = shareItemList.get(position);
        if (position == getItemCount() - 1) {
            holder.bind(shareItem, false);
        } else {
            holder.bind(shareItem, true);
        }

        holder.setOnItemClickListener(appContext, shareItem);
    }

    @Override
    public int getItemCount() {
        return shareItemList.size();
    }

    static class ShareAppsViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private ImageView icAppLogo;
        private TextView tvAppName;
        private View viewDivider;
        private ImageView viewDecoration;

        public ShareAppsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            icAppLogo = itemView.findViewById(R.id.iv_share_apps_logo);
            tvAppName = itemView.findViewById(R.id.tv_share_apps_name);
            viewDivider = itemView.findViewById(R.id.view_divider);
            viewDecoration = itemView.findViewById(R.id.view_decoration);
        }

        public void bind(ShareItem shareItem, Boolean isViewDividerVisible) {
            icAppLogo.setImageResource(shareItem.getItemIcon());
            tvAppName.setText(shareItem.getItemName());
            if (isViewDividerVisible) {
                viewDivider.setVisibility(View.VISIBLE);
            } else {
                viewDivider.setVisibility(View.INVISIBLE);
            }
            if(LanguageSettingHelper.getInstance(itemView.getContext()).isNeedToChangeDirection()) {
                viewDecoration.setImageResource(R.mipmap.ic_go_1);
            } else {
                viewDecoration.setImageResource(R.mipmap.ic_go_0);
            }

        }

        @SuppressLint("NonConstantResourceId")
        public void setOnItemClickListener(Context context, ShareItem shareItem) throws Resources.NotFoundException {
            itemView.setOnClickListener(v -> {
                if (shareItem.getItemName().equals(context.getResources().getString(R.string.vs_feature_share_item_name_whats_app))) {
                    ShareHelper.getInstance(context).shareByWhatsApp();
                    return;
                }

                if (shareItem.getItemName().equals(context.getResources().getString(R.string.vs_feature_share_item_name_sms))) {
                    ShareHelper.getInstance(context).shareBySMS();
                    return;
                }

                if (shareItem.getItemName().equals(context.getResources().getString(R.string.vs_feature_share_item_name_email))) {
                    ShareHelper.getInstance(context).shareByEmail();
                    return;
                }

                if (shareItem.getItemName().equals(context.getResources().getString(R.string.vs_feature_share_item_name_copy_link))) {
                    ShareHelper.getInstance(context).shareByCopyLink();
                    return;
                }

                if (shareItem.getItemName().equals(context.getResources().getString(R.string.vs_feature_share_item_name_more))) {
                    ShareHelper.getInstance(context).ShareByMore();
                    return;
                }
            });
        }
    }
}
