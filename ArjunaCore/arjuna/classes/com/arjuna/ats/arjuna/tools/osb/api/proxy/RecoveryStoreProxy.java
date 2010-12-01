package com.arjuna.ats.arjuna.tools.osb.api.proxy;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.ObjectStateWrapper;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.RecoveryStoreBeanMBean;

/**
 * Remote proxy to a RecoveryStore
 */
public class RecoveryStoreProxy extends TxLogProxy implements RecoveryStore {
    private RecoveryStoreBeanMBean rsProxy;	// proxy for the recovery store

    public RecoveryStoreProxy(RecoveryStoreBeanMBean rsProxy) {
        super(rsProxy);
		this.rsProxy = rsProxy;
	}


    public boolean allObjUids(String type, InputObjectState buff, int match) throws ObjectStoreException {
        ObjectStateWrapper ios = rsProxy.allObjUids(type, match);
        OutputObjectState oos = ios.getOOS();
        if (oos == null)
            return false;
        buff.copyFrom(oos);
        return ios.isValid();
    }

    public boolean allObjUids(String type, InputObjectState buff) throws ObjectStoreException {
        ObjectStateWrapper ios = rsProxy.allObjUids(type);
        OutputObjectState oos = ios.getOOS();
        if (oos == null)
            return false;
        buff.copyFrom(oos);
        return ios.isValid();
    }

    public boolean allTypes(InputObjectState buff) throws ObjectStoreException {
        ObjectStateWrapper ios = rsProxy.allTypes();
        OutputObjectState oos = ios.getOOS();
        if (oos == null)
            return false;
        buff.copyFrom(oos);
        return ios.isValid();
    }

    public int currentState(Uid u, String tn) throws ObjectStoreException {
        return rsProxy.currentState(u, tn);
    }

    public boolean hide_state(Uid u, String tn) throws ObjectStoreException {
        return rsProxy.hide_state(u, tn);
    }

    public boolean reveal_state(Uid u, String tn) throws ObjectStoreException {
        return rsProxy.reveal_state(u, tn);
    }

    public InputObjectState read_committed(Uid u, String tn) throws ObjectStoreException {
        ObjectStateWrapper osw = rsProxy.read_committed(u, tn);
        return osw.getIOS();
    }

    public boolean isType(Uid u, String tn, int st) throws ObjectStoreException {
        return rsProxy.isType(u, tn, st);
    }
}
