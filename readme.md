Distributed KV Store :

This is a simple InMemory Key Value Store process which stores all the keys and values posted through http endpoint and also exposes endpont to get the inserted keys.

It works as a both standalone and distributed process as well running on differnt ports. 

Language used : core java(Java 8).
External dependencies / jars : None
Input : config file as a argument which contains the required input parameters.

Running the code : It has a main class, main class starts the server. 

Functioanlity : 

Server : This class is responsible for handling all the incoming HTTP requests and routes to appropriate handles and starts the replication process when started.

Handlers : There are four HTTP handlres for handling different requests:
           
           1)GET handler : serves the get call on key, if key cannot be found in its Inmemory KV Store, it contacts peers for checking and getting it. If key is found in the other process, stores it in local.
           
           2)Put Handler : Servers the set call on key, it ensures that key is replicated to other processes as well when replication factor is set from the config.
           
           3)BatchPut Handler: This is used for serving the replication, when process wants to replicate it's local data to its peers, it calls batch post api and send all the keys came into by set call. 
           
           4)Default Handler : This responds when invalid path is called on port.
           
LocalKVStore : This serves as a store to store all the keys and values that are set and replicated to it by other processes, it maintains the list to track all the appends/updates of keys and this list is used in the replicator to avoid duplicate replication and also for check pointing.

Client : Client is used to call other processes for replication and for get calls.

Main: Process starts with the main class and config file as a parameter.


Execution : Please clone the project and build using maven.

This project is built using maven tool.

After the build , jar will be located target directory.

java -jar target/{*.jar} {config.yml - sample file available in resources}.

Note : This offerens eventual consistency when key is already present in one process and updated in other process. (i.e., after replication).