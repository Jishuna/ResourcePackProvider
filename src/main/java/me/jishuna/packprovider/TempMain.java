package me.jishuna.packprovider;

import me.jishuna.packprovider.api.PackProvider;
import me.jishuna.packprovider.api.PackProviderException;
import me.jishuna.packprovider.api.ResourcePackStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TempMain extends JavaPlugin implements Listener {
    private PackProvider provider;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            provider = PackProvider.create(this, "localhost");
            provider.setDebugLogging(true);
            provider.addPack(new File(getDataFolder(), "test.zip").toPath()).setSendOnJoin(true);
            provider.addPack(new File(getDataFolder(), "test2.zip").toPath()).setSendOnJoin(true);
        } catch (PackProviderException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onStatusChange(ResourcePackStatusChangeEvent event) {
        System.out.printf("%s: %s -> %s%n", event.getPlayer().getName(), event.getPreviousStatus(), event.getNewStatus());
    }
}
