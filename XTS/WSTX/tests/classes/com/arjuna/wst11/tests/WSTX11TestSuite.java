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
 * (C) 2005-2008,
 * @author JBoss Inc.
 */
/*
 * TestSuite.java
 */

package com.arjuna.wst11.tests;

import com.arjuna.wst11.tests.junit.basic.SubtransactionCommit;
import com.arjuna.wst11.tests.junit.basic.SubtransactionCommitFailInPrepare;
import com.arjuna.wst11.tests.junit.basic.SubtransactionCommitRollbackInPrepare;
import com.arjuna.wst11.tests.junit.basic.SubtransactionRollback;

public class WSTX11TestSuite extends junit.framework.TestSuite
{
    public WSTX11TestSuite()
    {
        // wst basic tests
        // this test fails because the commit is retried forever instead of an exception being generated
        //addTest(new junit.framework.TestSuite(CommitExceptionInCommit.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.CommitExceptionInPrepare.class));

        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.CommitRollbackInPrepare.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.MultiParticipants.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.NestedTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.NullCommitTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.NullRollbackTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.PrintTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.ResumeNullTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.RollbackExceptionInRollback.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SingleParticipant.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SuspendCommitTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SuspendNullTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SuspendResumeCommitTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SuspendResumeParticipants.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SuspendResumeSingleParticipant.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.SuspendTransaction.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.basic.ThreadedTransaction.class));
        // subtransaction tests
        addTest(new junit.framework.TestSuite(SubtransactionCommit.class));
        addTest(new junit.framework.TestSuite(SubtransactionRollback.class));
        addTest(new junit.framework.TestSuite(SubtransactionCommitRollbackInPrepare.class));
        addTest(new junit.framework.TestSuite(SubtransactionCommitFailInPrepare.class));


        // wst BA tests
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.Cancel.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.Close.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.Compensate.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.ConfirmWithComplete.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.Exit.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.MultiCancel.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.MultiClose.class));
        addTest(new junit.framework.TestSuite(com.arjuna.wst11.tests.junit.ba.MultiCompensate.class));

        // wstx basic tests
        // these don't run at present because the WSCF protocol manager code cannot process the xml
        // in UserTwoPhaseTx.xml and TwoPhaseTxManager.xml and wstx.UserTransactionFactory tries to
        // use these documents to identify the transaction protocol it is meant to support
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.NullCommitTransaction.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.NullNestedCommit.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.NullNestedRollback.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.NullRollbackOnly.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.NullRollbackTransaction.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.SingleParticipant.class));
        //addTest(new junit.framework.TestSuite(com.arjuna.wstx11.tests.junit.basic.SynchronizationParticipant.class));
    }
}