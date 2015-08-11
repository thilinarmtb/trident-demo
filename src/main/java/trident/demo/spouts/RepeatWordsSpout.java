package trident.demo.spouts;

import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import storm.trident.spout.ITridentSpout;

import java.util.List;
import java.util.Map;

public class RepeatWordsSpout implements ITridentSpout<List<Long>> {
    private static final long serialVersionUID = 1L;
    BatchCoordinator<List<Long>> coordinator = new RWBatchCoordinator();
    Emitter<List<Long>> emitter = new RWEmitter();

    @Override
    public Emitter<List<Long>> getEmitter(String s, Map map, TopologyContext topologyContext) {
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
    public BatchCoordinator<List<Long>> getCoordinator(String s, Map map, TopologyContext topologyContext) {
        return coordinator;
    }
}
