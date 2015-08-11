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

public class Demo3 {

    public static StormTopology buildTopology(StateFactory state) {
        TridentTopology topology = new TridentTopology();

        TridentState wordCounts = topology.newStream("spout1", new RepeatWordsSpout())
                .parallelismHint(1)
                .groupBy(new Fields("word"))
                .persistentAggregate(state, new Count(), new Fields("count"))
                .parallelismHint(1);

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
        StateFactory redis = RedisState.transactional(new InetSocketAddress("localhost", 6379));

        StormTopology topology = buildTopology(redis);

        Config conf = new Config();
        conf.setDebug(false);
        if (args != null && args.length > 0) {
            conf.setNumWorkers(4);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, topology);
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("tester", conf, topology);
        }

        DRPCClient client = new DRPCClient("localhost", 3772);

        try {
            while (true) {
                System.out.println("DRPC: " + client.execute("words", "apple ball cat dog"));
                Utils.sleep(2000);
            }

        } catch (JedisConnectionException e) {
            throw new RuntimeException("Unfortunately, this test requires redis-server runing on localhost:6379", e);
        }
    }
}
