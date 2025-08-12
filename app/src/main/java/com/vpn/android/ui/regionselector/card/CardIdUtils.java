package com.vpn.android.ui.regionselector.card;

import androidx.annotation.Nullable;

public class CardIdUtils {
    public static String getCardId(boolean connectedCard, @Nullable String regionUUID) {
        if (connectedCard) {
            return "ConnectedCard";
        }
        return "RegionCard_" + regionUUID;
    }
}
