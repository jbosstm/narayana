package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * JMX interface to the JBossTS recovery store
 * OutputObjectState and InputObjectState are wrapped since they are not convertible to
 * open MBean types.
 *
 * @see com.arjuna.ats.arjuna.tools.osb.api.proxy.RecoveryStoreProxy
 * for the actual remote RecoveryStore proxy
 *
 * @see com.arjuna.ats.arjuna.objectstore.RecoveryStore for the interface it implements
 */
public interface RecoveryStoreBeanMBean extends TxLogBeanMBean
{
    public ObjectStateWrapper allObjUids(String type, int match) throws ObjectStoreException;
    public ObjectStateWrapper allObjUids(String type) throws ObjectStoreException;

    public ObjectStateWrapper allTypes() throws ObjectStoreException;
    public int currentState (Uid u, String tn) throws ObjectStoreException;
    public boolean hide_state (Uid u, String tn) throws ObjectStoreException;
    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException;
    public ObjectStateWrapper read_committed (Uid u, String tn) throws ObjectStoreException;
    public boolean isType (Uid u, String tn, int st) throws ObjectStoreException;
}
