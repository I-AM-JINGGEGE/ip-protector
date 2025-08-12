package com.github.shadowsocks.aidl.imsvc;

import com.github.shadowsocks.imsvc.uptime.Uptime;

interface IUptimeLimitService {
    const String UPTIME_LIMIT_SERVICE = "UPTIME_LIMIT_SERVICE";

    void startNew(in Uptime uptime);
    void extend(in long duration);
    void cancel();

    Uptime getOngoing();
}
