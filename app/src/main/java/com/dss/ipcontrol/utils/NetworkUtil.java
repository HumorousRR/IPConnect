package com.dss.ipcontrol.utils;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkUtil {

    public static String getIpAddress(Context context) {
        ConnectivityManager netManager = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = netManager.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return getWifiIpAddress(context);
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return getEthIpAddress();
            }
        }
        return "0.0.0.0";
    }

    private static String getEthIpAddress() {
        String infaceName = "eth0";
        String ip = "0.0.0.0";
        try {
            Enumeration<NetworkInterface> netInterface = NetworkInterface.getNetworkInterfaces();
            while (netInterface.hasMoreElements()) {
                NetworkInterface inface = netInterface.nextElement();
                if (!inface.isUp() || !infaceName.equals(inface.getDisplayName())) {
                    continue;
                }
                Enumeration<InetAddress> netAddressList = inface.getInetAddresses();
                while (netAddressList.hasMoreElements()) {
                    InetAddress inetAddress = netAddressList.nextElement();
                    if (inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }


    private static String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }


    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                if (nc != null) {
                    if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return "WIFI";
                    } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return "ETHERNET";
                    }
                }
            }
        }
        return "UNKNOWN";
    }

}
