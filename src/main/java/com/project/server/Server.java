package com.project.server;

import com.project.handlers.BatchPutHandler;
import com.project.handlers.DefaultHandler;
import com.project.handlers.GetHandler;
import com.project.handlers.PutHandler;
import com.project.replication.Replicator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

import java.net.InetSocketAddress;


public class Server {

    private final HttpServer server;
    private final InetSocketAddress address;
    private Replicator replicator;

    public Server(int port) throws IOException {
        address = new InetSocketAddress("127.0.0.1",port);
        server = HttpServer.create();
        server.bind(address,50);
    }


    public void startServer(){
        server.createContext("/get",new GetHandler());
        server.createContext("/set",new PutHandler());
        server.createContext("/set/batch",new BatchPutHandler());
        server.createContext("/",new DefaultHandler());
        server.start();
        System.out.println("Server started on  "+ address.toString() + " name: "+Config.getConfig().getServerName());
    }

    public void startReplicator(){
        replicator = Replicator.getReplicator(Config.getConfig());
        replicator.scheduleReplication();
    }

    public void stopServer(int delay){
        if (server != null){
            if (replicator != null) {
                replicator.shutDownReplicator();
            }
            if (delay >= 0) {
                server.stop(delay);
            }else {
                server.stop(0);
            }
        }
    }




}
