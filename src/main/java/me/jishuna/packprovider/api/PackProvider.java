package me.jishuna.packprovider.api;

import com.google.common.base.Preconditions;
import me.jishuna.packprovider.ServerIpResolver;
import me.jishuna.packprovider.netty.NettyInjector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    /**
     * Creates a new PackProvider for the given plugin. The server ip and port will be determined automatically.
     *
     * @param plugin the plugin instance
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin) throws PackProviderException {
        String ip = ServerIpResolver.getServerIp();
        if (ip == null || ip.isBlank()) {
            throw new PackProviderException("Failed to automatically determine server IP");
        }
        int port = Bukkit.getPort();

        return create(plugin, ip, port);
    }

    /**
     * Creates a new PackProvider for the given plugin. The server port will be determined automatically.
     *
     * @param plugin the plugin instance
     * @param serverIp the public server ip to send requests to
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin, @NotNull String serverIp) throws PackProviderException {
        int port = Bukkit.getPort();

        return create(plugin, serverIp, port);
    }

    /**
     * Creates a new PackProvider for the given plugin. The server ip will be determined automatically.
     *
     * @param plugin the plugin instance
     * @param port the port to send requests to
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin, int port) throws PackProviderException {
        String ip = ServerIpResolver.getServerIp();
        if (ip == null || ip.isBlank()) {
            throw new PackProviderException("Failed to automatically determine server IP");
        }

        return create(plugin, ip, port);
    }

    /**
     * Creates a new PackProvider for the given plugin.
     *
     * @param plugin the plugin instance
     * @param serverIp the public server ip to send requests to
     * @param port the port to send requests to
     * @return a PackProvider instance that has been injected and is ready to use
     * @throws PackProviderException if something goes wrong creating or injecting the PackProvider
     */
    public static @NotNull PackProvider create(@NotNull Plugin plugin, @NotNull String serverIp, int port) throws PackProviderException {
        Preconditions.checkArgument(plugin != null, "The provided plugin cannot be null");
        Preconditions.checkArgument(serverIp != null, "The provided serverIp cannot be null");
        Preconditions.checkArgument(!serverIp.isBlank(), "The provided serverIp cannot be blank");

        try {
            PackProvider provider = new PackProvider(plugin, serverIp, port);
            provider.injector.inject();

            plugin.getLogger().log(Level.INFO, "Started PackProvider with URI {0}", provider.requestUrl);

            return provider;
        } catch (Exception e) {
            throw new PackProviderException("Failed to create pack provider", e);
        }
    }

    private final ConcurrentMap<String, ResourcePack> availablePacks = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final NettyInjector injector;
    private final String requestUrl;

    private PackProvider(Plugin plugin, String serverIp, int port) {
        this.plugin = plugin;
        this.injector = new NettyInjector(this);

        this.requestUrl = "http://%s:%s/packs/".formatted(serverIp, port);
    }

    /**
     * Adds a resource pack to this PackProvider, allowing it to be sent to players.
     * @param file the zip file for the resource pack
     * @return a new {@link ResourcePack} instance
     */
    public @NotNull ResourcePack addPack(@NotNull File file) {
        Preconditions.checkArgument(file != null, "The provided file cannot be null");
        Preconditions.checkArgument(file.exists(), "The provided file does not exist");
        Preconditions.checkArgument(!file.isDirectory(), "The provided file cannot be a directory");

        byte[] hash;
        try {
            hash = getHash(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ReflectionException("Failed to add pack " + file.getName(), e);
        }

        ResourcePack pack = new ResourcePack(requestUrl, hash, file);
        availablePacks.put(pack.getUUID().toString(), pack);
        return pack;
    }

    /**
     * Gets the {@link ResourcePack} with the given id, if one has been registered to this pack provider.
     * @param id the id
     * @return The resource pack for the given id, or null
     */
    public @Nullable ResourcePack getPack(@Nullable String id) {
        return availablePacks.get(id);
    }

    /**
     * Gets the plugin that owns this PackProvider
     * @return the owning plugin
     */
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    private byte[] getHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fileStream = new FileInputStream(file);
             DigestInputStream stream = new DigestInputStream(fileStream, digest)) {
            stream.readAllBytes();

            return digest.digest();
        }
    }

    @Override
    public @NotNull Iterator<ResourcePack> iterator() {
        return Collections.unmodifiableCollection(availablePacks.values()).iterator();
    }
}
