package trident.demo;

import java.net.InetSocketAddress;

import backtype.storm.StormSubmitter;
import backtype.storm.utils.DRPCClient;
import redis.clients.jedis.exceptions.JedisConnectionException;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.FilterNull;
import storm.trident.operation.builtin.MapGet;
import storm.trident.redis.RedisState;
import storm.trident.state.StateFactory;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import storm.trident.testing.Split;
import trident.demo.spouts.RepeatWordsSpout;

public class Demo {

    public static StormTopology buildTopology(StateFactory state) {
        // A Trident topology operates in a higher level than a Storm topology.
        TridentTopology topology = new TridentTopology();

        // Create a simple new stream using the `RepeatWordsSpout`. In this topology,
        // we group the words emitted by `RepeatWordsSpout` by the field and store the count in
        // `StateFactory` passed to the function. In order to aggregate across the batches
        // We use persistentAggregate function. parallelismHint provides a hint for the
        // parallelism of the topology.
        TridentState wordCounts = topology.newStream("wordCount", new RepeatWordsSpout())
                .parallelismHint(1)
                .groupBy(new Fields("word"))
                .persistentAggregate(state, new Count(), new Fields("count"))
                .parallelismHint(1);

        // DRPC stands for Distributed Remote Procedure Call.
        // We can issue calls to the following DRPC Stream using a client library.
        // A DRPC call takes two Strings, function name and function arguments.
        // Function arguments are available as the field `args`.
        // In the following DRPC Stream defined with the name "words", we split the words
        // contained in the `args` and return the count of each word currently stored
        // in the state.
        topology.newDRPCStream("words")
                .parallelismHint(1)
                .each(new Fields("args"), new Split(), new Fields("word"))
                .parallelismHint(1)
                .groupBy(new Fields("word"))
                .stateQuery(wordCounts, new Fields("word"), new MapGet(), new Fields("count"))
                .parallelismHint(1)
                .each(new Fields("count"), new FilterNull())
                .parallelismHint(1);

        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        // We create the redis StateFactory using the `trident-redis` library.
        // We use redis to store our state.
        StateFactory redis = RedisState.transactional(new InetSocketAddress("localhost", 6379));

        // Build the topology
        StormTopology topology = buildTopology(redis);

        // `conf` stores the configuration information that needs to be supplied with
        // the strom topology.
        Config conf = new Config();
        conf.setDebug(false);
        if (args != null && args.length > 0) {
            // Submit the topology to the cluster.
            conf.setNumWorkers(4);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, topology);
        } else {
            // Run locally if no input arguments are present.
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("tester", conf, topology);
        }

        // Create a DRPC client
        DRPCClient client = new DRPCClient("localhost", 3772);

        try {
            while (true) {
                // We keep querying the state stored in redis forever.
                System.out.println("DRPC: " + client.execute("words", "apple ball cat dog"));
                Utils.sleep(2000);
            }

        } catch (JedisConnectionException e) {
            throw new RuntimeException("Unfortunately, this test requires redis-server runing on localhost:6379", e);
        }
    }
}
