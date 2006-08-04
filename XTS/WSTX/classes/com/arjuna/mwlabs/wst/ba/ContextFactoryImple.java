/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * $Id: ContextFactoryImple.java,v 1.4.4.1 2005/11/22 10:36:14 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba;

import com.arjuna.mw.wscf.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wst.common.Protocols;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.ba.context.ArjunaContextImple;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;

public class ContextFactoryImple implements ContextFactory
{

    public ContextFactoryImple ()
    {
        try
        {
            _coordManager = CoordinatorManagerFactory.coordinatorManager();

            _theRegistrar = new RegistrarImple();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Called when a context factory is added to a context factory mapper. This method will be called multiple times
     * if the context factory is added to multiple context factory mappers or to the same context mapper with different
     * protocol identifiers.
     *
     * @param coordinationTypeURI the coordination type uri
     */

    public void install (final String coordinationTypeURI)
    {
    }

    // TODO interposition

    /*
     * If there is a context passed through to create then this newly created
     * coordinator should be interposed.
     */

    /**
     * Creates a coordination context.
     *
     * @param coordinationTypeURI the coordination type uri
     * @param expires the expire date/time for the returned context, can be null
     * @param currentContext the current context, can be null
     *
     * @return the created coordination context
     *
     * @throws InvalidCreateParametersException if a parameter passed is invalid
     *         this activity identifier.
     *
     * @message com.arjuna.mwlabs.wst.ba.ContextFactoryImple_1 [com.arjuna.mwlabs.wst.ba.ContextFactoryImple_1] - Invalid type URI: < {0} , {1} >
     * @message com.arjuna.mwlabs.wst.ba.ContextFactoryImple_3 [com.arjuna.mwlabs.wst.ba.ContextFactoryImple_3] - Invalid type URI: 
     */

    public CoordinationContextType create (final String coordinationTypeURI,
            final Long expires, final CoordinationContextType currentContext)
        throws InvalidCreateParametersException
    {
        if (Protocols.BusinessActivityAtomic.equals(coordinationTypeURI))
    	{
    	    try
    	    {
    		// make sure no transaction is currently associated
    
    		_coordManager.suspend();
    		
    		final int timeout ;
            if (expires == null)
            {
                timeout = 0 ;
            }
            else
            {
                final long longTimeout = expires.longValue() ;
                timeout = (longTimeout > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)longTimeout) ;
            }
    		
    		_coordManager.begin(timeout);
    
            final ArjunaContextImple arjunaContext = ArjunaContextImple.getContext() ;
            final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
            final String registrationCoordinatorURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
            
            final CoordinationContextType coordinationContext = new CoordinationContextType() ;
            coordinationContext.setCoordinationType(new URI(coordinationTypeURI)) ;
            coordinationContext.setIdentifier(new AttributedURIType(arjunaContext.getTransactionIdentifier())) ;
            final int transactionExpires = arjunaContext.getTransactionExpires() ;
            if (transactionExpires > 0)
            {
                coordinationContext.setExpires(new AttributedUnsignedIntType(transactionExpires)) ;
            }
            final EndpointReferenceType registrationCoordinator = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorURI)) ;
            InstanceIdentifier.setEndpointInstanceIdentifier(registrationCoordinator, arjunaContext.getTransactionIdentifier()) ;
            coordinationContext.setRegistrationService(registrationCoordinator) ;
            
            TerminationCoordinatorProcessor.getProcessor().activateParticipant(new BusinessActivityTerminatorImple(), arjunaContext.getTransactionIdentifier()) ;
    
    		_theRegistrar.associate();
    
    		return coordinationContext;
	    }
	    catch (com.arjuna.mw.wsas.exceptions.NoActivityException ex)
	    {
		// TODO handle properly

		ex.printStackTrace();
	    }
	    catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	    {
		// TODO handle properly

		ex.printStackTrace();
	    }
	    catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	    {
		// TODO handle properly

		ex.printStackTrace();
	    }
	    catch (Exception ex)
	    {
		// TODO handle properly

		ex.printStackTrace();
	    }
	}
	else
	{
	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.ba.ContextFactoryImple_1",
					  new Object[]{Protocols.BusinessActivityAtomic, coordinationTypeURI});

	    throw new InvalidCreateParametersException(wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.ba.ContextFactoryImple_3")+" < "+Protocols.BusinessActivityAtomic+", "+coordinationTypeURI+" >");
	}

	return null;
    }

    /**
     * Called when a context factory is removed from a context factory mapper. This method will be called multiple
     * times if the context factory is removed from multiple context factory mappers or from the same context factory
     * mapper with different coordination type uris.
     *
     * @param serviceAddress the address of the service
     * @param coordinationTypeURI the coordination type uri
     */

    public void uninstall (String coordinationTypeURI)
    {
	// we don't use this as one implementation is registered per type
    }

    public final RegistrarImple registrar ()
    {
	return _theRegistrar;
    }

    private CoordinatorManager                   _coordManager;
    private RegistrarImple                       _theRegistrar;
    
}
