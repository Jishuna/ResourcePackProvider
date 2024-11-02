package me.jishuna.packprovider;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    public static HttpRequest parse(ByteBuf buf) {
        try (ByteBufInputStream stream = new ByteBufInputStream(buf)) {
            return parse(stream);
        } catch (IOException e) {
            throw new RuntimeException("Temp");
        }
    }

    public static HttpRequest parse(InputStream stream) {
        try (InputStreamReader streamReader = new InputStreamReader(stream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String request = reader.readLine();
            Map<String, String> headers = readHeaders(reader);

            return new HttpRequest(request, headers);
        } catch (IOException e) {
            throw new RuntimeException("Temp");
        }
    }

    private static Map<String, String> readHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();

        String header = reader.readLine();
        while (header != null && !header.isEmpty()) {
            int split = header.indexOf(':');
            if (split < 0) {
                throw new RuntimeException("Temp");
            }

            headers.put(header.substring(0, split), header.substring(split + 1).trim());
            header = reader.readLine();
        }
        return headers;
    }

    private final String requestMethod;
    private final String requestURI;
    private final String protocolVersion;
    private final Map<String, String> headers;

    private HttpRequest(String request, Map<String, String> headers) {
        this.headers = headers;

        // TODO validate
        String[] requestParts = request.split(" ");
        this.requestMethod = requestParts[0];
        this.requestURI = requestParts[1];
        this.protocolVersion = requestParts[2];
    }

    public String getRequestMethod() {
        return this.requestMethod;
    }

    public String getRequestURI() {
        return this.requestURI;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public String getHeaderValue(String name) {
        return this.headers.get(name);
    }

    public void temp() {
        this.headers.forEach((k, v) -> System.out.println(k + " : " + v));
    }
}
