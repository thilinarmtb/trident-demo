# Trident demonstrations

## Introduction

This repository contains a single trident topology and a DRPC Stream
defined on that topology. This topology is used to demonstrate two
things: exactly once processing semantics and stateful processing of
Trident.

### Topology and DRPC Stream

The topology simply reads a stream of words from a input spout
(`RepeatWordsSpout()`) and stores the number of times each word was
seen in a `redis` server which is a key-value cache and store. Read
more about `redis` [here](http://redis.io/).

The DRPC Stream is defined to read the stored count for a set of words
passed to it and return their count.

## Setting up the environment

