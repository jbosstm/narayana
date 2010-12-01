package com.arjuna.ats.arjuna.tools.osb.api.proxy;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.OutputObjectStateWrapper;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.ParticipantStoreBeanMBean;

/**
 * Remote proxy to a ParticipantStore
 */
public class ParticipantStoreProxy extends TxLogProxy implements ParticipantStore {
    private ParticipantStoreBeanMBean psProxy;	// proxy for the participant store

    public ParticipantStoreProxy(ParticipantStoreBeanMBean rsProxy) {
        super(rsProxy);
		this.psProxy = rsProxy;
	}

    public boolean commit_state(Uid u, String tn) throws ObjectStoreException {
        return psProxy.commit_state(u, tn);
    }

    public InputObjectState read_committed(Uid u, String tn) throws ObjectStoreException {
        return psProxy.read_committed(u, tn).getIOS();
    }

    public InputObjectState read_uncommitted(Uid u, String tn) throws ObjectStoreException {
        return psProxy.read_uncommitted(u, tn).getIOS();
    }

    public boolean remove_uncommitted(Uid u, String tn) throws ObjectStoreException {
        return psProxy.remove_uncommitted(u, tn);
    }

    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        return psProxy.write_uncommitted(u, tn, new OutputObjectStateWrapper(buff));
    }

    public boolean fullCommitNeeded() {
        return psProxy.fullCommitNeeded();
    }
}
