package trident.demo.spouts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.TridentCollector;
import storm.trident.spout.ITridentSpout;
import storm.trident.topology.TransactionAttempt;
import backtype.storm.tuple.Values;

public class RWEmitter implements ITridentSpout.Emitter<Long>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final long N = 50;

    private Map<Long, List<List<Object>>> dataStore = new HashMap<Long, List<List<Object>>>();
    private static AtomicInteger count = new AtomicInteger(0);

    private static final Logger LOG = LoggerFactory.getLogger(RWEmitter.class);

    @Override
    public void emitBatch(TransactionAttempt transactionAttempt, Long aLong, TridentCollector tridentCollector) {
        boolean reEmit = true;
        List<List<Object>> batch = dataStore.get(transactionAttempt.getTransactionId());

        if (batch == null) {
            reEmit = false;
            count.incrementAndGet();
        }

        if (!reEmit && count.get() < N) {
            batch = new ArrayList<List<Object>>();
            batch.add(new Values("apple"));
            batch.add(new Values("ball"));
            batch.add(new Values("cat"));
            batch.add(new Values("dog"));

            dataStore.put(transactionAttempt.getTransactionId(), batch);
        }

        if (reEmit || count.get() < N) {
            LOG.info("Emitting transaction [" + transactionAttempt.getTransactionId() + "]");
            for (List<Object> tuple : batch) {
                tridentCollector.emit(tuple);
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void success(TransactionAttempt transactionAttempt) {
        dataStore.remove(transactionAttempt.getTransactionId());
    }

    @Override
    public void close() {
    }
}
