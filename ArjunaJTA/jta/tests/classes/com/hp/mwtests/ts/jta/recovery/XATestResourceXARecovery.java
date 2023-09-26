/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import java.sql.SQLException;

/**
 * XAResourceRecovery that returns unrecoverable and recoverable resources
 */
public class XATestResourceXARecovery implements XAResourceRecovery {
    private static final int MAX_COUNT = 2;

    private static boolean useFaultyResources;

    private int count = 0;

    public static void setUseFaultyResources(boolean useFaultyResources) {
        XATestResourceXARecovery.useFaultyResources = useFaultyResources;
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        count++;

        if (count == 1 && useFaultyResources)
            return new XATestResource(XATestResource.FAULTY_JNDI_NAME, true);

        return new XATestResource(XATestResource.OK_JNDI_NAME, false);
    }

    @Override
    public boolean initialise(String p) throws SQLException {
        return true;
    }

    @Override
    public boolean hasMoreResources() {
        if (count < MAX_COUNT)
            return true;

        count = 0;

        return false;
    }
}