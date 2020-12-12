package com.project.replication;

import com.project.client.Client;
import com.project.server.Config;
import com.project.store.LocalKvStore;


import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Replicator {

    private static Replicator replicator = null;
    private final Config config;
    private final LocalKvStore localKvStore = LocalKvStore.getInstance();
    private ScheduledExecutorService replicationExecutor;
    private Map<String,Integer> peerToCheckPoint = new HashMap<>();
    private Set<String> peers;

    private Replicator(Config config){
        this.config = config;
        peers = config.getPeers();
        peers.forEach(peer->
                peerToCheckPoint.put(peer,-1));
        replicationExecutor = Executors.newScheduledThreadPool(Math.min(peerToCheckPoint.size(),5));
    }

    public void scheduleReplication(){
        for (String peer : peers){
            System.out.println("Scheduling replication to the peers: "+peer + " interval in seconds: "+config.getRepMinInterval());
            replicationExecutor.scheduleAtFixedRate(new ReplicationFuture(peer),1, config.getRepMinInterval(),TimeUnit.SECONDS);
        }
    }

    public static Replicator getReplicator(Config config){
        if (replicator == null){
            synchronized (Replicator.class){
                if (replicator == null){
                    replicator = new Replicator(config);
                }
            }
        }
        return replicator;
    }

    class ReplicationFuture implements Runnable {

        private String peer;

        ReplicationFuture(String peer){
            this.peer = peer;
        }

        @Override
        public void run() {
            System.out.println("Replicator replicating from server : "+config.getServerName() + " to peer: "+peer);
            LinkedList<String> keys = localKvStore.getKeysInAppendList();
            System.out.println("Keys to be replicated size : "+keys.size());
            if (keys.size() > 0 && peerToCheckPoint.get(peer) < keys.size()) {
                Client client = new Client();
                int checkPoint = peerToCheckPoint.get(peer);
                Map<String, String> payload = new HashMap<>();
                int i = 0;
                if (checkPoint > keys.size()) {
                    System.out.println("Resetting the checkpoint : " + checkPoint + "to : 0");
                    checkPoint = 0;
                }
                if (checkPoint < 0)
                    checkPoint = 0;
                while (i <= config.getMaxRepBatchSize() && ((checkPoint+i) < keys.size())) {
                    String key = keys.get(checkPoint + i);
                    payload.put(key, localKvStore.get(key));
                    i++;
                }
                String[] response = new String[2];
                try {
                    response = client.batchPut(payload, "http://" + peer + "/set/batch");
                } catch (Exception e) {

                    System.out.println("Replicator failed for server " + config.getServerName() + " from peer : " + peer + " is " + response[1]);
                }
                if (response[0].equalsIgnoreCase("200")) {
                    checkPoint += i;
                    peerToCheckPoint.put(peer, checkPoint);
                }
                System.out.println("Replicator response for server " + config.getServerName() + " from peer : " + peer + " is " + response[1]);
            }else {
                System.out.println("No replication needed "+config.getServerName()+ " peer "+peer+" as already replicated or keys are zero");
            }
        }
    }

    class UpdatePeers implements Callable{

        @Override
        public Object call() throws Exception {
            if (config.getPeers().size() > peerToCheckPoint.size()){
                config.getPeers().forEach(peer->{
                    if (!peerToCheckPoint.containsKey(peer)){
                        peerToCheckPoint.put(peer,-1);
                    }
                });
            }
            return null;
        }
    }

    public void shutDownReplicator(){
        if (replicator != null){
            System.out.println("Shutting down replicator");
            replicationExecutor.shutdownNow();
        }
    }

}
