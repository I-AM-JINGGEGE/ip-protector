package com.ironmeta.one.ui.settings.appsbypass;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ironmeta.one.MainApplication;
import com.ironmeta.one.R;

import java.util.List;

public class AppsInfoRecyclerViewAdapter extends RecyclerView.Adapter<AppsInfoRecyclerViewAdapter.MyViewHolder> {
    private List<AppInfo> mAppsInfo;
    private AppsBypassSettingsViewModel mAppsBypassSettingsViewModel;

    public AppsInfoRecyclerViewAdapter(AppsBypassSettingsViewModel appsBypassSettingsViewModel) {
        mAppsBypassSettingsViewModel = appsBypassSettingsViewModel;
    }

    public void setAppsInfo(List<AppInfo> appsInfo) {
        mAppsInfo = appsInfo;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_info, parent,false));
        viewHolder.toggleBtn.setOnCheckedChangeListener((buttonView, isChecked) -> mAppsBypassSettingsViewModel.setAppBypass((String) viewHolder.itemView.getTag(), !isChecked));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppInfo appInfo = mAppsInfo.get(position);
        holder.itemView.setTag(appInfo.getPackageName());
        loadAppIcon(appInfo, holder.appIcon);
        holder.appName.setText(appInfo.getAppName());
        holder.toggleBtn.setChecked(!appInfo.getBypass());
    }

    private void loadAppIcon(AppInfo appInfo, ImageView imageView) {
        if (appInfo.getIconResId() == 0) {
            imageView.setImageDrawable(appInfo.getAppIcon());
            return;
        }
        try {
            Context toUse = MainApplication.Companion.getContext().createPackageContext(appInfo.getPackageName(), 0);
            Resources resources = toUse.getResources();
            Uri uri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(appInfo.getPackageName())
                    .appendPath(resources.getResourceTypeName(appInfo.getIconResId()))
                    .appendPath(resources.getResourceEntryName(appInfo.getIconResId()))
                    .build();
            Glide.with(MainApplication.Companion.getContext()).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (mAppsInfo == null) {
            return 0;
        }
        return mAppsInfo.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        ToggleButton toggleBtn;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.icon_app);
            appName = itemView.findViewById(R.id.name_app);
            toggleBtn = itemView.findViewById(R.id.switch_func);
        }
    }
}
