/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.basic;

import com.arjuna.mwlabs.testframework.unittest.Test;
import com.arjuna.mwlabs.testframework.unittest.LocalHarness;

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

/*
 * Copyright (C) 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATransactionCommitTest.java 2342 2006-03-30 13:06:17Z  $
 */

public class JTATransactionCommitTest extends Test
{
    public void run(String[] args)
    {
        try
        {
            TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            System.out.println("Beginning new transaction");
            tm.begin();

            Transaction tx = tm.suspend();

            System.out.println("Beginning second transaction");
            tm.begin();

            System.out.println("Committing original transaction (via Transaction interface)");
            tx.commit();

            System.out.println("Committing second transaction (via TransactionManager interface)");
            tm.commit();

            assertSuccess();
        }
        catch (Exception e)
        {
            System.out.println("Unexpected exception: "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }
    }

    public static void main(String[] args)
    {
        JTATransactionCommitTest test = new JTATransactionCommitTest();
        test.initialise(null, null, args, new LocalHarness());
        test.runTest();
    }
}
