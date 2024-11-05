package me.jishuna.packprovider.api;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

/**
 * Represents a hosted resource pack that can be sent to players
 */
public class ResourcePack {
    private final String uri;
    private final UUID uuid;
    private final byte[] hash;
    private final File file;

    ResourcePack(String uri, byte[] hash, File file) {
        this.uuid = UUID.nameUUIDFromBytes(hash);
        this.hash = hash;
        this.uri = uri + this.uuid;
        this.file = file;
    }

    /**
     * Sends this resource pack to the provided player., uses the default prompt message and does not force the client to use the pack.
     * @param player the player to send to
     */
    public void send(@NotNull Player player) {
        send(player, null, false);
    }

    /**
     * Sends this resource pack to the provided player., does not force the client to use the pack.
     * @param player the player to send to
     * @param prompt the custom prompt to show the player before downloading
     */
    public void send(@NotNull Player player, String prompt) {
        send(player, prompt, false);
    }

    /**
     * Sends this resource pack to the provided player., uses the default prompt message.
     * @param player the player to send to
     * @param force whether to force the client to accept the pack or disconnect from the server
     */
    public void send(@NotNull Player player, boolean force) {
        send(player, null, force);
    }

    /**
     * Sends this resource pack to the provided player.
     * @param player the player to send to
     * @param prompt the custom prompt to show the player before downloading
     * @param force whether to force the client to accept the pack or disconnect from the server
     */
    public void send(@NotNull Player player, @Nullable String prompt, boolean force) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        player.addResourcePack(this.uuid, this.uri, this.hash, prompt, force);
    }

    /**
     * Removes this resource pack from the provided player, if they are using it.
     * @param player
     */
    public void remove(@NotNull Player player) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        player.removeResourcePack(this.uuid);
    }

    /**
     * Gets the UUID of this resource pack.
     * @return the UUID
     */
    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    /**
     * Gets the file of this resource pack.
     * @return the file
     */
    public @NotNull File getFile() {
        return file;
    }
}
