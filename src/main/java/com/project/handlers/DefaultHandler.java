package com.project.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class DefaultHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        System.out.println(path);
        byte[] response = "Health check".getBytes();
        int responseCode = 200;
        try {
            exchange.sendResponseHeaders(responseCode, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
