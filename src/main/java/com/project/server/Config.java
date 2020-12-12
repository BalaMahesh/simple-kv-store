package com.project.server;

import java.util.*;

public class Config {

    private static Config config;
    private final int port;
    private Set<String> peersList;
    private final String serverName;
    private final int repMinInterval;
    private final int maxRepBatchSize;
    private final int rf;
    private Config(int port, String[] peers, String name){
        this(port,peers,name,10,1000,1);
    }

    private Config(int port, String[] peers, String name,int replicationFactor,int repMinInterval,int repMaxBath){
        this.port = port;
        this.peersList = new HashSet<>();
        this.serverName = name;
        peersList.addAll(Arrays.asList(peers));
        this.repMinInterval = repMinInterval;
        this.maxRepBatchSize = repMaxBath;
        this.rf = replicationFactor;
    }

    public static Config getConfig(){
        if (config == null){
            throw new RuntimeException("Config not set");
        }
        return config;
    }

    public static Config setConfig(int port, String[] peers,String name,int rf,int repMinInterval,int maxRepBatchSize){
        config = new Config(port,peers,name,rf,repMinInterval,maxRepBatchSize);
        return config;
    }

    public int getPort(){
        return this.port;
    }

    public int getReplicationFactor(){ return this.rf; };

    public Set<String> getPeers(){
        return this.peersList;
    }

    public void addPeer(String peer){
        this.peersList.add(peer);
    }

    public String getServerName(){
        return this.serverName;
    }

    public int getRepMinInterval(){return this.repMinInterval;}

    public int getMaxRepBatchSize(){return this.maxRepBatchSize;}



}
