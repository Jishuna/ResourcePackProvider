package me.jishuna.packprovider.api;

import com.google.common.base.Preconditions;
import me.jishuna.packprovider.ServerIpResolver;
import me.jishuna.packprovider.listener.PlayerJoinListener;
import me.jishuna.packprovider.listener.PluginDisableListener;
import me.jishuna.packprovider.listener.ResourcePackStatusListener;
import me.jishuna.packprovider.netty.NettyInjector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * Represents a pack provider system that can host resource packs on the same port as the server.
 */
public class PackProvider implements Iterable<ResourcePack> {
    private static final ConcurrentMap<Plugin, PackProvider> providers = new ConcurrentHashMap<>();

    /**
     * Creates a new PackProvider for the given plugin. The server ip and port will be determined automatically.
     *
     * @param plugin the plugin instance
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin) throws PackProviderException {
        return create(plugin, null, -1);
    }

    /**
     * Creates a new PackProvider for the given plugin. The server port will be determined automatically.
     *
     * @param plugin   the plugin instance
     * @param serverIp the public server ip to send requests to
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin, @Nullable String serverIp) throws PackProviderException {
        return create(plugin, serverIp, -1);
    }

    /**
     * Creates a new PackProvider for the given plugin. The server ip will be determined automatically.
     *
     * @param plugin the plugin instance
     * @param port   the port to send requests to
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin, int port) throws PackProviderException {
        return create(plugin, null, port);
    }

    /**
     * Creates a new PackProvider for the given plugin.
     *
     * @param plugin   the plugin instance
     * @param serverIp the public server ip to send requests to
     * @param port     the port to send requests to
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin, @Nullable String serverIp, int port) throws PackProviderException {
        Preconditions.checkArgument(plugin != null, "The provided plugin cannot be null");
        PackProvider existing = providers.get(plugin);
        if (existing != null && !existing.discarded) {
            return existing;
        }

        try {
            if (serverIp == null || serverIp.isBlank()) {
                serverIp = ServerIpResolver.getServerIp();
            }

            if (port < 0) {
                port = Bukkit.getPort();
            }

            PackProvider provider = new PackProvider(plugin, serverIp, port);
            provider.injector.inject();

            providers.put(plugin, provider);
            return provider;
        } catch (Exception e) {
            throw new PackProviderException("Failed to create pack provider", e);
        }
    }

    private final ConcurrentMap<String, ResourcePack> availablePacks = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final NettyInjector injector;
    private final String requestUrl;
    private boolean debugLogging;
    private boolean discarded;

    private PackProvider(Plugin plugin, String serverIp, int port) {
        this.plugin = plugin;
        this.injector = new NettyInjector(this, plugin.getName());
        this.requestUrl = "http://%s:%s/packs/".formatted(serverIp, port);

        Bukkit.getPluginManager().registerEvents(new PluginDisableListener(this, plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new ResourcePackStatusListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), plugin);
    }

    public void discard() {
        checkDiscarded();
        injector.cleanup();
        discarded = true;

        providers.remove(plugin);
    }


    /**
     * Adds a resource pack to this PackProvider, allowing it to be sent to players.
     *
     * @param path the path to the zip file for this pack
     * @return a new {@link ResourcePack} instance
     */
    public @NotNull ResourcePack addPack(@NotNull Path path) {
        checkDiscarded();
        Preconditions.checkArgument(path != null, "The provided file cannot be null");
        Preconditions.checkArgument(Files.exists(path), "The provided file does not exist");
        Preconditions.checkArgument(!Files.isDirectory(path), "The provided file cannot be a directory");

        byte[] hash;
        try {
            hash = getHash(path);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ResourcePackException("Failed to add pack " + path.getFileName().toString(), e);
        }

        ResourcePack pack = new ResourcePack(requestUrl, hash, path);
        availablePacks.put(pack.getUUID().toString(), pack);
        log(Level.INFO, "Hosting pack \"{0}\" with id \"{1}\"", path.getFileName().toString(), pack.getUUID().toString());
        return pack;
    }

    /**
     * Gets the {@link ResourcePack} with the given id, if one has been registered to this pack provider.
     *
     * @param id the id
     * @return The resource pack for the given id, or null
     */
    public @Nullable ResourcePack getPack(@Nullable String id) {
        checkDiscarded();
        return availablePacks.get(id);
    }

    public void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    public void log(Level level, String message, Object... args) {
        checkDiscarded();
        if (level == Level.INFO && !debugLogging) {
            return;
        }

        plugin.getLogger().log(level, message, args);
    }

    @Override
    public @NotNull Iterator<ResourcePack> iterator() {
        checkDiscarded();
        return Collections.unmodifiableCollection(availablePacks.values()).iterator();
    }

    private byte[] getHash(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fileStream = Files.newInputStream(path);
             DigestInputStream stream = new DigestInputStream(fileStream, digest)) {
            stream.readAllBytes();

            return digest.digest();
        }
    }

    private void checkDiscarded() {
        if (discarded) {
            throw new IllegalStateException("discard() has already been called on this PackProvider");
        }
    }
}
