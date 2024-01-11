package me.jishuna.resourcepackprovider;

import com.google.common.base.Preconditions;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Represents a local HTTP server that can send resource packs to clients.
 */
public class PackServer {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Map<UUID, PackProvider> availablePacks = new HashMap<>();
    private final PackListeners listeners = new PackListeners(this);

    private final Plugin plugin;
    private final int port;

    private HttpServer server;
    private String url;
    private boolean running = false;

    /**
     * Creates a new {@link PackServer} instance, does not start the server.
     *
     * @param plugin the plugin instance that is creating this server
     * @param host   the hostname/ip to use for the server
     * @param port   the port to use for the server
     * @see PackServer#start()
     * @see PackServer#registerEvents()
     */
    public PackServer(Plugin plugin, String host, int port) {
        this.plugin = plugin;
        this.port = port;

        this.url = "http://%s:%s".formatted(host, port);
    }

    /**
     * Attempt to start the server
     *
     * @return true if successful, false if an error occurs
     */
    public boolean start() {
        try {
            startServer();
            createContext();
            this.running = true;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Stop this server immediately. <br>
     * <b>Note:</b> once a PackServer is stopped it cannot be reused, a new server
     * must be created.
     */
    public void stop() {
        stop(0);
        this.plugin.getLogger().log(Level.INFO, "Stopping local resource pack server on {0}", this.url);
    }

    /**
     * Stop this server and wait a maximum of {@code maxWaitSeconds} for any
     * requests to finish. <br>
     * <b>Note:</b> once a PackServer is stopped it cannot be reused, a new server
     * must be created.
     *
     * @param maxWaitSeconds the maximum number of seconds to wait for any requests
     *                       to finish.
     */
    public void stop(int maxWaitSeconds) {
        this.server.stop(maxWaitSeconds);
        this.running = false;
    }

    /**
     * Checks if this PackServer is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Register event listeners for this server. <br>
     * Required if you wish to create providers that automatically apply to players
     * when they join or check the status of a {@link PackProvider} for a player.
     *
     * @see PackServer#createProvider(File, boolean)
     * @see PackProvider#getStatus(Player)
     */
    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this.listeners, this.plugin);
    }

    /**
     * Create a pack provider for the given file. <br>
     * The provided file must exist.
     *
     * @param file the file
     * @return a PackProvider for the given file, or null if an error occurs
     */
    public PackProvider createProvider(File file) {
        return createProvider(file, false);
    }

    /**
     * Create a pack provider for the given file and optionally sets it to be
     * automatically applied to players on join. <br>
     * The provided file must exist.
     *
     * @param file        the file
     * @param applyOnJoin whether to automatically apply the pack to players when
     *                    they join
     * @return a PackProvider for the given file, or null if an error occurs
     */
    public PackProvider createProvider(File file, boolean applyOnJoin) {
        Preconditions.checkArgument(file.exists(), "The provided file does not exist");

        PackData data;
        try {
            data = readData(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ResourcePackException("Failed to create resoruce pack provider", e);
        }

        UUID id = UUID.nameUUIDFromBytes(data.hash());
        String packUrl = "%s/%s".formatted(this.url, id.toString());
        PackProvider provider = new PackProvider(id, packUrl, file.toPath(), data);
        this.availablePacks.put(id, provider);
        if (applyOnJoin) {
            this.listeners.addOnJoinProvider(provider);
        }

        return provider;
    }

    /**
     * Removes and unregisters a {@link PackProvider} with the given UUID if one
     * exists.
     *
     * @param id the UUID
     * @return true if an existing provider was removed, false otherwise
     */
    public boolean removeProvider(UUID id) {
        PackProvider provider = getProvider(id);
        if (provider != null) {
            return removeProvider(provider);
        }
        return false;
    }

    /**
     * Removes and unregisters the given {@link PackProvider}.
     *
     * @param provider the provider
     * @return true if an existing provider was removed, false otherwise
     */
    public boolean removeProvider(PackProvider provider) {
        this.listeners.removeOnJoinProvider(provider);
        return this.availablePacks.remove(provider.getUUID()) != null;
    }

    /**
     * Gets a {@link PackProvider} from a UUID if one exists.
     *
     * @param id the UUID
     * @return the provider with the given UUID, or null if none exists
     */
    public PackProvider getProvider(UUID id) {
        return this.availablePacks.get(id);
    }

    /**
     * Gets an immutable collection of all registered {@link PackProvider}.
     *
     * @return a collection of providers, will be empty if none exist
     */
    public Collection<PackProvider> getProviders() {
        return Collections.unmodifiableCollection(this.availablePacks.values());
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
            String idString = exchange.getRequestURI().toString().substring(1);
            UUID id;
            try {
                id = UUID.fromString(idString);
            } catch (IllegalArgumentException e) {
                exchange.sendResponseHeaders(404, -1);
                this.plugin.getLogger().log(Level.WARNING, "Recieved a request for an invalid resource pack id: {0}", idString);
                return;
            }

            PackProvider provider = this.availablePacks.get(id);
            if (provider == null) {
                exchange.sendResponseHeaders(404, -1);
                this.plugin.getLogger().log(Level.WARNING, "Recieved a request for an unknown resource pack id: {0}", id);
                return;
            }

            exchange.sendResponseHeaders(200, provider.getSize());
            Files.copy(provider.getPath(), exchange.getResponseBody());
        } catch (Exception e) {
            throw new ResourcePackException("An exception occured while processing a resource pack request", e);
        }
    }

    private PackData readData(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fileStream = new FileInputStream(file);
                DigestInputStream stream = new DigestInputStream(fileStream, digest)) {
            int size = stream.readAllBytes().length;

            return new PackData(digest.digest(), size);
        }
    }
}
