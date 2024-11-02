package me.jishuna.packprovider.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import me.jishuna.packprovider.api.PackProvider;

public class PreChannelInitializer extends ChannelInboundHandlerAdapter {
    private final PackProvider provider;

    public PreChannelInitializer(PackProvider provider) {
        this.provider = provider;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.channel().pipeline();
        synchronized (ctx.channel()) {
            pipeline.addFirst(new HTTPRequestHandler(provider));
        }
        if (pipeline.context(this) != null) {
            pipeline.remove(this);
        }
        pipeline.fireChannelRegistered();
    }
}
