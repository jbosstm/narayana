/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.implementations.local;

import org.jboss.jbossts.qa.performance.PerformanceTest;
import org.jboss.jbossts.qa.performance.products.TxWrapper;

public class TopLevelTrx extends PerformanceTest
{
    protected void work() throws Exception
    {
        try
        {
            TxWrapper tx = getTxWrapper();

            tx.begin();  // Top level begin

            if (isParameterDefined("-commit"))
                tx.commit(); // Top level commit
            else
                tx.abort(); // Top level rollback

        }
        catch (Exception e)
        {
            System.err.println("Error!!");
            e.printStackTrace();
        }
    }
}