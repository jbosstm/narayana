/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.local;

// Top-level transactions with two enlisted participants.
// (two phase commit/rollback)

import org.jboss.jbossts.qa.performance.PerformanceTest;
import org.jboss.jbossts.qa.performance.products.TxWrapper;
import org.jboss.jbossts.qa.performance.records.DummyResource;


public class TwoPhaseTrx extends PerformanceTest
{
    public void work()
    {
        try
        {
            TxWrapper tx = getTxWrapper();
            tx.begin(); // Top level begin

            // enlist two participants (resource) two-phase commit

            tx.add(DummyResource.create());
            tx.add(DummyResource.create());

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