package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * JMX interface to the JBossTS participant store
 *
 * OutputObjectState and InputObjectState are wrapped since they are not convertible to
 * open MBean types.
 *
 * @see com.arjuna.ats.arjuna.tools.osb.api.proxy.ParticipantStoreProxy
 * for the actual remote RecoveryStore proxy
 *
 * @see com.arjuna.ats.arjuna.objectstore.ParticipantStore for the interface it implements
 */
public interface ParticipantStoreBeanMBean extends TxLogBeanMBean {
    public boolean commit_state (Uid u, String tn) throws ObjectStoreException;
    public ObjectStateWrapper read_committed (Uid u, String tn) throws ObjectStoreException;
    public ObjectStateWrapper read_uncommitted (Uid u, String tn) throws ObjectStoreException;
    public boolean remove_uncommitted (Uid u, String tn) throws ObjectStoreException;
    public boolean write_uncommitted (Uid u, String tn, OutputObjectStateWrapper buff) throws ObjectStoreException;
    public boolean fullCommitNeeded ();
}
