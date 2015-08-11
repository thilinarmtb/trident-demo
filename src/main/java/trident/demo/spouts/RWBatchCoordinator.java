package trident.demo.spouts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.spout.ITridentSpout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RWBatchCoordinator implements ITridentSpout.BatchCoordinator<List<Long>>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(RWBatchCoordinator.class);

    @Override
    public List<Long> initializeTransaction(final long txid, final List<Long> prevMetaData, List<Long> currMetaData) {
        LOG.info("Initializing transaction [" + txid + "]");

        if (prevMetaData == null && currMetaData == null) {
            currMetaData = new ArrayList<Long>() {{add(0L); add(txid % 2);}};
        } else if (currMetaData == null) {
            currMetaData = new ArrayList<Long>() {{add(prevMetaData.get(0) + 1); add(txid % 2);}};
        }
        return currMetaData;
    }

    @Override
    public void success(long txid) {
        LOG.info("Successful transaction [" + txid + "]");
    }

    @Override
    public boolean isReady(long l) {
        return true;
    }

    @Override
    public void close() {
    }
}