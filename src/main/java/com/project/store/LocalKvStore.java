package com.project.store;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class LocalKvStore{

    private static LocalKvStore kvStore = null;

    private ConcurrentHashMap<String,String> keyValue;

    private final LinkedList<String> keysAppendList;  // for replication;

    public boolean put(String key, String value){
        keyValue.put(key,value);
        keysAppendList.add(key);
        if (keysAppendList.size() >= Integer.MAX_VALUE-1000){
            keysAppendList.clear();
        }
        return true;
    }

    public boolean update(String key, String value){
        keyValue.put(key,value);
        return true;
    }

    public String get(String key){
        return keyValue.get(key);
    }

    public static LocalKvStore getInstance(){
        if (kvStore == null){
            synchronized (LocalKvStore.class){
                if (kvStore == null){
                    kvStore = new LocalKvStore();
                }
            }
        }
        return kvStore;
    }

    public LinkedList<String> getKeysInAppendList(){
        return this.keysAppendList;
    }

    private LocalKvStore(){
        keyValue = new ConcurrentHashMap<>();
        keysAppendList = new LinkedList<>();
    }

}
