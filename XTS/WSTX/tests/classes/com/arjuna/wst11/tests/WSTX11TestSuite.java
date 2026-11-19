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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
            // wst basic tests
            // this test fails because the commit is retried forever instead of an exception being generated
            //addTest(new junit.framework.TestSuite(CommitExceptionInCommit.class));
            com.arjuna.wst11.tests.junit.basic.CommitExceptionInPrepare.class,

            com.arjuna.wst11.tests.junit.basic.CommitRollbackInPrepare.class,
            com.arjuna.wst11.tests.junit.basic.MultiParticipants.class,
            com.arjuna.wst11.tests.junit.basic.NestedTransaction.class,
            com.arjuna.wst11.tests.junit.basic.NullCommitTransaction.class,
            com.arjuna.wst11.tests.junit.basic.NullRollbackTransaction.class,
            com.arjuna.wst11.tests.junit.basic.PrintTransaction.class,
            com.arjuna.wst11.tests.junit.basic.ResumeNullTransaction.class,
            com.arjuna.wst11.tests.junit.basic.RollbackExceptionInRollback.class,
            com.arjuna.wst11.tests.junit.basic.SingleParticipant.class,
            com.arjuna.wst11.tests.junit.basic.SuspendCommitTransaction.class,
            com.arjuna.wst11.tests.junit.basic.SuspendNullTransaction.class,
            com.arjuna.wst11.tests.junit.basic.SuspendResumeCommitTransaction.class,
            com.arjuna.wst11.tests.junit.basic.SuspendResumeParticipants.class,
            com.arjuna.wst11.tests.junit.basic.SuspendResumeSingleParticipant.class,
            com.arjuna.wst11.tests.junit.basic.SuspendTransaction.class,
            com.arjuna.wst11.tests.junit.basic.ThreadedTransaction.class,
            // subtransaction tests
            SubtransactionCommit.class,
            SubtransactionRollback.class,
            SubtransactionCommitRollbackInPrepare.class,
            SubtransactionCommitFailInPrepare.class,


            // wst BA tests
            com.arjuna.wst11.tests.junit.ba.Cancel.class,
            com.arjuna.wst11.tests.junit.ba.Close.class,
            com.arjuna.wst11.tests.junit.ba.Compensate.class,
            com.arjuna.wst11.tests.junit.ba.ConfirmWithComplete.class,
            com.arjuna.wst11.tests.junit.ba.Exit.class,
            com.arjuna.wst11.tests.junit.ba.MultiCancel.class,
            com.arjuna.wst11.tests.junit.ba.MultiClose.class,
            com.arjuna.wst11.tests.junit.ba.MultiCompensate.class
    })
public class WSTX11TestSuite
{
}