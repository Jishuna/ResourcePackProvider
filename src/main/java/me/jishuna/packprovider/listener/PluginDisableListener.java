package me.jishuna.packprovider.listener;

import me.jishuna.packprovider.api.PackProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class PluginDisableListener implements Listener {
    private final PackProvider provider;
    private final Plugin owner;

    public PluginDisableListener(PackProvider provider, Plugin owner) {
        this.provider = provider;
        this.owner = owner;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(owner)) {
            provider.discard();
        }
    }
}
