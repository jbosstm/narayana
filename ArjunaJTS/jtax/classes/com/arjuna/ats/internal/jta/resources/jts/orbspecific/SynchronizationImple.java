/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import org.omg.CORBA.UNKNOWN;

import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.internal.jta.utils.jts.StatusConverter;
import com.arjuna.ats.internal.jts.ORBManager;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Whenever a synchronization is registered, an instance of this class
 * is used to wrap it.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: SynchronizationImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class SynchronizationImple implements org.omg.CosTransactions.SynchronizationOperations
{

    public SynchronizationImple (jakarta.transaction.Synchronization ptr)
    {
	_theSynch = ptr;
	_theReference = null;
	_theClassLoader = this.getContextClassLoader();
    }

    public final org.omg.CosTransactions.Synchronization getSynchronization ()
    {
        if (_theReference == null)
        {
            _thePOATie = getPOATie();

            ORBManager.getPOA().objectIsReady(_thePOATie);

            _theReference = org.omg.CosTransactions.SynchronizationHelper.narrow(ORBManager.getPOA().corbaReference(_thePOATie));
        }

        return _theReference;
    }

    public void before_completion () throws org.omg.CORBA.SystemException
    {
	if (jtaxLogger.logger.isTraceEnabled()) {
        jtaxLogger.logger.trace("SynchronizationImple.before_completion - Class: " + _theSynch.getClass() + " HashCode: " + _theSynch.hashCode() + " toString: " + _theSynch);
    }

	if (_theSynch != null)
	{
	    ClassLoader origClassLoader = this.getContextClassLoader();

	    try
	    {
		this.setContextClassLoader(_theClassLoader);
		_theSynch.beforeCompletion();
	    }
	    catch (Exception e)
	    {
		jtaxLogger.logger.trace("SynchronizationImple.before_completion failed - toString: " + _theSynch, e);
		throw new UNKNOWN();
	    }
	    finally
	    {
		this.setContextClassLoader(origClassLoader);
	    }
	}
	else
	    throw new UNKNOWN();
    }

    public void after_completion (org.omg.CosTransactions.Status status) throws org.omg.CORBA.SystemException
    {
	if (jtaxLogger.logger.isTraceEnabled()) {
        jtaxLogger.logger.trace("SynchronizationImple.after_completion - Class: " + _theSynch.getClass() + " HashCode: " + _theSynch.hashCode() + " toString: " + _theSynch);
    }

	if (_theSynch != null)
	{
	    int s = StatusConverter.convert(status);
	    ClassLoader origClassLoader = this.getContextClassLoader();

	    try
	    {
		this.setContextClassLoader(_theClassLoader);
		_theSynch.afterCompletion(s);

		if (_theReference != null)
		    ORBManager.getPOA().shutdownObject(_thePOATie);
	    }
	    catch (Exception e)
	    {
		jtaxLogger.logger.trace("SynchronizationImple.after_completion failed - toString: " + _theSynch, e);

		if (_theReference != null)
		    ORBManager.getPOA().shutdownObject(_thePOATie);

		throw new UNKNOWN(); // should not cause any affect!
	    }
	    finally
	    {
		this.setContextClassLoader(origClassLoader);
	    }
	}
	else
	    throw new UNKNOWN(); // should not cause any affect!
    }

    // this is used to allow subclasses to override the Tie type provided.
    // the Tie classes do not inherit from one another even if the business interfaces
    // they correspond to do in the idl. Hence Servant is the only common parent.
    protected org.omg.PortableServer.Servant getPOATie() {
        return new org.omg.CosTransactions.SynchronizationPOATie(this);
    }

    private ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
	    return Thread.currentThread().getContextClassLoader();
	} else {
	    return (ClassLoader)AccessController.doPrivileged(
	        new PrivilegedAction() {
	            public Object run() {
	    	        return Thread.currentThread().getContextClassLoader();
	            }
	        });
	}
    }

    private void setContextClassLoader(final ClassLoader classLoader) {
        if (System.getSecurityManager() == null) {
	    Thread.currentThread().setContextClassLoader(classLoader);
	} else {
	    AccessController.doPrivileged(
	        new PrivilegedAction() {
	            public Object run() {
	    	        Thread.currentThread().setContextClassLoader(classLoader);
			return null;
	            }
	        });
	}
    }

    private jakarta.transaction.Synchronization       _theSynch;
    private org.omg.CosTransactions.Synchronization _theReference;
    private org.omg.PortableServer.Servant _thePOATie;
    private ClassLoader _theClassLoader;
}