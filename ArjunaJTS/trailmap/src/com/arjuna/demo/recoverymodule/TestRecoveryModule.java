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
 * Copyright (C) 2003, 2004
 * Arjuna Technologies Limited
 * Newcastle upon Tyne, UK
 *
 * $Id: TestRecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.demo.recoverymodule;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;

/**
 * BEWARE: Implementing a <CODE>RecoveryModule</CODE> and <CODE>AbstractRecord</CODE> is a very advanced feature of the
 * transaction service. It should only be performed by users familiar with the all the concepts used in the Arjuna
 * Transactions product. Please see the ArjunaCore guide for more information about <CODE>RecoveryModule</CODE>s and
 * <CODE>AbstractRecord</CODE>s.

 * This is the entry point into the JBoss Transactions product advanced trailmap regarding the implementation of a
 * recovery module. It allows a user to create a record within the scope of a transaction and then to commit or
 * abort the transaction. The program is quite verbose and describes as it is running what is happening.
 *
 * The program illustrates the crash recovery capabilities of the JBoss Transactions product. The record type it
 * defines allows us to crash the application during the commit of the resource. We can then see how the recovery
 * manager will (when the application recovers [restarts]) retry to commit the transaction.
 */
public class TestRecoveryModule
{
    /**
     * Runs the recovery module test program. This will create a <CODE>SimpleRecord</CODE> telling it whether to crash
     * or not and then commit/rollback transaction depending upon user choices.
     *
     * @param args  The arguments are not required but can contain any of "-commit" "-rollback" "-crash"
     */
    public static void main(String args[])
    {
        // Initialize the configuration for the test
        boolean commit = true;
        boolean crash = false;

        // Obtain the user choices about the test type to run
        for (int i = 0; i < args.length; i++)
        {
            if ((args[i].compareTo("-commit") == 0))
                commit = true;
            if ((args[i].compareTo("-rollback") == 0))
                commit = false;
            if ((args[i].compareTo("-crash") == 0))
                crash = true;
        }

        // Create a new JBoss transaction
        AtomicAction tx = new AtomicAction();
        // Allocate space to monitor the transaction, this variable is overriden each time the transaction is used.
        int actionStatus = tx.begin(); // Top level begin

        // If the transaction was successful began
        if (actionStatus == ActionStatus.RUNNING)
        {
            // Enlist the participant
            int addOutcome = tx.add(new SimpleRecord(crash));
            // If the participant was succesfully added to the intentions list
            if (addOutcome == AddOutcome.AR_ADDED)
            {
                System.out.println("About to complete the transaction ");
                // Try to complete the transaction as requested by the user
                if (commit)
                    actionStatus = tx.commit();  // Top level commit
                else
                    actionStatus = tx.abort();  // Top level rollback

                System.out.println("The status of the transaction is " + ActionStatus.stringForm(actionStatus));
            }
            else
            {
                // There was an unexpected problem adding the record to the transaction
                System.err.println("There was an unexpected problem calling AtomicAction.add(AbstractRecord), returned: " +
                                   AddOutcome.printString(actionStatus));
            }
        }
        // A problem occured beginning the transaction
        else
        {
            // There was an unexpected problem beginning the transaction
            System.err.println("There was an unexpected problem calling AtomicAction.begin(), returned: " +
                               ActionStatus.stringForm(actionStatus));
        }
    }
}
