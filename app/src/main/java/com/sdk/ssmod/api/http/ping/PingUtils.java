package com.sdk.ssmod.api.http.ping;

import android.text.TextUtils;

import com.sdk.ssmod.api.http.beans.FetchResponse;
import com.vpn.android.report.VpnReporter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PingUtils {
    public static PingResult getConnectionResult(FetchResponse.Host vpnServer, final int connectTimeout, final int readTimeout) {
        PingResult connectionResult = new PingResult();
        connectionResult.setVPNServer(vpnServer);
        connectionResult.setCost(0L);

        if (vpnServer == null) {
            connectionResult.setCost(0L);
            return connectionResult;
        }

        String ip = vpnServer.getHost();
        int port = vpnServer.getPort();

        if (TextUtils.isEmpty(ip)) {
            connectionResult.setCost(0L);
            VpnReporter.INSTANCE.reportPingResult(false, "ip_address is null", 0, vpnServer);
            return connectionResult;
        }

        InetSocketAddress inetAddress = new InetSocketAddress(ip, port);
        Socket socket = new Socket();

        long start = System.currentTimeMillis();

        try {
            socket.connect(inetAddress, connectTimeout);
            socket.setSoTimeout(readTimeout);
        } catch (Exception e) {
            e.printStackTrace();
            connectionResult.setCost(System.currentTimeMillis() - start);
            VpnReporter.INSTANCE.reportPingResult(false, e.getMessage(), connectionResult.getCost(), vpnServer);
            return connectionResult;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    socket = null;
                }
            }
        }
        connectionResult.setCost(System.currentTimeMillis() - start);
        connectionResult.setResult(true);
        VpnReporter.INSTANCE.reportPingResult(true, null, connectionResult.getCost(), vpnServer);
        return connectionResult;
    }
}
