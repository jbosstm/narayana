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
 * $Id: LocalContextFactoryImple.java,v 1.5.4.1 2005/11/22 10:36:10 kconner Exp $
 */

package com.arjuna.mwlabs.wst.at.local;

import com.arjuna.mw.wsas.context.DeploymentContext;
import com.arjuna.mw.wsas.context.DeploymentContextFactory;
import com.arjuna.mw.wscf.model.twophase.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.at.context.ArjunaContextImple;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;

public class LocalContextFactoryImple implements ContextFactory
{

	public LocalContextFactoryImple ()
	{
		try
		{
			_coordManager = CoordinatorManagerFactory.coordinatorManager();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Called when a context factory is added to a context factory mapper. This
	 * method will be called multiple times if the context factory is added to
	 * multiple context factory mappers or to the same context mapper with
	 * different protocol identifiers.
	 * 
	 * @param coordinationTypeURI
	 *            the coordination type uri
	 */

	public void install (final String coordinationTypeURI)
	{
	}

	/**
	 * Creates a coordination context.
	 * 
	 * @param coordinationTypeURI
	 *            the coordination type uri
	 * @param expires
	 *            the expire date/time for the returned context, can be null
	 * @param currentContext
	 *            the current context, can be null
	 * 
	 * @return the created coordination context
	 * 
	 * @throws InvalidCreateParametersException
	 *             if a parameter passed is invalid this activity identifier.
	 * 
	 * @message com.arjuna.mwlabs.wst.at.local.LocalContextFactoryImple_1
	 *          [com.arjuna.mwlabs.wst.at.local.LocalContextFactoryImple_1] -
	 *          Invalid type URI: < {0} , {1} >
	 * @message com.arjuna.mwlabs.wst.at.local.LocalContextFactoryImple_11
	 *          [com.arjuna.mwlabs.wst.at.local.LocalContextFactoryImple_11] -
	 *          Invalid type URI:
	 */

	public CoordinationContextType create (final String coordinationTypeURI,
            final Long expires, final CoordinationContextType currentContext)
			throws InvalidCreateParametersException
	{
		if (coordinationTypeURI.equals(AtomicTransactionConstants.WSAT_PROTOCOL))
		{
			try
			{
				_coordManager.suspend();

				_coordManager.begin();

				DeploymentContext manager = DeploymentContextFactory.deploymentContext();
				ArjunaContextImple arjunaContext = (ArjunaContextImple)manager.context();

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

				return coordinationContext;
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
			wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.at.local.LocalContextFactoryImple_1", new Object[]
			{ AtomicTransactionConstants.WSAT_PROTOCOL, coordinationTypeURI });

			throw new InvalidCreateParametersException(
					wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.at.local.LocalContextFactoryImple_11"
							+ " <"
							+ AtomicTransactionConstants.WSAT_PROTOCOL
							+ ", "
							+ coordinationTypeURI + " >"));
		}

		return null;
	}

	/**
	 * Called when a context factory is removed from a context factory mapper.
	 * This method will be called multiple times if the context factory is
	 * removed from multiple context factory mappers or from the same context
	 * factory mapper with different coordination type uris.
	 * 
	 * @param coordinationTypeURI
	 *            the coordination type uri
	 */

	public void uninstall (final String coordinationTypeURI)
	{
	}

	public CoordinatorManager coordinatorManager ()
	{
		return _coordManager;
	}

	private CoordinatorManager _coordManager;

}
