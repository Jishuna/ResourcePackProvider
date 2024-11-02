package me.jishuna.packprovider;

import me.jishuna.packprovider.api.PackProvider;
import me.jishuna.packprovider.api.PackProviderException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TempMain extends JavaPlugin implements Listener {
    private PackProvider provider;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            provider = PackProvider.create(this);
            provider.addPack(new File(getDataFolder(), "test.zip"));
            provider.addPack(new File(getDataFolder(), "test2.zip"));
        } catch (PackProviderException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        provider.forEach(pack -> pack.send(event.getPlayer()));
    }
}
