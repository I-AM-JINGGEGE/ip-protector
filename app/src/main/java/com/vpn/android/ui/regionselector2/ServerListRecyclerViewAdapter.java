package com.vpn.android.ui.regionselector2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vpn.base.vstore.VstoreManager;
import com.vpn.android.R;
import com.vpn.android.constants.KvStoreConstants;
import com.vpn.android.region.RegionUtils;
import com.vpn.android.ui.helper.LanguageSettingHelper;
import com.sdk.ssmod.api.http.beans.FetchResponse;

public class ServerListRecyclerViewAdapter extends RecyclerView.Adapter<ServerListRecyclerViewAdapter.MyViewHolder> {
    private Context mAppContext;
    private FetchResponse mVPNServerRegions;
    private IItemCallback mItemCallback;
    private String regionUUIDSelected;

    public ServerListRecyclerViewAdapter(@NonNull Context context, @Nullable IItemCallback itemCallback) {
        mAppContext = context.getApplicationContext();
        mItemCallback = itemCallback;
        regionUUIDSelected = VstoreManager.getInstance(context)
                .decode(true, KvStoreConstants.KEY_CORE_SERVICE_REGION_UUID_SELECTED, "");
    }

    public void setVPNServerRegions(@Nullable FetchResponse vpnServerRegions) {
        mVPNServerRegions = vpnServerRegions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_region_info, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FetchResponse.ServerZone vpnServerRegion = mVPNServerRegions.getServerZones().get(position);
        holder.setListener(mItemCallback, vpnServerRegion);
        holder.bind(mAppContext, regionUUIDSelected, vpnServerRegion);

    }

    @Override
    public int getItemCount() {
        if (mVPNServerRegions == null) {
            return 0;
        }
        return mVPNServerRegions.getServerZones().size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView regionIcon;
        TextView countryName;
        TextView regionName;
        ImageView signalLvIcon;
        ImageView selectedIcon;
        View itemView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            regionIcon = itemView.findViewById(R.id.region_flag);
            signalLvIcon = itemView.findViewById(R.id.ivSignal);
            countryName = itemView.findViewById(R.id.region_name);
            regionName = itemView.findViewById(R.id.region_desc);
            selectedIcon = itemView.findViewById(R.id.ivSelected);
        }

        void setListener(IItemCallback itemCallback, FetchResponse.ServerZone vpnServerRegion) {
            itemView.setOnClickListener(v -> {
                if (itemCallback != null) {
                    itemCallback.onItemClick(vpnServerRegion);
                }
            });
        }

        void bind(Context context, String regionUUIDSelected, FetchResponse.ServerZone vpnServerRegion) {
            regionIcon.setImageResource(RegionUtils.getRegionFlagImageResource(context, vpnServerRegion.getCountry()));
            int signalIvId;
            switch (vpnServerRegion.getSignalStrength()) {
                case Lost:
                    if(LanguageSettingHelper.getInstance(context).isNeedToChangeDirection()) {
                        signalIvId = R.mipmap.ic_signal_b_1;
                    } else {
                        signalIvId = R.mipmap.ic_signal_a_1;
                    }
                    break;

                case Crap:
                    if(LanguageSettingHelper.getInstance(context).isNeedToChangeDirection()) {
                        signalIvId = R.mipmap.ic_signal_b_2;
                    } else {
                        signalIvId = R.mipmap.ic_signal_a_2;
                    }
                    signalIvId = R.mipmap.ic_signal_a_2;
                    break;

                case Poor:
                    if(LanguageSettingHelper.getInstance(context).isNeedToChangeDirection()) {
                        signalIvId = R.mipmap.ic_signal_b_3;
                    } else {
                        signalIvId = R.mipmap.ic_signal_a_3;
                    }
                    break;

                case Okay:
                    if(LanguageSettingHelper.getInstance(context).isNeedToChangeDirection()) {
                        signalIvId = R.mipmap.ic_signal_b_4;
                    } else {
                        signalIvId = R.mipmap.ic_signal_a_4;
                    }
                    break;

                default:
                    if(LanguageSettingHelper.getInstance(context).isNeedToChangeDirection()) {
                        signalIvId = R.mipmap.ic_signal_b_5;
                    } else {
                        signalIvId = R.mipmap.ic_signal_a_5;
                    }
                    break;
            }
            signalLvIcon.setImageResource(signalIvId);
            countryName.setText(RegionUtils.getRegionName(context, vpnServerRegion.getCountry()));
            regionName.setText(RegionUtils.getRegionDesc(context, vpnServerRegion.getCountry(), vpnServerRegion.getCity()));
            selectedIcon.setImageResource(R.mipmap.ic_unselected);
            if (regionUUIDSelected.equals("")) return;
            if (regionUUIDSelected.equals(vpnServerRegion.getId())) {
                selectedIcon.setImageResource(R.mipmap.ic_selected);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public interface IItemCallback {
        void onItemClick(@NonNull FetchResponse.ServerZone vpnServerRegion);
    }
}
