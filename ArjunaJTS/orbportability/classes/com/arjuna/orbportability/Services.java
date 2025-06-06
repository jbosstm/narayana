/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.UserException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;

/**
 * An attempt at some ORB portable ways of accessing ORB services.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Services.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Services
{

    /**
     * The various means used to locate a service.
     */

public static final int CONFIGURATION_FILE = 0;
public static final int RESOLVE_INITIAL_REFERENCES = 1;
public static final int NAME_SERVICE = 2;
public static final int FILE = 3;
public static final int NAMED_CONNECT = 4;
public static final int BIND_CONNECT = 5;

public static final String[] BINDING_SERVICES = {"CONFIGURATION_FILE","RESOLVE_INITIAL_REFERENCES","NAME_SERVICE","FILE","NAMED_CONNECT","BIND_CONNECT"};


    /*
     * Could have a method that tries all of them.
     */

    /**
     * Create a Services class which provides standard services for
     * the given ORB instance.
     *
     * @param orb The orb the services should use.
     */
public Services(ORB orb)
    {
        _orb = orb;
    }

    /**
     * The default implementation (specified on a per-ORB basis.)
     */
    
public org.omg.CORBA.Object getService (String serviceName,
					Object[] params) throws org.omg.CORBA.ORBPackage.InvalidName, IOException, SystemException
    {
	switch (bindDefault())
	{
	case Services.RESOLVE_INITIAL_REFERENCES:
	    return getService(serviceName, params, RESOLVE_INITIAL_REFERENCES);
	case Services.NAME_SERVICE:
	    return getService(serviceName, params, NAME_SERVICE);
	case Services.FILE:
	    return getService(serviceName, params, FILE);
	case Services.NAMED_CONNECT:
	    return getService(serviceName, params, NAMED_CONNECT);
	default:
	    return getService( serviceName, params, CONFIGURATION_FILE);
	}
    }

public org.omg.CORBA.Object getService (String serviceName,
					Object[] params,
					int mechanism) throws org.omg.CORBA.ORBPackage.InvalidName, IOException, SystemException
    {
	org.omg.CORBA.Object objRef = null;
	
	switch (mechanism)
	{
	case RESOLVE_INITIAL_REFERENCES:
	    {
		try
		{
		    objRef = _orb.orb().resolve_initial_references(serviceName);
		}
		catch (SystemException ex)
		{
		    throw ex;
		}
		catch (InvalidName exp)
		{
		    throw exp;
		}
		catch (Exception e)
		{

            if (opLogger.logger.isTraceEnabled())
            {
                opLogger.logger.trace("Services.getService - resolve_initial_references on "+serviceName+" failed: "+e.toString());
            }

		    throw new InvalidName();
		}
	    }
	break;
	case NAME_SERVICE:
	    {
		String kind = ((params == null) ? null : (String) params[0]);
		
		try
		{
		    org.omg.CORBA.Object nsRef = _orb.orb().resolve_initial_references(Services.nameService);
	    
		    NamingContext ncRef = NamingContextHelper.narrow(nsRef);
	    
		    // bind the Object Reference in Naming
		    
		    NameComponent nc = new NameComponent(serviceName, kind);
		    NameComponent path[] = {nc};

		    objRef = ncRef.resolve(path);
		}
		catch (SystemException ex)
		{
		    throw ex;
		}
		catch (UserException e)
		{
		    throw new org.omg.CORBA.ORBPackage.InvalidName();
		}
	    }
	break;
	case CONFIGURATION_FILE:
	    {
		String cosservicesRoot = opPropertyManager.getOrbPortabilityEnvironmentBean().getInitialReferencesRoot();
		String configLocation = opPropertyManager.getOrbPortabilityEnvironmentBean().getInitialReferencesFile();
		String configFile = cosservicesRoot+File.separatorChar+configLocation;
		LineNumberReader input;

		try
		{
		    input = new LineNumberReader(new FileReader(configFile));
		}
		catch (FileNotFoundException e)
		{
            if ( opLogger.logger.isTraceEnabled() )
            {
                opLogger.logger.trace("Services.getService could not open config file "+configFile);
            }

		    throw new InvalidName();
		}
    
		String ior = null;
    
		try
		{
		    boolean finished = false;
	    
		    while ((ior == null) && !finished)
		    {
			String line = input.readLine();

			if (line == null)
			    finished = true;
			else
			{
			    int occur = line.indexOf(serviceName);

			    if (occur == 0)  // should be first on line
				ior = line.substring(serviceName.length() +1);  // +1 for space separator
			}
		    }
		    
		    input.close();
		}
		catch (SystemException ex)
		{
		    input.close();
		    throw ex;
		}
		catch (Exception e)
		{
			if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
				opLogger.i18NLogger.warn_Services_unexpectedexception("Services.getService", e); // JBTM-3990
			}

		    input.close();
		    throw new UNKNOWN();
		}

		if (ior == null)
		{
			if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
				opLogger.i18NLogger.warn_Services_servicenotfound(serviceName, configFile); // JBTM-3990
			}

		    throw new InvalidName();
		}
		else
		{
		    try
		    {
			objRef = _orb.orb().string_to_object(ior);
		    }
		    catch (Exception e)
		    {
			objRef = null;
		    }
		}
	    }
	break;
	case FILE:
	    {
		try
		{
		    String fileDir = opPropertyManager.getOrbPortabilityEnvironmentBean().getFileDir();
		    File f = null;

		    if (fileDir != null && fileDir.length() != 0)
			f = new File(fileDir+File.separator+serviceName);
		    else
			f = new File(serviceName);
		    
		    FileInputStream ifile = new FileInputStream(f);
		    int size = (int) f.length();
		    byte b[] = new byte[size];

		    ifile.read(b);
		    ifile.close();
	
		    String objString = new String(b, StandardCharsets.UTF_8);
		    objRef = _orb.orb().string_to_object(objString);

		    objString = null;
		}
		catch (SystemException ex)
		{
		    throw ex;
		}
		catch (FileNotFoundException e)
		{
		    throw new InvalidName();
		}
	    }
	break;
	case NAMED_CONNECT:
	    {
			if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
				opLogger.i18NLogger.warn_Services_unsupportedoption("NAMED_CONNECT"); // JBTM-3990
			}

		throw new BAD_PARAM();
	    }
	}

	return objRef;
    }

    /**
     * The default implementation (probably specified on a per-ORB basis.)
     */
    
