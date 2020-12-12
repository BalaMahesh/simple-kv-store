package com.project.handlers;

import com.project.store.LocalKvStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

public class BatchPutHandler implements HttpHandler {

    LocalKvStore kVStore = LocalKvStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        byte[] response = "SERVER_ERROR, CAN'T PROCESS THIS REQUEST TYPE".getBytes();
        int responseCode = 503;
        if (path == null){
            response = "INVALID_REQUEST".getBytes();
            responseCode = 403;
        }else {
            String[] s = path.split("/");
            if (s.length < 3 || s[2].isEmpty()) {
                response = "NOT_FOUND".getBytes();
                responseCode = 404;
            } else {
                InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(),"utf-8");
                BufferedReader br = new BufferedReader(isr);
                int b;
                StringBuilder buf = new StringBuilder(1024);
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }
                br.close();
                isr.close();
                String input = buf.toString();
                if (input.isEmpty()){
                    response = "EMPTY_BATCH".getBytes();
                    responseCode = 202;
                }else {
                    String[] kvs = input.split(",");
                    System.out.println("Batch received "+ Arrays.toString(kvs));
                    Arrays.stream(kvs).parallel().map(kv -> kv.split(":")).forEach(k -> kVStore.update(k[0], k[1]));
                    response = "OK".getBytes();
                    responseCode = 200;
                }
            }
        }
        exchange.sendResponseHeaders(responseCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}
