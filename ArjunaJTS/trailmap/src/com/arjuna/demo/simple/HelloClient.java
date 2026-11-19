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
 * $Id: HelloClient.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.simple;

import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.omg.CosTransactions.Current;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.SubtransactionsUnavailable;

/**
 * The <CODE>HelloClient</CODE> is used in the first stage of the JBoss Transactions product trailmap. It is a trivial
 * JTS client application which invokes a remote method within the scope of a transaction.
 */
public class HelloClient
{
    /**
     * The client entry point. This runs a simple client application which connects to a <CODE>HelloImpl</CODE> and
     * transactionally invokes it's service method.
     *
     * @param args These are not used by this application.
     */
    public static void main(String[] args)
    {
        // 0. Define and create the ORB
        // Define an ORB suitable for use by the JBoss Transactions product ORB portability layer.
        ORB myORB = null;
        // Define an object adapter suitable for use by the JBoss Transactions product ORB portability layer.
        RootOA myOA = null;
        try
        {
            // Initialize the ORB reference using the JBoss Transactions product ORB portability layer.
            myORB = ORB.getInstance("ClientSide");
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
            System.err.println("Trailmap Error: ORB Initialisation failed: " + e);
            e.printStackTrace();
            System.exit(0);
        }

        // 1. Obtain a reference to the HelloImpl CORBA representation inorder to be able to use it to transactionally
        // invoke a method to validate the installation of the JBoss Transactions product
        // Define the reference to invoke
        Hello hello = null;
        // Obtain the reference for hello
        try
        {
            // Read the IOR from file
            java.io.FileInputStream file = new java.io.FileInputStream("ObjectId");
            java.io.InputStreamReader input = new java.io.InputStreamReader(file);
            java.io.BufferedReader reader = new java.io.BufferedReader(input);
            // This contains the IOR
            String stringTarget = reader.readLine();

            // Convert the IOR into a CORBA object
            org.omg.CORBA.Object obj = myORB.orb().string_to_object(stringTarget);
            // Narrow the object into an object implementing <CODE>Hello</CODE>
            hello = HelloHelper.narrow(obj);
        }
        catch (java.io.IOException ioe)
        {
            // The IOR could not be read from file
            // Display as much help as possible to the user track down the configuration problem
            System.out.println("Trailmap Error: Could not read the IOR of the HelloImpl (from file ./ObjectId): " + ioe);
            ioe.printStackTrace();
            System.exit(0);
        }

        // 2. Transactionally use the Hello object
        try
        {
            // Obtain a reference to the current transaction and begin it
            System.out.println("Creating a transaction !");
            Current current = OTSManager.get_current();
            current.begin();

            // Transactionally invoke the print_hello operation. This is purely to validate the installation of the product
            System.out.println("Call the Hello Server !");
            hello.print_hello();

            // Complete the transaction normally
            System.out.println("Commit transaction");
            current.commit(false);
        }
        catch (SubtransactionsUnavailable subtransactionsUnavailable)
        {
            // Display as much help as possible to the user track down the configuration problem
            System.err.println("Trailmap Error: Problem during begin: " + subtransactionsUnavailable);
            subtransactionsUnavailable.printStackTrace();
            System.exit(0);
        }
        catch (NoTransaction noTransaction)
        {
            // Display as much help as possible to the user track down the configuration problem
            System.err.println("Trailmap Error: Problem during commit: " + noTransaction);
            noTransaction.printStackTrace();
            System.exit(0);
        }
        catch (HeuristicHazard heuristicHazard)
        {
            // Display as much help as possible to the user track down the configuration problem
            System.err.println("Trailmap Error: Problem during commit: " + heuristicHazard);
            heuristicHazard.printStackTrace();
            System.exit(0);
        }
        catch (HeuristicMixed heuristicMixed)
        {
            // Display as much help as possible to the user track down the configuration problem
            System.err.println("Trailmap Error: Problem during commit: " + heuristicMixed);
            heuristicMixed.printStackTrace();
            System.exit(0);
        }

        // 3. This indicates that the client was succesful in transactionally calling the Hello server program over
        // CORBA
        System.out.println("Done");
    }
}
