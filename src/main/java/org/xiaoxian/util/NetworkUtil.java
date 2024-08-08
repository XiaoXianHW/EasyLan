package org.xiaoxian.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

public class NetworkUtil {
    public static String getPublicIPv4() {
        try {
            URL url = new URL("https://easylan-api.xiaoxian.org/api/myip");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                return jsonObject.get("ip").getAsString();
            }

            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static boolean checkIpIsPublic() {
        try {
            URL url = new URL("https://easylan-api.xiaoxian.org/api/myipcheck");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                return jsonObject.get("public").getAsString().equals("true");
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getLocalIpv4() {
        return getLocalAddress(false) != null ? getLocalAddress(false).getHostAddress() : "Unknown";
    }

    public static String getLocalIpv6() {
        return getLocalAddress(true) != null ? getLocalAddress(true).getHostAddress() : "Unknown";
    }

    public static InetAddress getLocalAddress(boolean preferIPv6) {
        List<InetAddress> addresses = new ArrayList<>();
        Set<String> virtualNetworkInterfaces = new HashSet<>(Arrays.asList("vmware", "virtual", "hyper-v", "vbox", "virtualbox"));

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                String displayName = networkInterface.getDisplayName().toLowerCase();

                // 检查是否是虚拟网卡
                if (isVirtualInterface(displayName, virtualNetworkInterfaces)) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        addresses.add(inetAddress);
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("[EasyLAN] Error getting local IP address: " + e.getMessage());
        }

        InetAddress IPv4Address = null;
        InetAddress IPv6Address = null;

        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                if (IPv4Address == null && isPrivateIPv4Address(address)) {
                    IPv4Address = address;
                }
            } else if (address instanceof Inet6Address) {
                if (IPv6Address == null && isValidIPv6Address(address)) {
                    IPv6Address = address;
                }
            }
        }

        return preferIPv6 ? IPv6Address : IPv4Address;
    }

    private static boolean isVirtualInterface(String displayName, Set<String> virtualNetworkInterfaces) {
        for (String virtualInterface : virtualNetworkInterfaces) {
            if (displayName.contains(virtualInterface)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPrivateIPv4Address(InetAddress address) {
        if (address instanceof Inet4Address) {
            String ip = address.getHostAddress();
            return ip.startsWith("10.") ||
                    ip.startsWith("192.168.") ||
                    ip.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*");
        }
        return false;
    }

    private static boolean isValidIPv6Address(InetAddress address) {
        if (address instanceof Inet6Address) {
            String ip = address.getHostAddress();
            return !ip.contains("%") && !ip.matches(".*:0:0:0:0:0:.*") && !ip.matches(".*::.*");
        }
        return false;
    }
}
