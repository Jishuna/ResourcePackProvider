package me.jishuna.resourcepackprovider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.bukkit.Bukkit;

public final class ResourcePackProvider {
    private static final SemanticVersion MC1_20_2 = new SemanticVersion(1, 20, 2);
    public static final boolean IS_NEW_SYSTEM = SemanticVersion.fromString(getServerVersion()).isNewerThan(MC1_20_2);

    public static String queryServerHost() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(new URI("https://ifconfig.me/ip")).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return null;
        }
    }

    private static String getServerVersion() {
        String version = Bukkit.getServer().getBukkitVersion();
        if (version.contains("-")) {
            return version.substring(0, version.indexOf('-'));
        }

        return version;
    }

    private ResourcePackProvider() {
    }
}
