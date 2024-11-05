package me.jishuna.packprovider;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ServerIpResolver {
    private static final String checkIpUrl = "http://checkip.amazonaws.com/";
    private static String serverIp;

    public static String getServerIp() {
        if (serverIp == null) {
            resolveServerIp();
        }

        return serverIp;
    }

    private static void resolveServerIp() {
        String ip = Bukkit.getIp();
        if (!ip.isBlank()) {
            return;
        }

        URL url;
        try {
            url = URL.of(new URI(checkIpUrl), null);
        } catch (URISyntaxException | MalformedURLException e) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            serverIp = br.readLine();
        } catch (IOException ignored) {

        }
    }
}
