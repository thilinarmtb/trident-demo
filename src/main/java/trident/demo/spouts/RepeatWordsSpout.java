package trident.demo.spouts;

import java.util.Map;

import storm.trident.spout.ITridentSpout;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;

public class RepeatWordsSpout implements ITridentSpout<Long> {
    private static final long serialVersionUID = 1L;
    SpoutOutputCollector collector;
    BatchCoordinator<Long> coordinator = new RWBatchCoordinator();
    Emitter<Long> emitter = new RWEmitter();

    @Override
    public Emitter<Long> getEmitter(String s, Map map, TopologyContext topologyContext) {
        return emitter;
    }

    @Override
    public Map getComponentConfiguration() {
        return null;
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("word");
    }

    @Override
    public BatchCoordinator<Long> getCoordinator(String s, Map map, TopologyContext topologyContext) {
        return coordinator;
    }
}
