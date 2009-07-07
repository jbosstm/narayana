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
/*
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTAAbort.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.basic;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.jta.common.*;
import com.arjuna.ats.jta.utils.*;

import com.arjuna.orbportability.*;

import javax.transaction.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class JTAAbort
{
    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        jtaPropertyManager.getPropertyManager().setProperty(com.arjuna.ats.jta.common.Environment.JTA_TM_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
        jtaPropertyManager.getPropertyManager().setProperty(com.arjuna.ats.jta.common.Environment.JTA_UT_IMPLEMENTATION, "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        System.out.println("Starting top-level transaction.");

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

        System.out.println("\nRolling back transaction.");

        theTransaction.rollback();

        System.out.println("\nTransaction now: "+theTransaction);

        System.out.println("\nThread associated: "+JTAHelper.stringForm(tm.getStatus()));

        theTransaction = tm.suspend();

        System.out.println("\nSuspended: "+theTransaction);

        tm.resume(theTransaction);

        myOA.destroy();
        myORB.shutdown();
    }
}
