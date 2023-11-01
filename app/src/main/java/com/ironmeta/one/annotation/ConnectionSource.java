package com.ironmeta.one.annotation;

import static com.ironmeta.one.report.ReportConstants.AppReport.SOURCE_CONNECTION_NOTIFICATION;
import static com.ironmeta.one.report.ReportConstants.AppReport.SOURCE_CONNECTION_PAGE_MAIN;
import static com.ironmeta.one.report.ReportConstants.AppReport.SOURCE_CONNECTION_PAGE_SERVER;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tom J
 * @package com.ironmeta.one.annotation
 * @description <p></p>
 * @date 2021/8/25 11:07 上午
 */
@StringDef({SOURCE_CONNECTION_PAGE_MAIN, SOURCE_CONNECTION_PAGE_SERVER, SOURCE_CONNECTION_NOTIFICATION})
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER})
public @interface ConnectionSource {
}
