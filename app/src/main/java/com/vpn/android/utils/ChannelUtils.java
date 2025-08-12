package com.vpn.android.utils;


public class ChannelUtils {
    private final static String CHANNEL_GP = "gp";
    private final static String CHANNEL_NEWINSIGHT = "rqn4fd8ad08";

    public static boolean isGooglePlay(String cnl) {
        return CHANNEL_GP.equals(cnl);
    }

    public static boolean isNewInsight(String cnl) {
        return CHANNEL_NEWINSIGHT.equals(cnl);
    }

}