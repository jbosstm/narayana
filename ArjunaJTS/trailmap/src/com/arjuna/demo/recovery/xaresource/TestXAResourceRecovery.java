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
* Copyright (C) 2004
* Arjuna Technologies Limited
* Newcastle upon Tyne, UK
*
* $Id: TestXAResourceRecovery.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.demo.recovery.xaresource;

import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

/**
 * The TestXAResourceRecovery class is used to run the JBoss Transactions product trailmap example which demonstrates
 * the reliability of the JBoss Transactions product. The test will use some XA resources which crash after returning
 * PREPARE_OK to the transaction manager, i.e are in the ready to commit phase. Rerunning the test will then see the
 * resources recover and be committed.
 */
public class TestXAResourceRecovery
{
   /**
    * This object is used to wait for the XAResources to recover.
    */
   private static final Object lock = new Object();

   /**
    * The number of XAResources that have recovered.
    */
   private static int resourceRecoveryCount = 0;

   /**
    * This sample application will either:
    * <ol>
    *  <li>Create a serializable and a non serializable XA resources which crash during commit</li>
    *  <li>Wait for the crashed XA resources to recover</li>
    * </ol>
    * It can also be configured to use an external recovery manager or create one to run in-VM (that is the default behaviour)
    *
    * @param args          The arguments allow the test to be configured in one of the above modes, the following
    *                      parameters are supported "-waitForRecovery" "-useExternalRecoveryManager" "-help".
    */
   public static void main(String[] args)
   {
      // The recovery manager if locally run.
      RecoveryManagerImple rcm = null;

      // Should the test use an external waitForRecovery manager or start one locally
      boolean              useExternalRecoveryManager = false;

      // Should the test wait for waitForRecovery or create objects to be recovered
      boolean              waitForRecovery = false;

      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals("-waitForRecovery"))
         {
            waitForRecovery = true;
         }
         if (args[i].equals("-useExternalRecoveryManager"))
         {
            useExternalRecoveryManager = true;
         }
         if (args[i].equals("-help"))
         {
            // Display help information about the parameters supported by this trailmap example
            System.out.println("Usage: java TestXAResourceRecovery [-waitForRecovery] [-useExternalRecoveryManager] [-help]");
            System.exit(0);
         }
      }

      // Define an ORB suitable for use by the JBoss Transactions product ORB portability layer.
      ORB myORB = null;
      // Define an object adapter suitable for use by the JBoss Transactions product ORB portability layer.
      RootOA myOA = null;
      try
      {
         // Initialize the ORB reference using the JBoss Transactions product ORB portability layer.
         myORB = ORB.getInstance("test");
         // Initialize the object adapter reference using the JBoss Transactions product ORB portability layer.
         myOA = OA.getRootOA(myORB);
         // Initialize the ORB using the JBoss Transactions product ORB portability layer.
         myORB.initORB(args, null);
         // Initialize the object adapter reference using the JBoss Transactions product ORB portability layer.
         myOA.initOA();
      }
      catch (Exception e)
      {
         // The ORB has not been correctly configured!
         // Display as much help as possible to the user track down the configuration problem
         System.err.println("TestXAResourceRecover Trailmap Error: ORB Initialisation failed: " + e);
         e.printStackTrace();
         System.exit(0);
      }

      // If the Recovery manager is to be ran in VM
      if (!useExternalRecoveryManager)
      {
         // Start a new Recovery manager in an additional thread
         rcm = new com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple(true);
      }

      // If this test run is creating the objects to be recovered
      if (!waitForRecovery)
      {
         try
         {
            // Get a reference to the user transaction object
            UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();
            // Begin the transaction
            ut.begin();

            // Get a reference to the current JTA transaction suitable for enlisting the XA resources within
            Transaction txImple = TransactionManager.transactionManager().getTransaction();

            // Enlist resource 1
            txImple.enlistResource(new ExampleXAResource("Serializable_Resource"));
            // Enlist resource 2
            txImple.enlistResource(new NonSerializableExampleXAResource("Non_Serializable_Resource", false));

            // Commit the UserTransaction, this call should kill the VM
            ut.commit();

            // Warm the user that the VM is unexpectedly not dead
            System.err.println("TestXAResourceRecovery Trailmap Error: The VM should now be dead");


         }
         catch (NotSupportedException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (NotSupportedException) from UserTransaction.begin(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
         catch (SystemException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (SystemException) from one of UserTransaction.begin(), TransactionManager.getTransaction(), Transaction.enlistResource(), UserTransaction.commit(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
         catch (RollbackException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (RollbackException) from one of Transaction.enlistResource(), UserTransaction.commit(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
         catch (IllegalStateException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (IllegalStateException) from one of Transaction.enlistResource(), UserTransaction.commit(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
         catch (HeuristicMixedException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (HeuristicMixedException) from UserTransaction.commit(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
         catch (HeuristicRollbackException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (HeuristicRollbackException) from UserTransaction.commit(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
         catch (SecurityException e)
         {
            System.err.println("TestXAResourceRecovery Trailmap XA problem (SecurityException) from UserTransaction.commit(): " + e);
            e.printStackTrace();
            System.exit(0);
         }
      }
      else
      {
         // Wait for both XA resources to be recovered by the recovery manager
         synchronized (lock)
         {
            while (resourceRecoveryCount != 2)
            {
               try
               {
                  System.out.println("TestXAResourceRecovery: Waiting for a resource to recover");
                  // Wait for the next resource to recover
                  lock.wait();
               }
               catch (InterruptedException e)
               {
                  System.err.println("TestXAResourceRecovery: Unexpectedly interrupted waiting for recovery: " + e.getMessage());
                  e.printStackTrace();
                  System.exit(0);
               }
            }
         }

         // Display information to indicate that the trailmap ran OK
         System.out.println("TestXAResourceRecovery trailmap example is now successfully completed!");
         // If the Recovery manager was ran in this VM
         if( rcm != null )
         {
            rcm.stop(true);
         }
      }
   }

   /**
    * This method should be invoked when the resource has commited. It will allow the program to complete.
    */
   static void notifyRecoveredResource()
   {
      synchronized (lock)
      {
         System.out.println("TestXAResourceRecovery: Notified by a resource recovering");
         resourceRecoveryCount++;
         lock.notify();
      }
   }
}
