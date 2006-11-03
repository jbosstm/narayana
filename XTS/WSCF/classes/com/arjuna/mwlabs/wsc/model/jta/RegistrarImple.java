/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: RegistrarImple.java,v 1.7.4.1 2005/11/22 10:34:10 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.jta;

import java.util.HashMap;

import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mw.wscf.model.xa.TransactionManagerFactory;
import com.arjuna.mwlabs.wsc.model.jta.participants.JTAParticipant;
import com.arjuna.mwlabs.wsc.model.jta.participants.JTASynchronization;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.Registrar;
import com.arjuna.wsc.RegistrarMapper;

public class RegistrarImple implements Registrar
{

    public RegistrarImple ()
    {
    	try
    	{
    	    _coordManager = TransactionManagerFactory.transactionManager();
            
            final RegistrarMapper mapper = RegistrarMapper.getFactory() ;

            mapper.addRegistrar(_2PCProtocolId, this);
            mapper.addRegistrar(_synchProtocolId, this);
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	}
        _coordinatorAddress = new EndpointReferenceType(new AttributedURIType(System.getProperty(JTA_DEPLOYMENT_URL)));
    }

    /**
     * Called when a registrar is added to a register mapper. This method will be called multiple times if the
     * registrar is added to multiple register mappers or to the same register mapper with different protocol
     * identifiers.
     *
     * @param protocolIdentifier the protocol identifier
     */

    public void install (String protocolIdentifier)
    {
    }

    // TODO need to be able to specify the specific transaction

    /**
     * Registers the interest of participant in a particular protocol.
     *
     * @param participantProtocolServiceAddress the address of the participant protocol service
     * @param protocolIdentifier the protocol identifier
     *
     * @return the PortReference of the coordinator protocol service
     *
     * @throws AlreadyRegisteredException if the participant is already registered for this coordination protocol under
     *         this activity identifier
     * @throws InvalidProtocolException if the coordination protocol is not supported
     * @throws InvalidStateException if the state of the coordinator no longer allows registration for this
     *         coordination protocol
     * @throws NoActivityException if the activity does not exist
     *
     * @message com.arjuna.mwlabs.wsc.model.jta.RegistrarImple_1 [com.arjuna.mwlabs.wsc.model.jta.RegistrarImple_1] - Invalid type URI: < {0} , {1} >
     */

    public EndpointReferenceType register (EndpointReferenceType participantProtocolService, String protocolIdentifier, InstanceIdentifier instanceIdentifier) throws AlreadyRegisteredException, InvalidProtocolException, InvalidStateException, NoActivityException
    {
        if (instanceIdentifier == null)
            throw new NoActivityException();
        
    	Transaction transaction = (Transaction) _hierarchies.get(instanceIdentifier.getInstanceIdentifier());
	
    	if (transaction == null)
    	    throw new NoActivityException();
	
    	if (protocolIdentifier.equals(_2PCProtocolId))
    	{
    	    // enlist participant that wraps the requester URI.
    
    	    try
    	    {
    	        _coordManager.resume(transaction);
    	    }
    	    catch (javax.transaction.InvalidTransactionException ex)
    	    {
    	        throw new NoActivityException();
    	    }
    	    catch (javax.transaction.SystemException ex)
    	    {
    	        throw new InvalidProtocolException();
    	    }
    
    	    try
    	    {
        		// TODO check for AlreadyRegisteredException
        
        		_coordManager.getTransaction().enlistResource(new JTAParticipant(participantProtocolService));
        		
        		_coordManager.suspend();
        		
                return _coordinatorAddress ;
    	    }
    	    catch (RollbackException ex)
    	    {
         		throw new InvalidStateException();
    	    }
    	    catch (IllegalStateException ex)
    	    {
        		throw new InvalidStateException();
    	    }
    	    catch (javax.transaction.SystemException ex)
    	    {
        		throw new InvalidStateException();
    	    }
    	}
    	else
    	{
    	    if (protocolIdentifier.equals(_synchProtocolId))
    	    {
        		try
        		{
        		    _coordManager.getTransaction().registerSynchronization(new JTASynchronization(participantProtocolService));
        
        		    _coordManager.suspend();
        
                    return _coordinatorAddress ;
        		}
        		catch (Exception ex)
        		{
        		    throw new InvalidStateException();
        		}
    	    }
    	    else
    	    {
        		wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsc.model.jta.RegistrarImple_1",
        					      new Object[]{_coordinationTypeURI, protocolIdentifier});
        
        		throw new InvalidProtocolException();
    	    }
    	}
    }

    /**
     * Called when a registrar is removed from a register mapper. This method will be called multiple times if the
     * registrar is removed from multiple register mappers or from the same register mapper with different protocol
     * identifiers.
     *
     * @param serviceAddress the address of the service
     * @param protocolIdentifier the protocol identifier
     */

    public void uninstall (String protocolIdentifier)
    {
    }

    public final void associate () throws Exception
    {
	String txIdentifier = _coordManager.getTransaction().toString();
	Transaction tx = _coordManager.suspend();

	_hierarchies.put(txIdentifier, tx);
    }

    public final void disassociate (String txIdentifier) throws Exception
    {
	_hierarchies.remove(txIdentifier);
    }

    public final EndpointReferenceType address ()
    {
    return _coordinatorAddress;
    }

    private TransactionManager    _coordManager;
    private HashMap               _hierarchies = new HashMap();
    private EndpointReferenceType _coordinatorAddress;

    public static final String JTA_DEPLOYMENT_URL = "com.arjuna.mwlabs.wsc.model.jta.deploymentURL";
    
    public static final String _coordinationTypeURI = "urn:arjuna:jta";
    public static final String _2PCProtocolId = "urn:arjuna:jta#2pc";
    public static final String _synchProtocolId = "urn:arjuna:jta#synch";

}
