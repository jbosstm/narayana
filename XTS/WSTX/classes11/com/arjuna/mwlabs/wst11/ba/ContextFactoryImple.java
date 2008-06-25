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
 * $Id: ContextFactoryImple.java,v 1.4.4.1 2005/11/22 10:36:14 kconner Exp $
 */

package com.arjuna.mwlabs.wst11.ba;

import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst11.ba.context.ArjunaContextImple;
import com.arjuna.mwlabs.wst11.ba.RegistrarImple;
import com.arjuna.mwlabs.wst11.ba.BusinessActivityTerminatorImple;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wst11.BusinessActivityTerminator;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

public class ContextFactoryImple implements ContextFactory
{

    public ContextFactoryImple()
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
     * @throws com.arjuna.wsc.InvalidCreateParametersException if a parameter passed is invalid
     *         this activity identifier.
     *
     * @message com.arjuna.mwlabs.wst.ba.Context11FactoryImple_1 [com.arjuna.mwlabs.wst.ba.Context11FactoryImple_1] - Invalid type URI: < {0} , {1} >
     * @message com.arjuna.mwlabs.wst.ba.Context11FactoryImple_3 [com.arjuna.mwlabs.wst.ba.Context11FactoryImple_3] - Invalid type URI:
     */

    public CoordinationContext create (final String coordinationTypeURI,
            final Long expires, final CoordinationContextType currentContext)
        throws InvalidCreateParametersException
    {
        if (BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME.equals(coordinationTypeURI))
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
            final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
            final String registrationCoordinatorURI = serviceRegistry.getServiceURI(CoordinationConstants.REGISTRATION_SERVICE_NAME) ;

            final CoordinationContext coordinationContext = new CoordinationContext() ;
            coordinationContext.setCoordinationType(coordinationTypeURI) ;
            CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
            identifier.setValue("urn:"+arjunaContext.getTransactionIdentifier());
            coordinationContext.setIdentifier(identifier) ;
            final int transactionExpires = arjunaContext.getTransactionExpires() ;
            if (transactionExpires > 0)
            {
                Expires expiresInstance = new Expires();
                expiresInstance.setValue(transactionExpires);
                coordinationContext.setExpires(expiresInstance);
            }
            final W3CEndpointReference registrationCoordinator = getRegistrationCoordinator(registrationCoordinatorURI, arjunaContext);
            coordinationContext.setRegistrationService(registrationCoordinator) ;

            String transactionIdentifier = arjunaContext.getTransactionIdentifier();
            BusinessActivityTerminator terminator = new BusinessActivityTerminatorImple();
            TerminationCoordinatorProcessor.getProcessor().activateParticipant(terminator, transactionIdentifier);

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
	    wstxLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wst.ba.Context11FactoryImple_1",
					  new Object[]{BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME, coordinationTypeURI});

	    throw new InvalidCreateParametersException(wstxLogger.log_mesg.getString("com.arjuna.mwlabs.wst.ba.Context11FactoryImple_3")+" < "+ BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME+", "+coordinationTypeURI+" >");
	}

	return null;
    }

    private static W3CEndpointReference getRegistrationCoordinator(String registrationCoordinatorURI, ArjunaContextImple arjunaContext) {
        final W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(CoordinationConstants.REGISTRATION_SERVICE_QNAME);
        builder.endpointName(CoordinationConstants.REGISTRATION_ENDPOINT_QNAME);
        // strictly we shouldn't need to set the address because we are in the same web app as the
        // coordinator but we have to as the W3CEndpointReference implementation is incomplete
        builder.address(registrationCoordinatorURI);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, arjunaContext.getTransactionIdentifier());
        W3CEndpointReference registrationCoordinator = builder.build();
        return registrationCoordinator;
    }

    /**
     * Called when a context factory is removed from a context factory mapper. This method will be called multiple
     * times if the context factory is removed from multiple context factory mappers or from the same context factory
     * mapper with different coordination type uris.
     *
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
    private RegistrarImple _theRegistrar;

}