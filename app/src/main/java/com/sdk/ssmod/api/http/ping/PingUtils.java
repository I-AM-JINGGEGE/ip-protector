package com.sdk.ssmod.api.http.ping;

import android.text.TextUtils;

import com.sdk.ssmod.api.http.beans.FetchResponse;

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
        connectionResult.setReplyLength(0L);

        if (vpnServer == null) {
            connectionResult.setReplyLength(0L);
            connectionResult.setCost(0L);
            return connectionResult;
        }

        String ip = vpnServer.getHost();
        int port = vpnServer.getPort();

        if (TextUtils.isEmpty(ip)) {
            connectionResult.setReplyLength(0L);
            connectionResult.setCost(0L);
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
            connectionResult.setReplyLength(0L);
            connectionResult.setCost(0);
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

        connectionResult.setReplyLength(0L);
        connectionResult.setCost(System.currentTimeMillis() - start);
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

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public static PingResult getConnectionResultForTest(FetchResponse.Host vpnServer, final int connectTimeout, final int readTimeout) {
        PingResult connectionResult = new PingResult();
        try {
            connectionResult.setVPNServer(vpnServer);
            long cost = getRandomSleep();
            Thread.sleep(cost);
            connectionResult.setCost(cost);
            connectionResult.setReplyLength(512);
            return connectionResult;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return connectionResult;
    }

    public static int getRandomSleep() {
        return generateRandomInt(2000) + 300;
    }
}
