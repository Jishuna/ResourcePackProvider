package me.jishuna.packprovider;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ServerIpResolver {
    private static final String checkIpUrl = "http://checkip.amazonaws.com/";
    private static String serverIp;

    public static String getServerIp() throws IOException, URISyntaxException {
        if (serverIp == null) {
            serverIp = resolveServerIp();
        }

        return serverIp;
    }

    private static String resolveServerIp() throws IOException, URISyntaxException {
        String ip = Bukkit.getIp();
        if (!ip.isBlank()) {
            return ip;
        }

        URL url = URL.of(new URI(checkIpUrl), null);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.readLine();
        }
    }
}
