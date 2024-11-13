package me.jishuna.packprovider.api;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a hosted resource pack that can be sent to players
 */
public class ResourcePack {
    private final ConcurrentMap<UUID, Status> playerStatus = new ConcurrentHashMap<>();
    private final String uri;
    private final UUID uuid;
    private final byte[] hash;
    private final Path path;
    private boolean sendOnJoin;

    ResourcePack(String uri, byte[] hash, Path path) {
        this.uuid = UUID.nameUUIDFromBytes(hash);
        this.hash = hash;
        this.uri = uri + this.uuid;
        this.path = path;
    }

    /**
     * Sends this resource pack to the provided player., uses the default prompt message and does not force the client to use the pack.
     *
     * @param player the player to send to
     */
    public void send(@NotNull Player player) {
        send(player, null, false);
    }

    /**
     * Sends this resource pack to the provided player., does not force the client to use the pack.
     *
     * @param player the player to send to
     * @param prompt the custom prompt to show the player before downloading
     */
    public void send(@NotNull Player player, String prompt) {
        send(player, prompt, false);
    }

    /**
     * Sends this resource pack to the provided player., uses the default prompt message.
     *
     * @param player the player to send to
     * @param force  whether to force the client to accept the pack or disconnect from the server
     */
    public void send(@NotNull Player player, boolean force) {
        send(player, null, force);
    }

    /**
     * Sends this resource pack to the provided player.
     *
     * @param player the player to send to
     * @param prompt the custom prompt to show the player before downloading
     * @param force  whether to force the client to accept the pack or disconnect from the server
     */
    public void send(@NotNull Player player, @Nullable String prompt, boolean force) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        player.addResourcePack(this.uuid, this.uri, this.hash, prompt, force);
    }

    /**
     * Removes this resource pack from the provided player, if they are using it.
     *
     * @param player
     */
    public void remove(@NotNull Player player) {
        Preconditions.checkArgument(player != null, "Player cannot be null");

        player.removeResourcePack(this.uuid);
        resetStatus(player);
    }

    public Status getStatus(Player player) {
        return playerStatus.getOrDefault(player.getUniqueId(), Status.UNKNOWN);
    }

    public void updateStatus(Player player, Status status) {
        Status currentStatus = getStatus(player);
        if (currentStatus == status) {
            return;
        }

        ResourcePackStatusChangeEvent event = new ResourcePackStatusChangeEvent(player, this, currentStatus, status);
        Bukkit.getPluginManager().callEvent(event);

        playerStatus.put(player.getUniqueId(), status);
    }

    public boolean isSendOnJoin() {
        return sendOnJoin;
    }

    public void setSendOnJoin(boolean sendOnJoin) {
        this.sendOnJoin = sendOnJoin;
    }

    /**
     * Gets the UUID of this resource pack.
     *
     * @return the UUID
     */
    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    /**
     * Gets the path of the file for this resource pack.
     *
     * @return the path
     */
    public @NotNull Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourcePack that = (ResourcePack) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, path);
    }
}
