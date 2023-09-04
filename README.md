# simple-kv-store
This is a simple in memory key value(only string) store  and also can run as a distributed and standalone process. When run as a distributed process, it provides eventual consistency and fault tolerant.

KV store should be running as (at least) 2 different processes that replicate data between them (ie) we should be able to put in a Key and Value to Process 1 and query for the same Key on Process 2, for which we should get the corresponding Value.

Using a common backend to processes / using an existing open source KV stores like redis, etc is not allowed
Your solution should work even when we run two (or more) processes on multiple machines / containers connected over a network.

We would like you to expose the store via a HTTP service that would allow us to GET / SET
key-value pairs.
$ curl -H "Content-type: application/json" -XPOST http://localhost:4455/set/key -d ‘“value”’
OK
$ curl -H “Accept: application/json” http://localhost:4466/get/key
“value”
