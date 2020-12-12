package com.project;

import com.project.server.Config;
import com.project.server.Server;

import java.io.File;
import java.io.FileInputStream;


public class Main {

    public static void main(String[] args) {
        // write your code here
        try {
            if (args.length < 1){
                System.out.println("No Config available, please provide sever port and peers address");
            }
            String configFileName = args[0];
            File file = new File(configFileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[fis.available()];
            int len = fis.read(b);
            String confStr = new String(b);
            System.out.println("====*****====");
            System.out.println("Config:");
            System.out.println(confStr);
            System.out.println("====*****====");
            String[] lines = confStr.split("\n");
            int port = Integer.parseInt(lines[0].split(":\\s")[1].trim());
            String peer = lines[1].split(":\\s")[1].trim().replaceAll("\\'","");
            String name = lines[2].split(":\\s")[1].trim();
            int rf = Integer.parseInt(lines[3].split(":\\s")[1].trim());
            int repIntervalMin = Integer.parseInt(lines[4].split(":\\s")[1].trim());
            int repMaxBatch = Integer.parseInt(lines[5].split(":\\s")[1].trim());
            String[] peers = peer.split(",");
            Config.setConfig(port,peers,name,rf,repIntervalMin,repMaxBatch);
            Server s = new Server(port);
            s.startServer();
            s.startReplicator();
            System.out.println("======*** server started **====");
        }catch (Exception e){
            System.out.println("Error Starting the server : " + e);
        }

    }
}
