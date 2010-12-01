package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.TxLog;

import javax.management.ObjectName;

/**
 * abstract implementation of the TxLog MBean
 */
public abstract class TxLogBean extends BaseStoreBean implements TxLogBeanMBean {
	private TxLog store;

	public TxLogBean(TxLog store) {
        super(store);
		this.store = store;
    }

    protected TxLog getStore() {
        return store;
    }

    @Override
    protected abstract ObjectName getMBeanName();

    public void sync () throws java.io.SyncFailedException, ObjectStoreException {
		store.sync ();
	}

	public boolean write_committed (Uid u, String tn, OutputObjectStateWrapper buff) throws ObjectStoreException {
		return store.write_committed (u, tn, buff.getOOS());
	}
    
	public boolean remove_committed (Uid u, String tn) throws ObjectStoreException {
		return store.remove_committed(u, tn);
	}

}
