package me.jishuna.packprovider.listener;

import me.jishuna.packprovider.api.PackProvider;
import me.jishuna.packprovider.api.ResourcePack;
import me.jishuna.packprovider.api.Status;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackStatusListener implements Listener {
    private final PackProvider provider;

    public ResourcePackStatusListener(PackProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPackStatusEvent(PlayerResourcePackStatusEvent event) {
        ResourcePack pack = provider.getPack(event.getID().toString());
        if (pack == null) {
            return;
        }

        pack.updateStatus(event.getPlayer(), Status.fromBukkit(event.getStatus()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        for (ResourcePack pack : provider) {
            pack.updateStatus(event.getPlayer(), Status.UNKNOWN);
        }
    }
}
