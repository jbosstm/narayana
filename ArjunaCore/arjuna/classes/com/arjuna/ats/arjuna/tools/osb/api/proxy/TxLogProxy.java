package com.arjuna.ats.arjuna.tools.osb.api.proxy;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.TxLog;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.OutputObjectStateWrapper;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.TxLogBeanMBean;

/**
 * Remote proxy to a TxLog Store
 */
public class TxLogProxy implements TxLog {
	private TxLogBeanMBean txLogProxy;

	public TxLogProxy(TxLogBeanMBean txLogProxy) {
		this.txLogProxy = txLogProxy;
    }

    // TxLog methods
    public boolean remove_committed (Uid u, String tn) throws ObjectStoreException {
        return txLogProxy.remove_committed(u, tn);
    }

    public boolean write_committed (Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        return txLogProxy.write_committed(u, tn, new OutputObjectStateWrapper(buff));
    }

    public void sync () throws java.io.SyncFailedException, ObjectStoreException {
        txLogProxy.sync();
    }

    // BaseStore methods
    public String getStoreName () {
        return txLogProxy.getStoreName();
    }

    public void start() {
        txLogProxy.start();
    }

    public void stop() {
        txLogProxy.stop();
    }
}
