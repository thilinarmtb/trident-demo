package trident.demo.spouts;

import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.ITridentSpout;
import storm.trident.topology.TransactionAttempt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RWEmitter implements ITridentSpout.Emitter<List<Long>>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final long N = 50;

    List<List<Object>> batchA = new ArrayList<List<Object>>() {{
        add(new Values("apple"));
        add(new Values("ball"));
        add(new Values("cat"));
        add(new Values("dog"));
    }};

    List<List<Object>> batchB = new ArrayList<List<Object>>() {{
        add(new Values("a"));
        add(new Values("b"));
        add(new Values("c"));
        add(new Values("d"));
    }};

    private static final Logger LOG = LoggerFactory.getLogger(RWEmitter.class);

    @Override
    public void emitBatch(TransactionAttempt transactionAttempt, List<Long> coordinatorMeta, TridentCollector tridentCollector) {
        List<List<Object>> batch;

        if (coordinatorMeta.get(0) < N) {
            LOG.info("Emitting transaction [" + transactionAttempt.getTransactionId() + "] " + coordinatorMeta);

            if (coordinatorMeta.get(1) == 0L) {
                batch = batchA;
            } else {
                batch = batchB;
            }

            for (List<Object> tuple : batch) {
                tridentCollector.emit(tuple);
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void success(TransactionAttempt transactionAttempt) {
    }

    @Override
    public void close() {
    }
}
