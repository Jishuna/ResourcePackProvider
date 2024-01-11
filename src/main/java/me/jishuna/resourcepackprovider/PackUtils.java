package me.jishuna.resourcepackprovider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.bukkit.Bukkit;

/**
 * Resource pack related utilities.
 */
public final class PackUtils {
    private static final SemanticVersion MC1_20_2 = new SemanticVersion(1, 20, 2);
    public static final boolean IS_NEW_SYSTEM = SemanticVersion.fromString(getServerVersion()).isNewerThan(MC1_20_2);

    /**
     * Queries the external ip/host for the running minecraft server. <br>
     * This involves an external web request so it is highly recommended you cache
     * this value.
     *
     * @return the external ip/host or null if it cannot be determined
     */
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

    private PackUtils() {
    }
}
