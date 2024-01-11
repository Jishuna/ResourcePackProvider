package me.jishuna.resourcepackprovider;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

final class PackListeners implements Listener {
    private final Set<PackProvider> applyOnJoin = new HashSet<>();
    private final PackServer server;

    public PackListeners(PackServer server) {
        this.server = server;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.applyOnJoin.forEach(provider -> provider.send(event.getPlayer()));
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!PackUtils.IS_NEW_SYSTEM) {
            return;
        }

        PackProvider provider = this.server.getProvider(event.getID());
        if (provider == null) {
            return;
        }

        provider.setStatus(event.getPlayer(), PackStatus.fromBukkit(event.getStatus()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.server.getProviders().forEach(provider -> provider.clearStatus(event.getPlayer()));
    }

    public void addOnJoinProvider(PackProvider provider) {
        this.applyOnJoin.add(provider);
    }

    public void removeOnJoinProvider(PackProvider provider) {
        this.applyOnJoin.remove(provider);
    }
}
