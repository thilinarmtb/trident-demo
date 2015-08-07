package trident.demo;

import java.net.InetSocketAddress;

import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.redis.RedisState;
import storm.trident.state.StateFactory;
import trident.demo.spouts.RepeatWordsSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;

public class Demo3 {

    public static StormTopology buildTopology(StateFactory state) {
        TridentTopology topology = new TridentTopology();

        TridentState wordCounts = topology.newStream("spout1", new RepeatWordsSpout())
                .parallelismHint(1)
                .groupBy(new Fields("word"))
                .persistentAggregate(state, new Count(), new Fields("count"))
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
            conf.setMaxTaskParallelism(1);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, topology);
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("tester", conf, topology);
        }
    }
}
