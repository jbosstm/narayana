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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserBusinessActivityImple.java,v 1.10.4.1 2005/11/22 10:36:07 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.remote;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mw.wst.common.Environment;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.ba.ContextImple;
import com.arjuna.mwlabs.wst.ba.context.TxContextImple;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ActivationCoordinator;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.RegistrationCoordinator;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.stub.BusinessActivityTerminatorStub;

/**
 * This is the interface that allows transactions to be started and terminated.
 * The messaging layer converts the Commit, Rollback and Notify messages into
 * calls on this.
 *
 * @message com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_1 [com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_1] - Invalid address.
 * @message com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_2 [com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_2] - Received context is null!
 * @message com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_3 [com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_3] - No termination context!
 */

public class UserBusinessActivityImple extends UserBusinessActivity
{
    public UserBusinessActivityImple ()
    {
        try
        {
            _activationCoordinatorService = System.getProperty(Environment.COORDINATOR_URL);

            /*
             * If the coordinator URL hasn't been specified via the
             * configuration file then assume we are using a locally registered
             * implementation.
             */

            if (_activationCoordinatorService == null)
            {
                final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
                _activationCoordinatorService = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_COORDINATOR) ;
            }
        }
        catch (Exception ex)
        {
            // TODO

            ex.printStackTrace();
        }
    }
    
    public void begin () throws WrongStateException, SystemException
    {
    	begin(0);
    }
    
    public void begin (int timeout) throws WrongStateException, SystemException
    {
    	try
    	{
    	    if (_ctxManager.currentTransaction() != null)
        		throw new WrongStateException();
    
    	    com.arjuna.mw.wsc.context.Context ctx = startTransaction(timeout);
    
    	    _ctxManager.resume(new TxContextImple(ctx));
    	}
    	catch (com.arjuna.wsc.InvalidCreateParametersException ex)
    	{
    	    tidyup();
    
    	    throw new SystemException(ex.toString());
    	}
    	catch (com.arjuna.wst.UnknownTransactionException ex)
    	{
    	    tidyup();
    
    	    throw new SystemException(ex.toString());
    	}
    	catch (SystemException ex)
    	{
    	    tidyup();
    
    	    throw ex;
    	}
    }

    public void close () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
    	TxContextImple ctx = null;
    
    	try
    	{
    	    ctx = (TxContextImple) _ctxManager.suspend();
            
            final String id = ctx.identifier() ;
            final EndpointReferenceType terminatorCoordinator = getTerminationCoordinator(ctx) ;
            
    	    BusinessActivityTerminatorStub terminatorStub = new BusinessActivityTerminatorStub(id, terminatorCoordinator);
    	    
    	    terminatorStub.close();
    	}
    	catch (SystemException ex)
    	{
    	    throw ex;
    	}
    	catch (TransactionRolledBackException ex)
    	{
    	    throw ex;
    	}
    	catch (UnknownTransactionException ex)
    	{
    	    throw ex;
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	    
    	    throw new SystemException(ex.toString());
    	}
    	finally
    	{
    	    tidyup();
    	}
    }

    public void cancel () throws UnknownTransactionException, SystemException
    {
    	TxContextImple ctx = null;
    
    	try
    	{
            ctx = (TxContextImple) _ctxManager.suspend();
            
            final String id = ctx.identifier() ;
            final EndpointReferenceType terminatorCoordinator = getTerminationCoordinator(ctx) ;
            
            BusinessActivityTerminatorStub terminatorStub = new BusinessActivityTerminatorStub(id, terminatorCoordinator);
    	    
    	    terminatorStub.cancel();
    	}
    	catch (SystemException ex)
    	{
    	    throw ex;
    	}
    	catch (UnknownTransactionException ex)
    	{
    	    throw ex;
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	    
    	    throw new SystemException(ex.toString());
    	}
    	finally
    	{
    	    tidyup();
    	}
    }

    public void complete () throws UnknownTransactionException, SystemException
    {
    	try
    	{
            final TxContextImple ctx = ((TxContextImple) _ctxManager.currentTransaction()) ;
            final String id = ctx.identifier() ;
            final EndpointReferenceType terminatorCoordinator = getTerminationCoordinator(ctx) ;
            
            BusinessActivityTerminatorStub terminatorStub = new BusinessActivityTerminatorStub(id, terminatorCoordinator);
            
    	    terminatorStub.complete();
    	}
    	catch (SystemException ex)
    	{
    	    throw ex;
    	}
    	catch (UnknownTransactionException ex)
    	{
    	    throw ex;
    	}
    	catch (Exception ex)
    	{
    	    throw new SystemException(ex.toString());
    	}
    }

    public String transactionIdentifier ()
    {
    	try
    	{
    	    return _ctxManager.currentTransaction().toString();
    	}
    	catch (com.arjuna.wst.SystemException ex)
    	{
    	    return "Unknown";
    	}
    	catch (NullPointerException ex)
    	{
    	    return "Unknown";
    	}
    }

    public String toString ()
    {
    	return transactionIdentifier();
    }

    private final com.arjuna.mw.wsc.context.Context startTransaction (int timeout) throws com.arjuna.wsc.InvalidCreateParametersException, SystemException
    {
        try
        {
            final Long expires = (timeout > 0 ? new Long(timeout) : null) ;
            final String messageId = new Uid().stringForm() ;
            final CoordinationContextType coordinationContext = ActivationCoordinator.createCoordinationContext(
                    _activationCoordinatorService, messageId, BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME, expires, null) ;
            if (coordinationContext == null)
            {
                throw new SystemException(
                    wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_2"));
            }
            return new ContextImple(coordinationContext) ;
        }
        catch (final InvalidCreateParametersException icpe)
        {
            throw icpe ;
        }
        catch (final SoapFault sf)
        {
            throw new SystemException(sf.getMessage()) ;
        }
        catch (final Exception ex)
        {
            throw new SystemException(ex.toString());
        }
	}
    
    private EndpointReferenceType getTerminationCoordinator(final TxContextImple ctx)
        throws SystemException
    {
        final CoordinationContextType coordinationContext = ctx.context().getCoordinationContext() ;
        final String messageId = new Uid().stringForm() ;
        try
        {
            return RegistrationCoordinator.register(coordinationContext, messageId,
                getParticipantProtocolService(ctx.identifier()), ArjunaTXConstants.WSARJTX_PROTOCOL_TERMINATION) ;
        }
        catch (final Throwable th)
        {
            throw new SystemException(wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple_3"));
        }
    }
    
    private EndpointReferenceType getParticipantProtocolService(final String id)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String serviceURI = soapRegistry.getServiceURI(ArjunaTXConstants.SERVICE_TERMINATION_PARTICIPANT) ;
        final EndpointReferenceType participant = new EndpointReferenceType(new AttributedURIType(serviceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participant, id) ;
        return participant ;
    }

    private final void tidyup ()
    {
    	try
    	{
    	    _ctxManager.suspend();
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	}
    }
    
    private ContextManager _ctxManager = new ContextManager();
    private String _activationCoordinatorService;
}
