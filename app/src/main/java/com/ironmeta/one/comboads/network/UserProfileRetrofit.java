package com.ironmeta.one.comboads.network;

import static com.ironmeta.one.server.ServerPathConstants.DEBUG_HOST;
import static com.ironmeta.one.server.ServerPathConstants.HOST_MAIN_1;
import static com.ironmeta.one.server.ServerPathConstants.HOST_MAIN_2;
import static com.ironmeta.one.server.ServerPathConstants.HOST_MAIN_3;

import android.content.Context;
import android.text.TextUtils;

import com.ironmeta.one.BuildConfig;
import com.ironmeta.one.base.utils.BuildConfigUtils;
import com.ironmeta.one.base.utils.DeviceUtils;
import com.ironmeta.one.ads.bean.UserAdConfig;
import com.ironmeta.one.ads.network.IpUtil;
import com.ironmeta.tahiti.TahitiCoreServiceUserUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * author: xiaosailing
 * date: 2022-03-24
 * description:
 * version：1.0
 */
public class UserProfileRetrofit {

    private final UserProfileService userRangeService;
    private static UserProfileRetrofit userProfileRetrofit;

    private UserProfileRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.DEBUG ? DEBUG_HOST : HOST_MAIN_1)
                .addConverterFactory(GsonConverterFactory.create())
                .client(retryNetWorkHttpClient())
                .build();
        userRangeService = retrofit.create(UserProfileService.class);
    }

    private OkHttpClient retryNetWorkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (!BuildConfig.DEBUG) {
            builder.addInterceptor(new HostSelectionInterceptor(HOST_MAIN_2))
                    .addInterceptor(new HostSelectionInterceptor(HOST_MAIN_3));
        }
        OkHttpClient client = builder
                .connectTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpClientRetryInterceptor())
                .build();
        return client;
    }

    public static UserProfileRetrofit getInstance() {
        if (userProfileRetrofit == null) {
            userProfileRetrofit = new UserProfileRetrofit();
        }
        return userProfileRetrofit;
    }

    public void getUserProfile(Context context, Callback callback) {
        String pkg = BuildConfigUtils.getPackageName(context);
        String cv = BuildConfigUtils.getVersionCode(context);
        String cnl = BuildConfigUtils.getCnl(context);
        String did = TahitiCoreServiceUserUtils.getUid(context);
        String mcc = DeviceUtils.getMcc(context);
        String mnc = DeviceUtils.getMnc(context);
        String lang = DeviceUtils.getOSLang(context);
        String rgn = DeviceUtils.getOSCountry(context);
        int random = new Random().nextInt();

        Map<String, Object> map = new HashMap<>();
        map.put("cv", cv);
        map.put("cnl", cnl);
        map.put("pkg", pkg);
        map.put("did", did);
        map.put("mcc", mcc);
        map.put("mnc", mnc);
        map.put("lang", lang);
        map.put("rgn", rgn);
        map.put("_random", random);
        Call<UserAdConfig> rqCol = userRangeService.adUserProfile(map);
        try {
            rqCol.enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportBeat(Context context, Callback callback) {
        String pkg = BuildConfigUtils.getPackageName(context);
        String cv = BuildConfigUtils.getVersionCode(context);
        String cnl = BuildConfigUtils.getCnl(context);
        String did = TahitiCoreServiceUserUtils.getUid(context);
        String mcc = DeviceUtils.getMcc(context);
        String mnc = DeviceUtils.getMnc(context);
        String lang = DeviceUtils.getOSLang(context);
        String rgn = DeviceUtils.getOSCountry(context);
        int random = new Random().nextInt();

        Map<String, Object> map = new HashMap<>();
        map.put("cv", cv);
        map.put("cnl", cnl);
        map.put("pkg", pkg);
        map.put("did", did);
        map.put("mcc", mcc);
        map.put("mnc", mnc);
        map.put("lang", lang);
        map.put("rgn", rgn);
        String ipAddress = IpUtil.INSTANCE.getConnectedIdAddress();
        if (!TextUtils.isEmpty(ipAddress)) {
            map.put("ip", ipAddress);
        }
        map.put("_random", random);
        Call<ResponseBody> rqCol = userRangeService.reportBeat(map);
        try {
            rqCol.enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