public void registerService (org.omg.CORBA.Object objRef,
		             String serviceName,
			     Object[] params) throws org.omg.CORBA.ORBPackage.InvalidName, IOException, SystemException
    {
	switch (bindDefault())
	{
	case Services.CONFIGURATION_FILE:
	    registerService(objRef, serviceName, params, CONFIGURATION_FILE);
	    break;
	case Services.RESOLVE_INITIAL_REFERENCES:
	    registerService(objRef, serviceName, params, RESOLVE_INITIAL_REFERENCES);
	    break;
	case Services.NAME_SERVICE:
	    registerService(objRef, serviceName, params, NAME_SERVICE);
	    break;
	case Services.NAMED_CONNECT:
	    registerService(objRef, serviceName, params, NAMED_CONNECT);
	    break;
	default:
	    registerService(objRef, serviceName, params, CONFIGURATION_FILE);
	}
    }

public void registerService (org.omg.CORBA.Object objRef,
		             String serviceName, Object[] params,
		             int mechanism) throws org.omg.CORBA.ORBPackage.InvalidName, IOException, SystemException
    {
	switch (mechanism)
	{
	case RESOLVE_INITIAL_REFERENCES:
	    {
		throw new BAD_PARAM();
	    }
	case NAME_SERVICE:
	    {
		String kind = ((params == null) ? null : (String) params[0]);
		
		try
		{
		    org.omg.CORBA.Object initServ = _orb.orb().resolve_initial_references(Services.nameService);
		    NamingContext rootContext = NamingContextHelper.narrow(initServ);
		    NameComponent[] contextName = new NameComponent[1];
		    contextName[0] = new NameComponent(serviceName, kind);
	    
		    rootContext.rebind(contextName, objRef);

                    if (opLogger.logger.isTraceEnabled()) {
                        opLogger.logger.trace("Services.registerService - object " + serviceName + " registered with name service.");
                    }
		}
		catch (org.omg.CORBA.ORBPackage.InvalidName e)
		{
		    throw new InvalidName();
		}
		catch (UserException e)
		{
		    throw new InvalidName();
		}
		catch (SystemException e)
		{
		    throw e;
		}
	    }
	break;
	case CONFIGURATION_FILE:
	    {
		String cosservicesRoot = opPropertyManager.getOrbPortabilityEnvironmentBean().getInitialReferencesRoot();
		String configLocation = opPropertyManager.getOrbPortabilityEnvironmentBean().getInitialReferencesFile();
		String configFile = cosservicesRoot+File.separatorChar+configLocation;
		LineNumberReader input = null;
		String objString = _orb.orb().object_to_string(objRef);
		File currFile = null;
		String newFileName = configFile+Services.tmpFile;
		
		try
		{
		    currFile = new File(configFile);
		    input = new LineNumberReader(new FileReader(currFile));
		}
		catch (FileNotFoundException e)
		{
            if ( opLogger.logger.isTraceEnabled() )
            {
                opLogger.logger.trace("Services.registerService could not open config file "+configFile);
            }

		    currFile = null;
		    input = null;

		    /*
		     * File not present, so this must be the first
		     * entry.
		     */
		    
		    newFileName = configFile;  // file does not exist, so write directly.
		}

		File nuFile = new File(newFileName);
		PrintWriter outputFile = new PrintWriter(new FileOutputStream(nuFile), true);
		boolean found = false;

		if (input != null)
		{
		    String line = null;

		    do
		    {
			line = input.readLine();

			if (line != null)
			{
			    int occur = line.indexOf(serviceName);

			    if (occur == 0)  // should be first on line
			    {
				if (line.substring(serviceName.length() +1) != null) // found old line?
				{
				    found = true;
				    outputFile.println(serviceName+separator+objString);
				}
			    }
			    else
				outputFile.println(line);

			    outputFile.flush();
			}
			
		    } while (line != null);
		}

		if (!found)
		{
		    outputFile.println(serviceName+separator+objString);
		    outputFile.flush();
		}

		outputFile.close();

		if (input != null)  // file existed.
		{
		    input.close();
		    
		    if (currFile.exists())
		    {
			currFile.delete();
		    }
		    
		    nuFile.renameTo(currFile);
		    input = null;
		}
		
		newFileName = null;
		outputFile = null;
		nuFile = null;

                if (opLogger.logger.isTraceEnabled()) {
                    opLogger.logger.trace("Services.registerService - object " + serviceName + " registered with configuration file: " + configFile);
                }
	    }
	break;
	case FILE:
	    {
		String fileDir = opPropertyManager.getOrbPortabilityEnvironmentBean().getFileDir();
		FileOutputStream ofile = null;

		if (fileDir != null && fileDir.length() != 0)
		    ofile = new FileOutputStream(fileDir+File.separator+serviceName);
		else
		    ofile = new FileOutputStream(serviceName);
		
		String objString = _orb.orb().object_to_string(objRef);
		byte b[] = objString.getBytes(StandardCharsets.UTF_8);

		ofile.write(b);
		ofile.close();

                if (opLogger.logger.isTraceEnabled()) {
                    opLogger.logger.trace("Services.registerService - object " + serviceName + " reference file created: " + fileDir + serviceName);
                }
	    }
	break;
	case NAMED_CONNECT:
	    {
			if (arjPropertyManager.getCoreEnvironmentBean().isLogAndRethrow()) {
				opLogger.i18NLogger.warn_Services_optionnotsupported("Services.registerService", "NAMED_CONNECT"); // JBTM-3990
			}

		throw new BAD_PARAM();
	    }
	default:
	    break;
	}
    }

    /**
     * @return the list of initial services that the ORB knows about. Only
     * the names are returned, not the IORs.
     *
     * @since JTS 2.1.1.
     *
     * getService/setService for INITIAL_REFERENCES could default to
     * config file if not supported. Transparently.
     */

