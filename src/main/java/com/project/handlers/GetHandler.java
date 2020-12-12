package com.project.handlers;

import com.project.client.Client;
import com.project.server.Config;
import com.project.store.LocalKvStore;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.*;

public class GetHandler implements HttpHandler {

    private final static LocalKvStore localKvStore = LocalKvStore.getInstance();
    private String requestSources = "";

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("sources")) {
            requestSources = headers.getFirst("sources");
        }
        byte[] response ;
        int responseCode ;
        if (path == null){
            response = "INVALID_REQUEST".getBytes();
            responseCode = 403;
        }else {
            String[] s = path.split("/");
            if (s.length < 2 || s[2].isEmpty()) {
                response = "NOT_FOUND".getBytes();
                responseCode = 404;
            } else {
                String key = s[2];
                String value = localKvStore.get(key);
                if (value == null) {
                    Optional<String> opt = checkAndGetFromPeers(key);
                    if (!opt.isPresent()) {
                        response = "NOT_FOUND".getBytes();
                        responseCode = 404;
                    }else{
                        response = opt.get().getBytes();
                        responseCode = 200;
                    }
                } else {
                    response = value.getBytes();
                    responseCode = 200;
                }
            }
        }
        exchange.sendResponseHeaders(responseCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private Optional<String> checkAndGetFromPeers(String key){
        Set<String> peersList = Config.getConfig().getPeers();
        System.out.println("Starting to check in other peers , count : "+peersList.size());
        ExecutorService service = Executors.newFixedThreadPool(Math.min(5,peersList.size()));
        List<Future<Optional<String>>> futures = new ArrayList<>();
        List<String> requestHosts = Arrays.asList(requestSources.split(","));
        requestHosts.stream().forEach(host->{if(!peersList.contains(host)) peersList.add(host);});
        for (String peer : peersList){
            if (requestSources.contains(peer)){
                continue;
            }
            futures.add(service.submit(new GetFuture(peer, key)));
        }
        System.out.println("Futures size: "+futures.size());
        if (futures.size() == 0){
            System.out.println("No peers left to check in server :"+Config.getConfig().getServerName());
        }
        for (Future future : futures){
            try {
                Optional<String> opt = (Optional<String>) future.get();
                if (opt.isPresent()){
                    return opt;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                future.cancel(true);
            }
        }
        service.shutdownNow();
        return Optional.empty();
    }

    class GetFuture implements Callable<Optional<String>>{

        private final String endpoint;
        private final String key;

        GetFuture(String endpoint,String key){
            this.endpoint = endpoint;
            this.key = key;
        }
        @Override
        public Optional<String> call() {
            Client client = new Client();
            String[] response = client.getKey(key,"http://"+endpoint+"/get",Config.getConfig().getServerName()+","+String.join(",",Config.getConfig().getPeers()));
            if (response[0].equalsIgnoreCase("200")){
                localKvStore.put(key,response[1]);
                return Optional.of(response[1]);
            }else{
                return Optional.empty();
            }
        }
    }




}
