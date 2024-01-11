package me.jishuna.resourcepackprovider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Represents a resource pack that can be sent to clients using the running
 * {@link PackServer}.
 */
public class PackProvider {
    private final Map<UUID, PackStatus> packStatus = new HashMap<>();

    private final UUID uuid;
    private final String url;
    private final Path path;
    private final byte[] hash;
    private final int size;

    protected PackProvider(UUID id, String url, Path path, PackData data) {
        this.uuid = id;
        this.url = url;
        this.path = path;
        this.hash = data.hash();
        this.size = data.size();
    }

    /**
     * Send the resource pack represented by this PackProvider to the given player.
     * <br>
     * On versions prior to 1.20.3 this will override any existing packs that have
     * been sent to the player.
     *
     * @param player the player
     */
    public void send(Player player) {
        send(player, null, false);
    }

    /**
     * Send the resource pack represented by this PackProvider to the given player.
     * <br>
     * On versions prior to 1.20.3 this will override any existing packs that have
     * been sent to the player.
     *
     * @param player the player
     * @param prompt the custom prompt message to show to the client
     */
    public void send(Player player, String prompt) {
        send(player, prompt, false);
    }

    /**
     * Send the resource pack represented by this PackProvider to the given player.
     * <br>
     * On versions prior to 1.20.3 this will override any existing packs that have
     * been sent to the player.
     *
     * @param player the player
     * @param force  when true the client will be kicked from the server if they do
     *               not successfully load the pack
     */
    public void send(Player player, boolean force) {
        send(player, null, force);
    }

    /**
     * Send the resource pack represented by this PackProvider to the given player.
     * <br>
     * On versions prior to 1.20.3 this will override any existing packs that have
     * been sent to the player.
     *
     * @param player the player
     * @param prompt the custom prompt message to show to the client
     * @param force  when true the client will be kicked from the server if they do
     *               not successfully load the pack
     */
    public void send(Player player, String prompt, boolean force) {
        if (PackUtils.IS_NEW_SYSTEM) {
            player.setResourcePack(this.uuid, this.url, this.hash, prompt, force);
        } else {
            player.setResourcePack(this.url, this.hash, prompt, force);
        }
    }

    /**
     * Remove the resource pack represented by this PackProvider from the given
     * player. <br>
     * On versions prior to 1.20.3 this method will have no effect.
     *
     * @param player the player
     */
    public void remove(Player player) {
        if (PackUtils.IS_NEW_SYSTEM) {
            player.removeResourcePack(this.uuid);
            clearStatus(player);
        }
    }

    /**
     * Gets the status of the resource pack represented by this PackProvider for the
     * given player. <br>
     * On versions prior to 1.20.3 this will always return
     * {@link PackStatus#UNKNOWN}.
     *
     * @param player the player
     * @return the status of this pack for the given player
     */
    public PackStatus getStatus(Player player) {
        return this.packStatus.getOrDefault(player.getUniqueId(), PackStatus.UNKNOWN);
    }

    /**
     * Gets the unique id of this PackProvider.
     *
     * @return the unique id
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Gets the path to the file this PackProvider represents.
     *
     * @return the path
     */
    public Path getPath() {
        return this.path;
    }

    protected void setStatus(Player player, PackStatus status) {
        this.packStatus.put(player.getUniqueId(), status);
    }

    protected void clearStatus(Player player) {
        this.packStatus.remove(player.getUniqueId());
    }

    protected int getSize() {
        return this.size;
    }
}
