package me.jishuna.packprovider.api;

import com.google.common.base.Preconditions;
import me.jishuna.packprovider.netty.NettyInjector;
import org.bukkit.plugin.Plugin;

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

public class PackProvider implements Iterable<ResourcePack> {
    public static PackProvider create(Plugin plugin) throws PackProviderException {
        try {
            PackProvider provider = new PackProvider(plugin);
            provider.injector.inject();

            return provider;
        } catch (Exception e) {
            throw new PackProviderException("Failed to create pack provider", e);
        }
    }

    private final ConcurrentMap<String, ResourcePack> availablePacks = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final NettyInjector injector;

    private PackProvider(Plugin plugin) {
        this.plugin = plugin;
        this.injector = new NettyInjector(this);
    }

    public ResourcePack addPack(File file) {
        Preconditions.checkArgument(file != null, "The provided file cannot be null");
        Preconditions.checkArgument(file.exists(), "The provided file does not exist");
        Preconditions.checkArgument(!file.isDirectory(), "The provided file cannot be a directory");

        byte[] hash;
        try {
            hash = getHash(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ReflectionException("Failed to add pack " + file.getName(), e);
        }

        ResourcePack pack = new ResourcePack("http://localhost:25565/packs/", hash, file);
        availablePacks.put(pack.getUUID().toString(), pack);
        return pack;
    }

    public ResourcePack getPack(String id) {
        return availablePacks.get(id);
    }

    public Plugin getPlugin() {
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
    public Iterator<ResourcePack> iterator() {
        return Collections.unmodifiableCollection(availablePacks.values()).iterator();
    }
}
