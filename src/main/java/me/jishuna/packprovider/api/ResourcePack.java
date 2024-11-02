package me.jishuna.packprovider.api;

import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class ResourcePack {
    private final String uri;
    private final UUID uuid;
    private final byte[] hash;
    private final File file;

    public ResourcePack(String uri, byte[] hash, File file) {
        this.uuid = UUID.nameUUIDFromBytes(hash);
        this.hash = hash;
        this.uri = uri + this.uuid;
        this.file = file;

        System.out.println(this.uri);
    }

    public void send(Player player) {
        send(player, null, false);
    }

    public void send(Player player, String prompt) {
        send(player, prompt, false);
    }

    public void send(Player player, boolean force) {
        send(player, null, force);
    }

    public void send(Player player, String prompt, boolean force) {
        player.addResourcePack(this.uuid, this.uri, this.hash, prompt, force);
    }

    public void remove(Player player) {
        player.removeResourcePack(this.uuid);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public File getFile() {
        return file;
    }
}
