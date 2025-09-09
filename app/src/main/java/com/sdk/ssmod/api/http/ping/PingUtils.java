package com.sdk.ssmod.api.http.ping;

import android.text.TextUtils;

import com.sdk.ssmod.api.http.beans.FetchResponse;
import com.vpn.android.report.VpnReporter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

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
        String pwd = vpnServer.getPassword();

        if (TextUtils.isEmpty(ip)) {
            connectionResult.setCost(0L);
            VpnReporter.INSTANCE.reportPingResult(false, "ip address is null", 0, vpnServer);
            return connectionResult;
        }

        InetSocketAddress inetAddress = new InetSocketAddress(ip, port);
        Socket socket = new Socket();
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;


        long start = System.currentTimeMillis();

        try {
            byte[] bytesToQuery = convertPwdToBytesToQuery(pwd);
            if (bytesToQuery == null || bytesToQuery.length < 16) {
                connectionResult.setCost(System.currentTimeMillis() - start);
                VpnReporter.INSTANCE.reportPingResult(true, "pwd is invalid", connectionResult.getCost(), vpnServer);
                return connectionResult;
            }

            start = System.currentTimeMillis();

            socket.connect(inetAddress, connectTimeout);
            socket.setSoTimeout(readTimeout);

            bos = new BufferedOutputStream(socket.getOutputStream());
            bis = new BufferedInputStream(socket.getInputStream(), 1024);

            for (int i = 0; i < bytesToQuery.length; i++) {
                bos.write(bytesToQuery[i]);
            }
            bos.flush();

            byte buffer[] = new byte[512];
            int bytesRead;

            while (((bytesRead = bis.read(buffer)) != -1)) {
                connectionResult.setResult(true);
                connectionResult.setCost(System.currentTimeMillis() - start);
                VpnReporter.INSTANCE.reportPingResult(true, null, connectionResult.getCost(), vpnServer);
                return connectionResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            connectionResult.setCost(System.currentTimeMillis() - start);
            VpnReporter.INSTANCE.reportPingResult(false, e.getMessage(), connectionResult.getCost(), vpnServer);
            return connectionResult;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bos = null;
                }
            }

            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bis = null;
                }
            }

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
        VpnReporter.INSTANCE.reportPingResult(false, "timeout", connectionResult.getCost(), vpnServer);
        return connectionResult;
    }

    private static byte[] convertPwdToBytesToQuery(String pwd) throws
            HostNotResolvedException {
        if (pwd == null) {
            throw new HostNotResolvedException();
        }

        byte[] bytesToQuery = new byte[16];

        byte[] bytesA = new byte[4];
        generateRandomBytes(bytesA);

        byte[] bytesB = new byte[4];
        generateRandomBytes(bytesB);

        try {
            byte[] bytesPwd = pwd.getBytes("US-ASCII");

            if (bytesPwd.length < 4) {
                throw new HostNotResolvedException();
            }

            for (int i = 0; i < 16; i++) {
                if (i >= 0 && i < 4) {
                    bytesToQuery[i] = bytesA[i];
                }

                if (i >= 4 && i < 8) {
                    bytesToQuery[i] = (byte) (bytesPwd[i - 4] ^ bytesA[i - 4]);
                }

                if (i >= 8 && i < 12) {
                    bytesToQuery[i] = bytesB[i - 8];
                }

                if (i >= 12 && i < 16) {
                    bytesToQuery[i] = (byte) (bytesPwd[i - 12] ^ bytesB[i - 12]);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new HostNotResolvedException();
        }

        return bytesToQuery;
    }

    private static int generateRandomInt(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    private static void generateRandomBytes(byte[] bytes) {
        Random random = new Random();
        random.nextBytes(bytes);
    }
}
