package com.vpn.android.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vpn.android.R;
import com.vpn.android.ui.bean.LanguageItem;
import com.vpn.android.ui.helper.LanguageSettingHelper;

import java.util.List;

public class LanguageSettingRecyclerViewAdapter extends RecyclerView.Adapter<LanguageSettingRecyclerViewAdapter.LanguageSettingViewHolder> {
    private List<LanguageItem> languageItemList;
    private Activity activity;

    private int indexOfSelected = -1;

    public LanguageSettingRecyclerViewAdapter(Activity activity) {
        this.activity = activity;
        languageItemList = LanguageSettingHelper.getInstance(activity).generate();
        for (int i = 0; i < languageItemList.size(); i++) {
            if (!languageItemList.get(i).getSelected()) {
                return;
            }
            indexOfSelected = i;
        }
    }

    @NonNull
    @Override
    public LanguageSettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LanguageSettingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_language, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageSettingViewHolder holder, int position) {
        holder.bind(languageItemList.get(position));

        holder.itemView.setOnClickListener(v -> {
            if (position == indexOfSelected) return;
            if (indexOfSelected != -1) {
                languageItemList.get(indexOfSelected).setSelected(false);
                notifyItemChanged(indexOfSelected);
            }
            indexOfSelected = position;
            notifyItemChanged(position);

            LanguageSettingHelper.getInstance(activity).changeAppLanguage(activity, languageItemList.get(position).getZoneCode());
        });
    }

    @Override
    public int getItemCount() {
        return languageItemList.size();
    }

    static class LanguageSettingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLanguageName;
        private final ImageView ivSelected;

        public LanguageSettingViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLanguageName = itemView.findViewById(R.id.tv_name_language);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }

        public void bind(LanguageItem languageItem) {
            tvLanguageName.setText(languageItem.getLanguageName());

            if (languageItem.getSelected()) {
                ivSelected.setImageResource(R.mipmap.ic_selected);
            } else {
                ivSelected.setImageResource(R.mipmap.ic_unselected);
            }
        }
    }
}
