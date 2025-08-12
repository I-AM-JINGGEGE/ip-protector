package com.vpn.android.utils;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DataConversionUtil {
    public static Map<String, Object> bundleToMap(Bundle extras) {
        Map<String, Object> map = new HashMap<>();

        Set<String> ks = extras.keySet();
        Iterator<String> iterator = ks.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, extras.get(key));
        }
        return map;
    }

    public static Map<String, Object> createMap(String... keys_values) {
        Map<String, Object> map = new HashMap<>();
        if (keys_values == null || keys_values.length == 0) {
            return map;
        }
        for (int i=0;i<keys_values.length;i = i+2) {
            if (i+1>=keys_values.length) {
                break;
            }
            map.put(keys_values[i], keys_values[i+1]);
        }
        return map;
    }
}
