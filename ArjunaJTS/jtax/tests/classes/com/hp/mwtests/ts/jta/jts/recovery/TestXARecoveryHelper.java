/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.jts.recovery;

import javax.transaction.xa.XAResource;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

public class TestXARecoveryHelper implements XAResourceRecoveryHelper {

    final private XAResource[] xares;

    public TestXARecoveryHelper(XAResource xaResource) {
        xares = new XAResource[]{ xaResource };
    }

    public TestXARecoveryHelper(XAResource... xaResource) {
        xares = xaResource;
    }

    @Override
    public boolean initialise(String p) throws Exception
    {
        return false;
    }

    @Override
    public XAResource[] getXAResources() throws Exception
    {
        return xares;
    }

}