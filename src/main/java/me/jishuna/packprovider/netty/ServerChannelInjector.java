package me.jishuna.packprovider.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.jishuna.packprovider.api.PackProvider;
import org.jetbrains.annotations.NotNull;

public class ServerChannelInjector extends ChannelInboundHandlerAdapter {
    private final PackProvider provider;

    ServerChannelInjector(PackProvider provider) {
        this.provider = provider;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        Channel channel = (Channel) msg;
        channel.pipeline().addLast(new PreChannelInitializer(provider));

        super.channelRead(ctx, msg);
    }
}
