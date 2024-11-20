package com.hp.mwtests.ts.jta.common;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import java.sql.SQLException;

public class BytemanControlledXAResourceRecovery implements XAResourceRecovery {

    private boolean alreadyReturned = false;

    @Override
    public XAResource getXAResource() throws SQLException {
        alreadyReturned = true;
        return new BytemanControlledXAResource();
    }

    @Override
    public boolean initialise(String p) throws SQLException {
        return true;
    }

    @Override
    public boolean hasMoreResources() {
        boolean tempBoolean = !alreadyReturned;
        alreadyReturned = false;
        return tempBoolean;
    }
}
