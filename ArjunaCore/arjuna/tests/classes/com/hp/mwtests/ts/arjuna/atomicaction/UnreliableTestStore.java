package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class UnreliableTestStore extends VolatileStore {
    private boolean writeError;

    public void setWriteError(boolean writeError) {
        this.writeError = writeError;
    }

    public UnreliableTestStore() throws ObjectStoreException {
        this(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class));

    }

    public UnreliableTestStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException {
        super(objectStoreEnvironmentBean);
    }

    @Override
    public boolean write_committed(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        if (writeError)
            throw new ObjectStoreException();

        return super.write_committed(u, tn, buff);
    }

    @Override
    public boolean write_uncommitted(Uid u, String tn, OutputObjectState buff) throws ObjectStoreException {
        if (writeError)
            throw new ObjectStoreException();

        return super.write_uncommitted(u, tn, buff);
    }
}
