package com.project;

import com.project.client.Client;
import com.project.server.Config;
import com.project.server.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for the api's
 */
public class TestDistributedKV {

    private static Server serverA;
    private static Client client;
    private static Config config;


    private static void testSetKeys(String address) throws IOException {
        String[] res = client.setKey("test","testset","http://"+address+"/set","");
        assert res[0].equalsIgnoreCase("200");
        assert res[1].equalsIgnoreCase("OK");
    }

    private static void testGetKeys(String address){
        String[] res = client.getKey("test","http://"+address+"/get","");
        assert res[1].equalsIgnoreCase("testset");
    }

    private static void testBatchPut(String address){
        Map<String,String> testMap = new HashMap<>();
        testMap.put("a","b");
        String[] res = client.batchPut(testMap,"http://"+address+"/set/batch");
        assert res[1].equalsIgnoreCase("OK");
    }


    private static void testStandAlone(String address) {
        try {
            testSetKeys(String.valueOf(address));
            testGetKeys(String.valueOf(address));
            testBatchPut(String.valueOf(address));
            System.out.println("***=== Test success on standalone process ***===");
        }catch (Exception e){
            System.out.println("Test failed");
        }
    }

    /**
     * Start the this process on differnt port manually and then run this test , else it fail, from test class it is difficult start other process;
     */
    private static void testOnOtherProcess(String thisAddress,String otherAddress){
        if (config.getPeers().size() == 0){
            config.addPeer(otherAddress);
        }
        try {
            doHealthCheck("http://"+otherAddress);
            testSetKeys(otherAddress);
            doHealthCheck("http://"+thisAddress);
            testGetKeys(thisAddress);
            System.out.println("***=== Test success on other process ***===");
        }catch (IOException e){
            System.out.println("***=== Test failed check if remote server is up ***===");
        }


    }

    private static void doHealthCheck(String address) throws IOException {
        System.out.println("Health check on address "+address);
        String[] response = client.healthCheck(address);
        if (response[0].equalsIgnoreCase("200")){
            return;
        }else {
            throw new IOException();
        }
    }

    private static void testReplication(String thisAddress,String otherAddress){
        try {
            doHealthCheck("http://"+thisAddress);
            testSetKeys(thisAddress);
            Thread.sleep((config.getRepMinInterval()+1)*1000);
            testGetKeys(otherAddress);
            System.out.println("***=== Replication test succeeded ***==");
        }catch (IOException |InterruptedException e){
            System.out.println("Replication test failed");
        }

    }

    public static void main(String[] args) throws IOException {
        config = Config.setConfig(13000,new String[]{"localhost:13001"},"localhost:13000",0,10,100);
        serverA = new Server(config.getPort());
        serverA.startServer();
        serverA.startReplicator();
        client = new Client();
        doHealthCheck("http://"+config.getServerName());
        testStandAlone(config.getServerName());
        testOnOtherProcess(config.getServerName(),"localhost:13001");
        testReplication(config.getServerName(),"localhost:13001");
        serverA.stopServer(0);
    }

}
