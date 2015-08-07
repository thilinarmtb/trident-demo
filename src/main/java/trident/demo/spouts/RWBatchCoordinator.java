package trident.demo.spouts;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.spout.ITridentSpout;

public class RWBatchCoordinator implements ITridentSpout.BatchCoordinator<Long>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(RWBatchCoordinator.class);

    @Override
    public Long initializeTransaction(long txid, Long prevMetaData, Long currMetaData) {
        LOG.info("Initializing transaction [" + txid + "] " + currMetaData + " " + prevMetaData);

        if(currMetaData != null)
            return currMetaData;
        return txid;
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