public final String[] listInitialServices () throws IOException, SystemException
    {
	/*
	 * First check the ORB initial services.
	 */

	String[] services = _orb.orb().list_initial_services();

	/*
	 * Now check the configuration file equivalent.
	 */

	String cosservicesRoot = opPropertyManager.getOrbPortabilityEnvironmentBean().getInitialReferencesRoot();
	String configLocation = opPropertyManager.getOrbPortabilityEnvironmentBean().getInitialReferencesFile();
	String configFile = cosservicesRoot+File.separatorChar+configLocation;
	LineNumberReader input = null;

	try
	{
	    input = new LineNumberReader(new FileReader(new File(configFile)));
	}
	catch (FileNotFoundException e)
	{
	    // assume not in use.

	    input = null;
	}

	Vector otherServices = new Vector();
	
	if (input != null)
	{
	    String line = null;
	    
	    do
	    {
		line = input.readLine();
		
		if (line != null)
		{
		    int index = line.indexOf(separator);

		    if (index != -1)  // should always be the case
		    {
			otherServices.add(new String(line.substring(0, index)));
		    }
		    else
		    {
                opLogger.i18NLogger.warn_Services_suspectentry("Services.listInitialServices", line);
		    }
		}
	    }
	    while (line != null);
	    input.close();
	}

	String[] completeServices = null;
	
	int totalSize = ((services == null) ? otherServices.size() : services.length + otherServices.size());
	
	if (totalSize > 0)
	{
	    int index = 0;
	    
	    completeServices = new String[totalSize];

	    if (services.length > 0)
	    {
		for (index = 0; index < services.length; index++)
		    completeServices[index] = services[index];
	    }
	    
	    for (int j = index; j < otherServices.size(); j++)
	    {
		completeServices[j + index] = (String) otherServices.elementAt(j);
	    }
	}
	
	return completeServices;
    }

