package com.project.handlers;

import com.project.client.Client;
import com.project.server.Config;
import com.project.store.LocalKvStore;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutHandler implements HttpHandler {


    private final LocalKvStore kvStore = LocalKvStore.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        System.out.println("Receiving put call "+path);
        byte[] response = "SERVER_ERROR, CAN'T PROCESS THIS REQUEST TYPE".getBytes();
        int responseCode = 503;
        if (path == null) {
            response = "INVALID_REQUEST".getBytes();
            responseCode = 403;
        } else {
            String[] s = path.split("/");
            if (s.length < 3 || s[2].isEmpty()) {
                response = "NOT_FOUND".getBytes();
                responseCode = 404;
            } else {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
                Headers headers = exchange.getRequestHeaders();
                String requestOrigin = "";
                if (headers.containsKey("sources")) {
                    requestOrigin = headers.getFirst("sources");
                }
                BufferedReader br = new BufferedReader(isr);
                int b;
                StringBuilder buf = new StringBuilder(512);
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }
                br.close();
                isr.close();
                String key = s[2];
                String val = buf.toString();
                kvStore.put(key, val);
                if (!val.isEmpty() && kvStore.put(key, val)) {
                    response = "OK".getBytes();
                    responseCode = 200;
                    if (Config.getConfig().getReplicationFactor() != 0 && !Config.getConfig().getPeers().contains(requestOrigin)) {
                        int rf = Math.min(Config.getConfig().getPeers().size(), Config.getConfig().getReplicationFactor());
                        Client client = new Client();
                        List<String> peers = new ArrayList<>(Config.getConfig().getPeers());
                        Map<String,String> a = new HashMap<>();;
                        a.put(key,val);
                        int index = peers.size() - 1;
                        while (rf > 0 && index > 0) {
                            try {
                                client.batchPut(a, "http://" + peers.get(index - 1) + "/set/batch");
                                rf--;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            index--;
                        }
                        if (rf == Math.min(Config.getConfig().getPeers().size(), Config.getConfig().getReplicationFactor()) && index == 0 && Config.getConfig().getReplicationFactor() != 0) {
                            response = "Replication failed, inserted in only this server".getBytes();
                        }
                    }

                }
            }

        }
        exchange.sendResponseHeaders(responseCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.flush();
        os.close();
        System.out.println("Finished put call "+path);
    }



}
