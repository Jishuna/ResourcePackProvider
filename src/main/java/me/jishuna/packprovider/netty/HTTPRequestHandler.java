package me.jishuna.packprovider.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import me.jishuna.packprovider.HTTPException;
import me.jishuna.packprovider.HttpRequest;
import me.jishuna.packprovider.api.PackProvider;
import me.jishuna.packprovider.api.ResourcePack;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;

public class HTTPRequestHandler extends ChannelDuplexHandler {
    private final PackProvider provider;

    HTTPRequestHandler(PackProvider provider) {
        this.provider = provider;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;

        if (!isGetRequest(in) || !handleGetRequest(ctx, in)) {
            super.channelRead(ctx, msg);
        }
    }

    private boolean handleGetRequest(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        HttpRequest request;
        try {
            request = HttpRequest.parse(in);
        } catch (HTTPException e) {
            provider.log(Level.INFO, "Encountered an unexpected exception while parsing HTTP request");
            e.printStackTrace();
            return false;
        }

        if (!request.getRequestURI().startsWith("/packs/")) {
            provider.log(Level.INFO, "Ignoring request to invalid URI \"{0}\"", request.getRequestURI());
            return false;
        }

        if (request.getHeaderValue("X-Minecraft-UUID") == null || request.getHeaderValue("X-Minecraft-Username") == null) {
            provider.log(Level.INFO, "Ignoring request without X-Minecraft-UUID or X-Minecraft-Username headers");
            return false;
        }

        String packId = request.getRequestURI().substring(7);
        ResourcePack pack = provider.getPack(packId);
        if (pack == null) {
            return false;
        }

        provider.log(Level.INFO, "Received request for \"{0}\" from \"{1}\"", pack.getPath().getFileName().toString(), request.getHeaderValue("X-Minecraft-Username"));

        ByteBuf buf = Unpooled.buffer();
        buf.writeCharSequence("HTTP/1.1 200 OK\n\n", StandardCharsets.US_ASCII);
        buf.writeBytes(Files.readAllBytes(pack.getPath()));
        ctx.writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
        return true;
    }


    private boolean isGetRequest(ByteBuf buf) {
        int index = buf.readerIndex();
        return buf.getUnsignedByte(index) == 'G'
                && buf.getUnsignedByte(index + 1) == 'E'
                && buf.getUnsignedByte(index + 2) == 'T';
    }

}
