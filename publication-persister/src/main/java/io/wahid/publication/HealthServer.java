package io.wahid.publication;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HealthServer {
    private HealthServer() {}
    public static void start() throws IOException {
        System.out.println("Checking health....");
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8088), 0);
        server.createContext("/health", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
                System.out.println("✅ Sending ok response!");
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("✅ Health endpoint running on http://localhost:8088/health");
    }
}
