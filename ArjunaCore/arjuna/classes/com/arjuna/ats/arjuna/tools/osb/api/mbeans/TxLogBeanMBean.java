package com.arjuna.ats.arjuna.tools.osb.api.mbeans;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

public interface TxLogBeanMBean extends BaseStoreMBean
{
	public boolean remove_committed (Uid u, String tn) throws ObjectStoreException;
    public boolean write_committed (Uid u, String tn, OutputObjectStateWrapper buff) throws ObjectStoreException;
    public void sync () throws java.io.SyncFailedException, ObjectStoreException;
}
