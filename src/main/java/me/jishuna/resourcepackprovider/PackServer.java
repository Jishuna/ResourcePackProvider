package me.jishuna.resourcepackprovider;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public class PackServer {
    private final AtomicInteger nextPackId = new AtomicInteger();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<Integer, PackProvider> availablePacks = new HashMap<>();

    private final Plugin plugin;
    private final int port;

    private HttpServer server;
    private String url;

    public PackServer(Plugin plugin, String host, int port) {
        this.plugin = plugin;
        this.port = port;

        this.url = "http://%s:%s".formatted(host, port);
    }

    public boolean start() {
        try {
            startServer();
            createContext();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public PackProvider createProvider(File file) {
        Preconditions.checkArgument(file.exists(), "The provided file does not exist");

        int id = this.nextPackId.getAndIncrement();
        String packUrl = "%s/%s".formatted(this.url, id);
        PackData data;
        try {
            data = readData(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ResourcePackException("Failed to create resoruce pack provider", e);
        }

        PackProvider provider = new PackProvider(packUrl, data.bytes(), data.hash());
        this.availablePacks.put(id, provider);

        return provider;
    }

    private void startServer() throws IOException {
        InetSocketAddress address = new InetSocketAddress("0.0.0.0", this.port);

        this.server = HttpServer.create(address, 8);
        this.server.setExecutor(this.executor);
        this.server.start();

        this.plugin.getLogger().log(Level.INFO, "Starting local resource pack server on {0}", this.url);
    }

    private void createContext() {
        this.server.createContext("/", this::handleExchange);
    }

    private void handleExchange(HttpExchange exchange) {
        try (exchange) {
            String stringId = exchange.getRequestURI().toString().substring(1);
            Integer id = Ints.tryParse(stringId);
            if (id == null) {
                exchange.sendResponseHeaders(404, -1);
                this.plugin.getLogger().log(Level.WARNING, "Recieved a request for an invalid resource pack id: {0}", stringId);
                return;
            }

            PackProvider provider = this.availablePacks.get(id);
            if (provider == null) {
                exchange.sendResponseHeaders(404, -1);
                this.plugin.getLogger().log(Level.WARNING, "Recieved a request for an unknown resource pack id: {0}", id);
                return;
            }

            byte[] bytes = provider.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (Exception e) {
            throw new ResourcePackException("An exception occured while processing a resource pack request", e);
        }
    }

    private PackData readData(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fileStream = new FileInputStream(file);
                DigestInputStream stream = new DigestInputStream(fileStream, digest)) {
            byte[] bytes = stream.readAllBytes();

            return new PackData(bytes, digest.digest());
        }
    }
}
