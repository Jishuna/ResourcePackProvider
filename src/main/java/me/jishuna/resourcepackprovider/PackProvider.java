package me.jishuna.resourcepackprovider;

import java.util.UUID;
import org.bukkit.entity.Player;

public class PackProvider {
    private final String url;
    private final UUID uuid;
    private final byte[] bytes;
    private final byte[] hash;

    public PackProvider(String url, byte[] bytes, byte[] hash) {
        this.url = url;
        this.uuid = UUID.nameUUIDFromBytes(hash);
        this.bytes = bytes;
        this.hash = hash;
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
        if (ResourcePackProvider.IS_NEW_SYSTEM) {
            player.setResourcePack(this.uuid, this.url, this.hash, prompt, force);
        } else {
            player.setResourcePack(this.url, this.hash, prompt, force);
        }
    }

    public void remove(Player player) {
        if (ResourcePackProvider.IS_NEW_SYSTEM) {
            player.removeResourcePack(this.uuid);
        }
    }

    public UUID getUUID() {
        return this.uuid;
    }

    protected byte[] getBytes() {
        return this.bytes;
    }
}
