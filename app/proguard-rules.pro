#sdk core
-keep class com.appcrypto.Crypto { *; }

-keepattributes SourceFile,LineNumberTable
-dontobfuscate
-dontwarn com.google.android.gms.internal.**

#gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

#okhttp3
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

#install referrer
-dontwarn com.android.installreferrer
-keep public class com.android.installreferrer.**{ *; }

-keep class me.jessyan.autosize.** { *; }
-keep interface me.jessyan.autosize.** { *; }

-keep class org.libpag.** {*;}
-keep class androidx.exifinterface.** {*;}

-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.** { *; }

-keep class com.appsflyer.** { *; }
-keep class kotlin.jvm.internal.** { *; }

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-keep class org.bouncycastle.** { *; }
-keep class org.conscrypt.** { *; }
-keep class org.openjsse.** { *; }

# ProGuard file

-renamesourcefileattribute SourceFile
-keepattributes LineNumberTable,Signature,*Annotation*
# -keepparameternames
-ignorewarnings
-repackageclasses

# Kotlin
-dontwarn kotlin.**
-keep class **.*$DefaultImpls { *; }

# GMS
-dontwarn com.google.android.gms.internal.**

# Gson
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# okhttp3
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# SDK Core public APIs
-keep class com.sdk.ssmod.IMSDK {
  public <methods>;
  public static <fields>;
}
-keep class com.sdk.ssmod.IMSDK$* { *; }
-keep interface com.github.shadowsocks.IIMSDKApplication { *; }
-keep interface com.sdk.ssmod.IIMSDKApplication$* { *; }
-keep interface com.sdk.ssmod.IServers { *; }
-keep interface com.sdk.ssmod.IDevice { *; }
-keep interface com.sdk.ssmod.IDevice$* { *; }
-keep class com.sdk.ssmod.IMSDK$WithResponseBuilder$ConnectedTo { *; }
-keep class com.sdk.ssmod.imsvcipc.IUptimeTimer { *; }
-keep class com.sdk.ssmod.imsvcipc.UptimeLimit { public <methods>; }
-keep class com.sdk.ssmod.beans.* { *; }
-keep class com.sdk.ssmod.api.http.beans.* { *; }
-keep class com.sdk.ssmod.WithResponseBuilderImpl** { public <methods>; }
-keep class com.sdk.ssmod.imsvcipc.IUptimeTimerEmptyImpl { *; }
-keep class com.sdk.ssmod.IServersImpl { public <methods>; }
-keep class com.sdk.ssmod.IServers$GeoRestrictedException { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
# 保留 Retrofit 接口
-keep interface com.example.api.** { *; }
# 保留数据模型类
-keep class com.example.model.** { *; }
# 保留 Gson 相关类
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
# 保留 OkHttp 相关类
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
# 保留动态生成的类
-keep class retrofit2.**$** { *; }
# 保留数据模型类字段
-keepclassmembers class com.example.model.** {
    *;
}