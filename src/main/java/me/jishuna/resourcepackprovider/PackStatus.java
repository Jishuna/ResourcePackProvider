package me.jishuna.resourcepackprovider;

import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

/**
 * Represents the status of a resource pack for a player.
 */
public enum PackStatus {
    /**
     * The resource pack has been successfully downloaded and applied to the client.
     */
    SUCCESSFULLY_LOADED,
    /**
     * The client refused to accept the resource pack.
     */
    DECLINED,
    /**
     * The client accepted the pack, but download failed.
     */
    FAILED_DOWNLOAD,
    /**
     * The client accepted the pack and is beginning a download of it.
     */
    ACCEPTED,
    /**
     * The client successfully downloaded the pack.
     */
    DOWNLOADED,
    /**
     * The pack URL was invalid.
     */
    INVALID_URL,
    /**
     * The client was unable to reload the pack.
     */
    FAILED_RELOAD,
    /**
     * The pack was discarded by the client.
     */
    DISCARDED,
    /**
     * The status of the client is unknown, they likely have not been sent the pack
     * yet.
     */
    UNKNOWN;

    private static final PackStatus[] CACHE = PackStatus.values();

    public static PackStatus fromBukkit(Status status) {
        return CACHE[status.ordinal()];
    }
}
