package com.ironmeta.one.annotation;

import static com.ironmeta.one.report.ReportConstants.AppReport.*;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tom J
 * @package com.ironmeta.one.annotation
 * @description <p></p>
 * @date 2021/8/25 10:27 上午
 */
@StringDef({SOURCE_SERVER_PAGE_SERVER, SOURCE_SERVER_PAGE_MAIN, SOURCE_SERVER_NETWORK_CONNECTED, SOURCE_SERVER_PROXY_CONNECTED, SOURCE_SERVER_COLD_START})
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER})
public @interface ServerSource {
}
