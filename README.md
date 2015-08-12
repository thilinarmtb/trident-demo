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

### Topology and DRPC Stream

The topology simply reads a stream of words from a input spout
(`RepeatWordsSpout()`) and stores the number of times each word was
seen in a `redis` server which is a key-value cache and store. Read
more about `redis` [here](http://redis.io/).

The DRPC Stream is defined to read the stored count for a set of words
passed to it and return their count.

## Setting up the environment

The most difficult thing we have to accomplish inorder to successfully
run the topology is setting up a production cluster locally. I referd
the excellent article written by Michael G. Noll to do this. You can
find the article [here](http://www.michael-noll.com/tutorials/running-multi-node-storm-cluster/).
Please follow this article carefully to setup a multinode storm cluster
locally.

