package com.meilinger.tj.admin;

public class NetworkDataHolder {
    private static String serverIP = "";
    private static int serverPort = 9696, devicePort = 9797;

    public static String getServerIP() {
        return serverIP;
    }

    public static void setServerIP(String serverIP) {
        NetworkDataHolder.serverIP = serverIP;
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static void setServerPort(int serverPort) {
        NetworkDataHolder.serverPort = serverPort;
    }

    public static int getDevicePort() {
        return devicePort;
    }

    public static void setDevicePort(int devicePort) {
        NetworkDataHolder.devicePort = devicePort;
    }
}
