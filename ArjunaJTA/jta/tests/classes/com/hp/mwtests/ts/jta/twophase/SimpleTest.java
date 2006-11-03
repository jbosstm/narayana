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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.twophase;

import com.hp.mwtests.ts.jta.common.TestResource;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class SimpleTest
{
    public SimpleTest ()
    {
        try
        {
            javax.transaction.TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

            transactionManager.begin();

            Transaction currentTrans = transactionManager.getTransaction();

            TestResource res1, res2;
            currentTrans.enlistResource( res1 = new TestResource() );
            currentTrans.enlistResource( res2 = new TestResource() );

            currentTrans.delistResource( res2, XAResource.TMSUCCESS );
            currentTrans.delistResource( res1, XAResource.TMSUCCESS );

            transactionManager.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            System.err.println("ERROR - "+e);
        }
    }

    public static void main(String[] args)
    {
        new SimpleTest();
    }

}
