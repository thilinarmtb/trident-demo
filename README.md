# Trident demonstrations

This repository contains a single trident topology and a DRPC Stream
defined to query the state stored by the topology.

## Topology and DRPC Stream

The topology simply reads a stream of words from a input spout and stores
the count of each word in a redis which is a key-value cache and store.

The DRPC Stream reads the stored state and output the count of the words
passed to the DRPC call.

