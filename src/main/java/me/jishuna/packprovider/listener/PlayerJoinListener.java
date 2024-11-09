package me.jishuna.packprovider.listener;

import me.jishuna.packprovider.api.PackProvider;
import me.jishuna.packprovider.api.ResourcePack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class PlayerJoinListener implements Listener {
    private final PackProvider provider;

    public PlayerJoinListener(PackProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (ResourcePack pack : provider) {
            if (pack.isSendOnJoin()) {
                provider.log(Level.INFO, "Automatically sending pack \"{0}\" to player \"{1}\"",
                        pack.getPath().getFileName().toString(), event.getPlayer().getName());
                pack.send(player);
            }
        }
    }
}
