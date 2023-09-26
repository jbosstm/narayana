/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.local;

import org.jboss.jbossts.qa.performance.PerformanceTest;
import org.jboss.jbossts.qa.performance.products.TxWrapper;
import org.jboss.jbossts.qa.performance.records.DummyResource;

public class OnePhaseTrx extends PerformanceTest
{
    protected void work() throws Exception
    {
        try
        {
            TxWrapper tx = getTxWrapper();
            tx.begin();   // Top level begin

            //enlist the single participant (resource) one-phase commit
            DummyResource ds = new DummyResource();
            tx.add(ds);

            if (isParameterDefined("-commit"))
                tx.commit();  // Top level commit
            else
                tx.abort();  // Top level rollback

        }
        catch (Exception e)
        {
            System.err.println("Unexpected Exception: "+e);
            e.printStackTrace(System.err);
        }
    }
}