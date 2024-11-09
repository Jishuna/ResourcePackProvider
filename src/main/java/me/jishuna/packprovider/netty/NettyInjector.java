package me.jishuna.packprovider.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import me.jishuna.packprovider.api.PackProvider;
import me.jishuna.packprovider.reflection.FieldAccess;
import me.jishuna.packprovider.reflection.ReflectionException;
import me.jishuna.packprovider.reflection.ReflectionHelper;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class NettyInjector {
    private static final Class<?> serverClass = ReflectionHelper.getClass("net.minecraft.server.MinecraftServer");
    private static final Class<?> serverConnectionClass = ReflectionHelper.getClass("net.minecraft.server.network.ServerConnection");
    private static final Class<?> craftServerClass = Bukkit.getServer().getClass();
    private static final FieldAccess<?> serverField = ReflectionHelper.getField(craftServerClass, serverClass, 0);
    private static final FieldAccess<?> serverConnectionField = ReflectionHelper.getField(serverClass, serverConnectionClass, 0);
    private static final FieldAccess<?> channelFutureListField = ReflectionHelper.getField(serverConnectionClass, List.class, 0);

    private final List<ChannelFuture> serverChannels = new ArrayList<>();
    private final PackProvider provider;
    private final String injectorName;

    public NettyInjector(PackProvider provider, String name) {
        this.provider = provider;
        this.injectorName = "RPP-%s-Injector".formatted(name);
    }

    public void inject() {
        try {
            injectChannelHandler();
        } catch (Exception e) {
            throw new ReflectionException("Netty injection failed", e);
        }
    }

    public void cleanup() {
        for (ChannelFuture channelFuture : serverChannels) {
            ChannelPipeline pipeline = channelFuture.channel().pipeline();
            if (pipeline.context(injectorName) != null) {
                pipeline.remove(injectorName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void injectChannelHandler() {
        Object server = serverField.read(Bukkit.getServer());
        Object serverConnection = serverConnectionField.read(server);

        List<ChannelFuture> connectionChannels = (List<ChannelFuture>) channelFutureListField.read(serverConnection);
        connectionChannels.forEach(channelFuture -> {

            ChannelPipeline pipeline = channelFuture.channel().pipeline();
            if (pipeline.context(injectorName) != null) {
                pipeline.remove(injectorName);
            }
            channelFuture.channel().pipeline().addFirst(injectorName, new ServerChannelInjector(provider));
            serverChannels.add(channelFuture);
        });
    }
}