public final static String bindString (int bindValue)
    {
	switch (bindValue)
	{
	case CONFIGURATION_FILE:
	    return "CONFIGURATION_FILE";
	case RESOLVE_INITIAL_REFERENCES:
	    return "RESOLVE_INITIAL_REFERENCES";
	case NAME_SERVICE:
	    return "NAME_SERVICE";
	case FILE:
	    return "FILE";
	case NAMED_CONNECT:
	    return "NAMED_CONNECT";
	case BIND_CONNECT:
	    return "BIND_CONNECT";
	default:
	    return "Unknown";
	}
    }

/**
 * Given a binding mechanism string name return the 
 * enumerated value.  If this is not a valid binding
 * mechanism name then return -1.
 *
 * @param bindString The binding mechanism string name to look up.
 * @return The enumerated value that this bind string represents.
 */
public final static int bindValue(String bindString)
    {
    	int bindValue = -1;
    	
    	for (int count=0;count<BINDING_SERVICES.length;count++)
    	{
    	    if (BINDING_SERVICES[count].equalsIgnoreCase(bindString))
    	    {
    	    	bindValue = count;
    	    }
    	}
    	
    	return(bindValue);
    }

    public static final int getResolver ()
    {
        int resolver = bindDefault();
        final String resolveService = opPropertyManager.getOrbPortabilityEnvironmentBean().getResolveService();

        if (resolveService != null)
        {
            if (resolveService.compareTo("NAME_SERVICE") == 0)
                resolver = com.arjuna.orbportability.Services.NAME_SERVICE;
            else
            {
                if (resolveService.compareTo("BIND_CONNECT") == 0)
                    resolver = com.arjuna.orbportability.Services.BIND_CONNECT;
                else
                {
                    if (resolveService.compareTo("FILE") == 0)
                        resolver = com.arjuna.orbportability.Services.FILE;
                    else
                    {
                        if (resolveService.compareTo("RESOLVE_INITIAL_REFERENCES") == 0)
                            resolver = com.arjuna.orbportability.Services.RESOLVE_INITIAL_REFERENCES;
                    }
                }
            }
        }

        return resolver;
    }

    /**
     * @return the default bind mechanism. TODO
     */
    private static synchronized final int bindDefault ()
    {
        if (_bindMethod == -1)
        {
            opLogger.i18NLogger.warn_common_Configuration_bindDefault_invalidbind("com.arjuna.orbportability.common.Configuration.bindDefault()");
        }

        return _bindMethod;
    }

    private static final int _bindMethod = Services.bindValue(opPropertyManager.getOrbPortabilityEnvironmentBean().getBindMechanism());

    	    	
public static final String nameService = "NameService";
public static final String transactionService = "TransactionManagerService";

public static final String otsKind = "OTS";

private static final String tmpFile = ".tmp";
private static final String separator = " ";

private ORB _orb = null;
}
