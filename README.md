# Trident demonstrations

## Introduction

This repository contains a single trident topology and a DRPC Stream
defined on that topology. This topology is used to demonstrate two
things: exactly once processing semantics and stateful processing of
Trident.

I hope to demonstrate these features by running this topology in a
locally created cluster. While the topology is running, we will kill
all of the worker nodes and conclude that trident has exactly once
semantics and stateful processing by looking at the final count stored
in the redis server.

To follow this tutorial I assume you know what Storm and Trident are
and have a sufficient knowledge on their core abstractions i.e.,
Spouts, Bolts, Stream, etc. Also, I assume you know what `nimbus`
and `supervisor` are.

### Topology and DRPC Stream

The topology simply reads a stream of words from a input spout and
stores the number of times each word was seen in a `redis` server which
is a key-value cache and store. Read more about `redis` in [1].

The DRPC Stream is defined to read the stored count for a set of words
passed to it and return their count.

## Setting up the environment

The most difficult thing we have to accomplish in order to successfully
run the topology is locally setting up a multi-node cluster. I referred
the excellent article written by Michael G. Noll to do this. You can
find the article in [2]. Please follow the above-mentioned article
carefully.

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
`worker1`, ... `worker4` is their configuration file `storm.yaml` found
under `conf/` in each directory. I have listed below the configuration
files I used for each of the nodes.

[nimbus](https://gist.github.com/thilinarmtb/85980741bcd90c483827)
[worker1](https://gist.github.com/thilinarmtb/2271b0eb9db5610dd636)
[worker2](https://gist.github.com/thilinarmtb/015de16702e372d810f5)
[worker3](https://gist.github.com/thilinarmtb/3264353b84cb2b66b9e7)
[worker4](https://gist.github.com/thilinarmtb/396f660e5d52960228cd)

## References

[1] Redis Homepage: http://redis.io/

[2] Running a Multi-Node Storm Cluster: http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/
