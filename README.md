# Trident demonstrations

## Introduction

This repository contains a single trident topology and a DRPC Stream
defined on that topology. This topology is used to demonstrate two
things: exactly once processing semantics and stateful processing of
Trident.

I hope to demonstrate these features by running this topology in a
locally created multi-node cluster. While the topology is running, we
will kill all of the worker nodes and conclude that trident has exactly
once semantics and stateful processing by looking at the final count stored
in the redis server.

To follow this tutorial I assume you know what Storm and Trident are
and have a sufficient knowledge on their core abstractions i.e.,
Spouts, Bolts, Stream, etc. Also, I assume you know what `nimbus`
and `supervisor` are.

### Topology and DRPC Stream

The topology simply reads a stream of words from a input spout and
stores the number of times each word was seen in a `redis` server which
is a key-value cache and store. Read more about `redis` in [1]. Input
spout emits two batches. One batch contains the words 
[`apple`, `ball`, `cat`, `dog`] and the other batch contains [`a`, `b`, `c`, `d`].
Each batch is emitted 25 times by the spout.

The DRPC Stream is defined to read the stored count for a set of words
passed to it and return their count.

## Setting up the environment

The most difficult thing we have to accomplish in order to successfully
run the topology is locally setting up a multi-node cluster. I referred
the excellent article written by Michael G. Noll to do this. You can
find the article in [2]. Please follow the above-mentioned article
carefully. We need to pay special attention to two parts of the tutorial:
Creating the multi-node cluster and running the nodes under supervision.

### Local multi-node cluster

The article in [2] describes in detail a general approach to configure
an actual multi-node cluster using multiple physical nodes. For most of
us, access to that kind of resources is not possible. So we are going
to mimic this environment locally.

The only thing you have to do differently from the tutorial is to create
multiple storm directories locally and update their `conf/storm.yaml` so
that the supervisors of each local copy run on different ports. You can
download storm releases from [here](http://storm.apache.org/downloads.html).
I used `0.9.2-incubating` version. You need to download a release, extract
it and rename the extracted directory depending on the tasks you need
that node to perform. Below is the folder structure of my cluster.

```
trident_cluster
├── nimbus
├── worker1
├── worker2
├── worker3
├── worker4
└── zookeeper-3.4.6
```

I have additionally added a zookeeper node as well. I am using zookeeper
version 3.4.6. 

Only difference (except the names of the directories) between `nimbus`,
`worker1`,`... worker4` is their configuration file `storm.yaml` found
under `conf/` in each directory. I have listed below the configuration
files I used for each of the nodes.

[nimbus](https://gist.github.com/thilinarmtb/85980741bcd90c483827)

[worker1](https://gist.github.com/thilinarmtb/2271b0eb9db5610dd636)

[worker2](https://gist.github.com/thilinarmtb/015de16702e372d810f5)

[worker3](https://gist.github.com/thilinarmtb/3264353b84cb2b66b9e7)

[worker4](https://gist.github.com/thilinarmtb/396f660e5d52960228cd)

### Running the nodes under supervision

It's really important to run our nodes under supervision. To do this,
you need to install `supervisor` program. Read more about this in
[2]. Below are the configurations files I put under `/etc/supervisor/conf.d/`
to set up the nodes to run under supervision. 

[zookeeper.conf](https://gist.github.com/thilinarmtb/d2976be13a4092c8c548)

[storm_drpc.conf](https://gist.github.com/thilinarmtb/6c18c0ae4ae5f2f83573)

[storm_nimbus.conf](https://gist.github.com/thilinarmtb/6053827e08343242c875)

[strom_worker1.conf](https://gist.github.com/thilinarmtb/9356bd752e8d68715121)

[storm_worker2.conf](https://gist.github.com/thilinarmtb/f987e25544e1731a031c)

[storm_worker3.conf](https://gist.github.com/thilinarmtb/317e0d09fa0361656caa)

[storm_worker4.conf](https://gist.github.com/thilinarmtb/77b0b41f7e0ad216cdc4)

Note that we need to run `zookeeper`, `nimbus`, `drpc` (since we are
using DRPC queries) and a `supervisor` for each worker node. Make sure that
you change the paths and the user in the configuration files according to your
setup.

### Installing redis

Follow the instructions found in [3] or [6] to install `redis` in your machine.

## Building the Demonstration

First clone and build the `trident-redis` [4] repository before building this
repository. `trident-redis` library implements a trident state on top of `redis`.
This is required to store our state on redis.

Then create a Uberjar of this repository by doing:
```
mvn clean package -Pcluster
```

This will create a jar called `demo-1.0-SNAPSHOT-jar-with-dependencies.jar` under
`target/` folder.

## Running the Demonstration

Finally, we can run the demonstration. To do this first we need to make sure
that `supervisor` is started and our programmes are running under supervision.
Enter the command `sudo supervisorctl status` in a terminal and you will see
something like following if everything is okay.

```
storm_drpc                       RUNNING    pid 7600, uptime 0:02:13
storm_nimbus                     RUNNING    pid 7602, uptime 0:02:13
storm_worker1                    RUNNING    pid 7603, uptime 0:02:13
storm_worker2                    RUNNING    pid 7605, uptime 0:02:13
storm_worker3                    RUNNING    pid 7604, uptime 0:02:13
storm_worker4                    RUNNING    pid 7606, uptime 0:02:13
zookeeper                        RUNNING    pid 7601, uptime 0:02:13
```

Otherwise start the supervisor service. You can read more about this in [5].

Follow the instructions in [6] to get the redis service started in your machine.
Then start the redis command line client by typing `redis-cli` in a terminal.
We are going to monitor the keys stored in the redis server using the cli.
Once you start a session using `redis-cli`, you can list the available
keys by typing `KEYS *` and get a value of a key by using `get`. Suppose
I have a key called `apple`. If I want to see the value of it, then I can type
`get apple` to get the value.

Then in a new terminal window, change directories to the nimbus node (or any
worker node) and submit the Uberjar to sotrm using the following command.
```
bin/storm jar <path_to_the_jar_we_created>/demo-1.0-SNAPSHOT-jar-with-dependencies.jar trident.demo.Demo demo
```

Now monitor the changes of the key-values using `KEYS *` and `get` command in
the `redis-cli`.

You can kil the worker nodes using `kill -9 <pid>` and observe the effect of
killing them in the final result. You will note that our topology will have
the same final state in `redis` irrespective of the killing of worker nodes.

**Note**: When running the topology again and again, make sure to clear the zookeeper
meta-data. You can do this by changing directories to the zookeeper node and doing
the following:
```
bin/zkCli.sh -server 127.0.0.1:2181
rmr /transactional
```

## References

[1] Redis Homepage: http://redis.io/

[2] Running a Multi-Node Storm Cluster: http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/

[3] Redis Quick Start: http://redis.io/topics/quickstart

[4] Trident redis: https://github.com/kstyrc/trident-redis

[5] How To Install and Manage Supervisor on Ubuntu and Debian VPS: https://www.digitalocean.com/community/tutorials/how-to-install-and-manage-supervisor-on-ubuntu-and-debian-vps

[6] How To Install and Use Redis: https://www.digitalocean.com/community/tutorials/how-to-install-and-use-redis
