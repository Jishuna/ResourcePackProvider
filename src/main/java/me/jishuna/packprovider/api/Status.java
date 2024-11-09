package me.jishuna.packprovider.api;

import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum Status {
    UNKNOWN(null),
    SUCCESSFULLY_LOADED(PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED),
    DECLINED(PlayerResourcePackStatusEvent.Status.DECLINED),
    FAILED_DOWNLOAD(PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD),
    ACCEPTED(PlayerResourcePackStatusEvent.Status.ACCEPTED),
    DOWNLOADED(PlayerResourcePackStatusEvent.Status.DOWNLOADED),
    INVALID_URL(PlayerResourcePackStatusEvent.Status.INVALID_URL),
    FAILED_RELOAD(PlayerResourcePackStatusEvent.Status.FAILED_RELOAD),
    DISCARDED(PlayerResourcePackStatusEvent.Status.DISCARDED);

    private static final ConcurrentMap<PlayerResourcePackStatusEvent.Status, Status> lookup = new ConcurrentHashMap<>();

    static {
        for (Status status : Status.values()) {
            if (status.bukkitStatus != null) {
                lookup.put(status.bukkitStatus, status);
            }
        }
    }

    public static Status fromBukkit(PlayerResourcePackStatusEvent.Status bukkitStatus) {
        return lookup.getOrDefault(bukkitStatus, UNKNOWN);
    }

    private final PlayerResourcePackStatusEvent.Status bukkitStatus;

    Status(PlayerResourcePackStatusEvent.Status bukkitStatus) {
        this.bukkitStatus = bukkitStatus;
    }
}
