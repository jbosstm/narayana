/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.ats.jtax.tests.implicit.impl;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RemoteImpl.java 2342 2006-03-30 13:06:17Z  $
 */

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jtax.tests.resources.ExampleXAResource;

public class RemoteImpl extends Example.testPOA
{
    public void invoke()
    {
        try
        {
            TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            ExampleXAResource a = new ExampleXAResource();
            ExampleXAResource b = new ExampleXAResource();

            Transaction tx = tm.getTransaction();

            System.out.println("CurrentTx : "+tx+" >> "+OTSImpleManager.current().getControlWrapper().isLocal());

            tx.enlistResource(a);
            tx.delistResource(a,javax.transaction.xa.XAResource.TMSUCCESS);

            tx.enlistResource(b);
            tx.delistResource(b,javax.transaction.xa.XAResource.TMSUCCESS);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
