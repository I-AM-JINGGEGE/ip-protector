package com.ironmeta.tahiti.constants;

import com.sdk.ssmod.IMSDK;

public class CoreServiceStateConstants {

    public static boolean isStarted(IMSDK.VpnState state) {
        if (state == IMSDK.VpnState.Connecting) {
            return true;
        }

        return false;
    }

    public static boolean isTesting(IMSDK.VpnState state) {
        if (state == IMSDK.VpnState.Connecting) {
            return true;
        }

        return false;
    }

    public static boolean isConnecting(IMSDK.VpnState state) {
        if (state == IMSDK.VpnState.Connecting) {
            return true;
        }

        return false;
    }

    public static boolean isConnected(IMSDK.VpnState state) {
        if (state == IMSDK.VpnState.Connected) {
            return true;
        }

        return false;
    }

    public static boolean isDisconnecting(IMSDK.VpnState state) {
        if (state == IMSDK.VpnState.Stopping) {
            return true;
        }

        return false;
    }

    public static boolean isDisconnected(IMSDK.VpnState state) {
        if (state == IMSDK.VpnState.Stopped || state == IMSDK.VpnState.Idle) {
            return true;
        }

        return false;
    }
}
