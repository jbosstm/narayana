/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.local;

import org.jboss.jbossts.qa.performance.PerformanceTest;
import org.jboss.jbossts.qa.performance.products.TxWrapper;

public class NestedTrx extends PerformanceTest
{
    protected boolean requiresNestedTxSupport()
    {
        return true;
    }

    protected void work() throws Exception
    {
        try
        {
            TxWrapper tx1 = getTxWrapper();
            TxWrapper tx2 = getTxWrapper();

            tx1.begin();         // Top level begin
            tx2.begin();         // Nested level begin

            if (isParameterDefined("-commit"))
            {
                tx2.commit();        // Nested level commit
                tx1.commit();        // Top level commit
            }
            else
            {
                tx2.abort();        // Nested level rollback
                tx1.abort();        // Top level rollback
            }

        }
        catch (Exception e)
        {
            System.err.println("Unexpected Exception: "+e);
            e.printStackTrace(System.err);
        }

    }
}