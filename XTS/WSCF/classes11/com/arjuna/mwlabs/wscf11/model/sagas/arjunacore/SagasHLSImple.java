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
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SagasHLSImple.java,v 1.3.4.1 2005/11/22 10:34:09 kconner Exp $
 */

package com.arjuna.mwlabs.wscf11.model.sagas.arjunacore;

import com.arjuna.mw.wscf.model.sagas.api.*;
import com.arjuna.mw.wscf.model.sagas.hls.SagasHLS;

import com.arjuna.mw.wscf.protocols.ProtocolRegistry;

import com.arjuna.mw.wsas.context.soap.SOAPContext;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wscf.api.UserCoordinatorService;

import com.arjuna.mw.wsas.context.Context;

import com.arjuna.mw.wsas.ActivityManagerFactory;

import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.HLS;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;
import com.arjuna.mw.wsas.exceptions.HLSError;

import com.arjuna.mw.wscf.exceptions.*;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.CoordinatorServiceImple;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.CoordinatorControl;
import com.arjuna.mwlabs.wscf.utils.ContextProvider;
import com.arjuna.mwlabs.wscf.utils.HLSProvider;

/**
 * The ArjunaCore coordination service implementation.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SagasHLSImple.java,v 1.3.4.1 2005/11/22 10:34:09 kconner Exp $
 * @since 1.0.
 */

@HLSProvider(serviceType = SagasHLSImple.serviceType)
public class SagasHLSImple implements SagasHLS, UserCoordinatorService
{
    public final static String serviceType = "Sagas11HLS";
    public final static String coordinationType = "http://docs.oasis-open.org/ws-tx/wsba/2006/06/AtomicOutcome";

    public SagasHLSImple()
    {
	try
	{
	    ActivityManagerFactory.activityManager().addHLS((HLS) this);
	}
	catch (Exception ex)
	{
	    throw new HLSError(ex.toString());
	}

	_coordinatorService = new CoordinatorServiceImple();
	_coordManager = new CoordinatorControl();
    }

    public UserCoordinatorService coordinatorService ()
    {
	return this;
    }

    public UserCoordinator userCoordinator ()
    {
	return _coordinatorService;
    }

    public CoordinatorManager coordinatorManager ()
    {
	return _coordinatorService;
    }

    /**
     * An activity has begun and is active on the current thread.
     */

    public void begun () throws SystemException
    {
	_coordManager.begin();
    }

    /**
     * The current activity is completing with the specified completion status.
     *
     * @param cs The completion status to use.
     *
     * @return The result of terminating the relationship of this HLS and
     * the current activity.
     */

    public Outcome complete (CompletionStatus cs) throws SystemException
    {
	return _coordManager.complete(cs);
    }

    /**
     * The activity has been suspended.
     */

    public void suspended () throws SystemException
    {
	_coordManager.suspend();
    }

    /**
     * The activity has been resumed on the current thread.
     */

    public void resumed () throws SystemException
    {
	_coordManager.resume();
    }

    /**
     * The activity has completed and is no longer active on the current
     * thread.
     */

    public void completed () throws SystemException
    {
	_coordManager.completed();
    }

    /**
     * We identify the HLS by the name of the coordination protocol it supports.
     */

    public String identity () throws SystemException
    {
        return serviceType;
    }

    /**
     * The activity service maintains a priority ordered list of HLS
     * implementations. If an HLS wishes to be ordered based on priority
     * then it can return a non-negative value: the higher the value,
     * the higher the priority and hence the earlier in the list of HLSes
     * it will appear (and be used in).
     *
     * @return a positive value for the priority for this HLS, or zero/negative
     * if the order is not important.
     */

    public int priority () throws SystemException
    {
	return 0;
    }

    /**
     * Return the context augmentation for this HLS, if any on the current
     * activity.
     *
     * @return a context object or null if no augmentation is necessary.
     */

    public Context context () throws SystemException
    {
        ensureContextInitialised();
        if (CONTEXT_IMPLE_CLASS != null) {
            try {
                SOAPContext ctx = (SOAPContext) CONTEXT_IMPLE_CLASS.newInstance();

                ctx.initialiseContext(_coordManager.currentCoordinator());

                return ctx;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new SystemException(ex.toString());
            }
        } else {
            throw new SystemException("Unable to create SOAPContext for SAGAS 1.1 service");
        }
    }

    private void ensureContextInitialised() throws SystemException
    {
        if (!initialised) {
            synchronized(this) {
                if (!initialised) {
                    // we  only do this once no matter what happens
                    initialised = true;
                    try {
                        Class<?> factoryClass = ProtocolRegistry.sharedManager().getProtocolImplementation(coordinationType).getClass();
                        ContextProvider contextProvider = factoryClass.getAnnotation(ContextProvider.class);
                        String providerServiceType = contextProvider.serviceType();
                        if (!providerServiceType.equals(serviceType)) {
                            throw new SystemException("Invalid serviceType for SOAPContext factory registered for SAGAS 1.1 service expecting " + serviceType + " got " + providerServiceType);
                        }
                        Class contextClass = contextProvider.contextImplementation();
                        if (!SOAPContext.class.isAssignableFrom(contextClass)) {
                            throw new SystemException("SOAPContext factory registered for SAGAS 1.1 service provides invalid context implementation");
                        }
                        CONTEXT_IMPLE_CLASS = contextClass;
                    } catch (ProtocolNotRegisteredException pnre) {
                        throw new SystemException("No SOAPContext factory registered for SAGAS 1.1 service");
                    }
                }
            }
        }
    }
    /**
     * If the application requires and if the coordination protocol supports
     * it, then this method can be used to execute a coordination protocol on
     * the currently enlisted participants at any time prior to the termination
     * of the coordination scope.
     *
     * This implementation only supports coordination at the end of the
     * activity.
     *
     * @param cs The completion status to use when determining
     * how to execute the protocol.
     *
     * @exception com.arjuna.mw.wsas.exceptions.WrongStateException Thrown if the coordinator is in a state
     * the does not allow coordination to occur.
     * @exception com.arjuna.mw.wsas.exceptions.ProtocolViolationException Thrown if the protocol is violated
     * in some manner during execution.
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any other error occurs.
     *
     * @return The result of executing the protocol, or null.
     */

    public Outcome coordinate (CompletionStatus cs) throws WrongStateException, ProtocolViolationException, SystemException
    {
	return _coordManager.coordinate(cs);
    }

    /**
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any error occurs.
     *
     * @return the status of the current coordinator. If there is no
     * activity associated with the thread then NoActivity
     * will be returned.
     *
     * @see com.arjuna.mw.wsas.status.Status
     */

    public com.arjuna.mw.wsas.status.Status status () throws SystemException
    {
	return _coordManager.status();
    }

    /**
     * Not supported by basic ArjunaCore.
     *
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any error occurs.
     *
     * @return the complete list of qualifiers that have been registered with
     * the current coordinator.
     */

    public Qualifier[] qualifiers () throws NoCoordinatorException, SystemException
    {
	return _coordManager.qualifiers();
    }

    /**
     * @exception com.arjuna.mw.wsas.exceptions.SystemException Thrown if any error occurs.
     *
     * @return The unique identity of the current coordinator.
     */

    public CoordinatorId identifier () throws NoCoordinatorException, SystemException
    {
	return _coordManager.identifier();
    }

    public static String className ()
    {
    	return SagasHLSImple.class.getName();
    }

    private static Class<?> CONTEXT_IMPLE_CLASS = null;
    private static boolean initialised = false;

    private CoordinatorControl      _coordManager;
    private CoordinatorServiceImple _coordinatorService;

}