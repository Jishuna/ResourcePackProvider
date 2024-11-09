package me.jishuna.packprovider.api;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class ResourcePackStatusChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ResourcePack pack;
    private final Status previous;
    private final Status newStatus;

    public ResourcePackStatusChangeEvent(Player player, ResourcePack pack, Status previous, Status newStatus) {
        super(player);
        this.pack = pack;
        this.previous = previous;
        this.newStatus = newStatus;
    }

    public ResourcePack getPack() {
        return pack;
    }

    public Status getNewStatus() {
        return newStatus;
    }

    public Status getPreviousStatus() {
        return previous;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
