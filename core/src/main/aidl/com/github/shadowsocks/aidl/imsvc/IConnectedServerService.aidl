package com.github.shadowsocks.aidl.imsvc;

import com.github.shadowsocks.imsvc.connection.ConnectedTo;

interface IConnectedServerService {
    const String CONNECTED_SERVER_SERVICE = "CONNECTED_SERVER_SERVICE";

    void setConnectedTo(in @nullable ConnectedTo connectedTo);
    @nullable
    ConnectedTo getConnectedTo();
}
