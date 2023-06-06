/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.jts;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.common.util.ConfigurationInfo;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;

public class TransactionServer
{
    public static final void registerTransactionManager (final int resolver, ORB myORB, org.omg.CosTransactions.TransactionFactory theOTS) throws Exception
    {
        final Services myServ = new Services(myORB);

        if (resolver != com.arjuna.orbportability.Services.BIND_CONNECT)
        {
            String[] params = new String[1];

            params[0] = com.arjuna.orbportability.Services.otsKind;

            /*                                                                                                                                           
             * Register using the default mechanism.                                                                                                     
             */

            myServ.registerService(theOTS, com.arjuna.orbportability.Services.transactionService, params, resolver);

            params = null;
        }
    }
    
    public static void main (String[] args)
    {
        try
        {
            doWork(args);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    static void doWork (String[] args) throws Exception
    {
        doWork(args, false);
    }
    
    static void doWork (String[] args, boolean exitOnComplete) throws Exception
    {
	String refFile = com.arjuna.orbportability.Services.transactionService;
	String objectName = null;
	boolean printReady = false;
	ORB myORB = null;
	RootOA myOA = null;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-otsname") == 0)
		objectName = args[i+1];
	    if (args[i].compareTo("-test") == 0)
		printReady = true;
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: [-otsname <name>] [-help] [-version] [-recovery]");
		
		if (exitOnComplete)
		    return;
		else
		    System.exit(0);
	    }
	    if (args[i].compareTo("-version") == 0)
	    {
		System.out.println("TransactionServer version "+ ConfigurationInfo.getVersion());
		
		if (exitOnComplete)
		    return;
		else
		    System.exit(0);
	    }

        if (args[i].compareTo("-recovery") == 0) {
            RecoveryManager.manager().startRecoveryManagerThread();
        }
	}

	com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple theOTS = null;
	final int resolver = Services.getResolver();
	
	try
	{
	    try
	    {
		myORB = ORB.getInstance("TransactionServer");
		myOA = OA.getRootOA(myORB);
	    
		myORB.initORB(args, null);
		myOA.initOA();

		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);
	    }
	    catch (Exception e)
	    {
		System.err.println("Initialisation of TransactionServer failed: "+e);

		throw e;
	    }

	    theOTS = new com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple(objectName);

	    try
	    {
	        registerTransactionManager(resolver, myORB, theOTS.getReference());

                if (!printReady)
                    System.out.println("Transaction manager registered.");
	    }
	    catch (Exception e1)
	    {
		System.err.println("Failed to bind transaction manager: "+e1);
		
		if (exitOnComplete)
		    throw new Exception("Failed to bind transaction manager:" +e1);
		else
		    System.exit(0);
	    }

	    if (printReady)
		System.out.println("Ready");
	    else
		System.out.println("JBossTS OTS Server startup.");

	    if (!exitOnComplete)
	    {
	        if (resolver == com.arjuna.orbportability.Services.BIND_CONNECT)
	            myOA.run(com.arjuna.orbportability.Services.transactionService);
	        else
	            myOA.run();
	    }
	}
	catch (Exception e2)
	{
	    System.err.println("TransactionServer caught exception "+e2);
	}
    
	System.out.println("JBossTS OTS Server shutdown");

	theOTS = null;

	if (myOA != null)
	    myOA.destroy();

	if (myORB != null)
	    myORB.shutdown();
    }
}