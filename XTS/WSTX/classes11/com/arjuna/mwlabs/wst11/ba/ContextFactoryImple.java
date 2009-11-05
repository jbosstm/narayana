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
import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mwlabs.wst11.ba.context.ArjunaContextImple;
import com.arjuna.mwlabs.wst11.ba.RegistrarImple;
import com.arjuna.mwlabs.wst11.ba.BusinessActivityTerminatorImple;
import com.arjuna.mwlabs.wst11.ba.participants.CleanupSynchronization;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.CoordinatorServiceImple;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.BACoordinator;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.wsc11.ContextFactory;
import com.arjuna.wsc11.RegistrationCoordinator;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wst11.BusinessActivityTerminator;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;
import com.arjuna.wst11.stub.SubordinateCoordinatorCompletionParticipantStub;
import com.arjuna.wst11.stub.BACoordinatorCompletionParticipantManagerStub;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.namespace.QName;

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
            final Long expires, final CoordinationContextType currentContext, final boolean isSecure)
        throws InvalidCreateParametersException
    {
        if (BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME.equals(coordinationTypeURI))
    	{
    	    try
    	    {
    		// make sure no transaction is currently associated

            if (currentContext == null) {
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
            final String registrationCoordinatorURI = serviceRegistry.getServiceURI(CoordinationConstants.REGISTRATION_SERVICE_NAME, isSecure) ;

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
            } else {
                // we need to create a subordinate transaction -- this transaction will not be associated
                // with an activity which identifes the parent transaction this means that we cannot use
                // the activity service to do things like enlist participants or deliver participant
                // initiated messages.

                SubordinateBACoordinator subTx = (SubordinateBACoordinator) createSubordinate();

                // now we register a coordinator completion participant on behalf of the subtransaction
                // with the registration service defined in the current context
                // there is no point registering a participant completion participant because we cannot
                // know when it is ok to forward a completed message -- even when all N registered PC
                // participants have notified completed another PC participant might enlist.

                String ccpid = subTx.getCoordinatorCompletionParticipantid();
                SubordinateCoordinatorCompletionParticipantStub ccp = new SubordinateCoordinatorCompletionParticipantStub(subTx);
                String messageId = MessageId.getMessageId() ;
                W3CEndpointReference participant = getParticipant(ccpid, isSecure);
                W3CEndpointReference coordinator = RegistrationCoordinator.register(currentContext, messageId, participant, BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION) ;
                final CoordinatorCompletionParticipantEngine engine = new CoordinatorCompletionParticipantEngine(ccpid, coordinator, ccp) ;
                CoordinatorCompletionParticipantProcessor.getProcessor().activateParticipant(engine, ccpid) ;
                // we need to pass a manager to the stub in case it has to fail at completion
                BAParticipantManager manager = new BACoordinatorCompletionParticipantManagerStub(engine);
                ccp.setManager(manager);

                // ok now create the context
                final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
                final String registrationCoordinatorURI = serviceRegistry.getServiceURI(CoordinationConstants.REGISTRATION_SERVICE_NAME, isSecure) ;

                final CoordinationContext coordinationContext = new CoordinationContext() ;
                coordinationContext.setCoordinationType(coordinationTypeURI);
                CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
                String txId = subTx.get_uid().stringForm();
                identifier.setValue("urn:" + txId);
                coordinationContext.setIdentifier(identifier) ;
                Expires expiresInstance = currentContext.getExpires();
                final long transactionExpires = (expiresInstance != null ? expiresInstance.getValue() : 0);
                if (transactionExpires > 0)
                {
                    expiresInstance = new Expires();
                    expiresInstance.setValue(transactionExpires);
                    coordinationContext.setExpires(expiresInstance);
                }
                W3CEndpointReference registrationCoordinator = getRegistrationCoordinator(registrationCoordinatorURI, txId);
                coordinationContext.setRegistrationService(registrationCoordinator) ;

                // now associate the tx id with the sub transaction

                _theRegistrar.associate(subTx);
                return coordinationContext;
            }
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

    /**
     * class used to return data required to manage a bridged to subordinate transaction
     */
    public class BridgeTxData
    {
        public CoordinationContext context;
        public SubordinateBACoordinator coordinator;
        public String identifier;
    }

    /**
     * create a bridged to subordinate WS-BA 1.1 transaction, associate it with the registrar and create and return
     * a coordination context for it. n.b. this is a private, behind-the-scenes method for use by the JTA-BA
     * transaction bridge code.
     * @param expires the timeout for the bridged to BA transaction
     * @param isSecure true if the registration cooridnator URL should use a secure address, otherwise false.
     * @return a coordination context for the bridged to transaction
     */
    public BridgeTxData createBridgedTransaction (final Long expires, final boolean isSecure)
    {
        // we need to create a subordinate transaction and expose it to the bridge layer so it can
        // be driven to completion
        
        SubordinateBACoordinator subTx = null;
        try {
            subTx = (SubordinateBACoordinator) createSubordinate();
        } catch (NoActivityException e) {
            // will not happen
            return null;
        } catch (InvalidProtocolException e) {
            // will not happen
            return null;
        } catch (SystemException e) {
            // may happen
            return null;
        }

        // ok now create the context

        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        final String registrationCoordinatorURI = serviceRegistry.getServiceURI(CoordinationConstants.REGISTRATION_SERVICE_NAME, isSecure) ;

        final CoordinationContext coordinationContext = new CoordinationContext() ;
        coordinationContext.setCoordinationType(BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME);
        CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
        String txId = subTx.get_uid().stringForm();
        identifier.setValue("urn:" + txId);
        coordinationContext.setIdentifier(identifier) ;
        if (expires != null && expires.longValue() > 0)
        {
            Expires expiresInstance = new Expires();
            expiresInstance.setValue(expires);
            coordinationContext.setExpires(expiresInstance);
        }
        W3CEndpointReference registrationCoordinator = getRegistrationCoordinator(registrationCoordinatorURI, txId);
        coordinationContext.setRegistrationService(registrationCoordinator) ;

        // now associate the tx id with the sub transaction

        try {
            _theRegistrar.associate(subTx);
        } catch (Exception e) {
            // will not happen
        }
        BridgeTxData bridgeTxData = new BridgeTxData();
        bridgeTxData.context = coordinationContext;
        bridgeTxData.coordinator = subTx;
        bridgeTxData.identifier = txId;

        return bridgeTxData;
    }

    private W3CEndpointReference getParticipant(final String id, final boolean isSecure)
    {
        final QName serviceName = BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_QNAME;
        final QName endpointName = BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_PORT_QNAME;
        final String address = ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME, isSecure);
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(serviceName);
        builder.endpointName(endpointName);
        builder.address(address);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
        return builder.build();
    }

    private static W3CEndpointReference getRegistrationCoordinator(String registrationCoordinatorURI, ArjunaContextImple arjunaContext) {
        final String identifier = arjunaContext.getTransactionIdentifier();
        return getRegistrationCoordinator(registrationCoordinatorURI, identifier);
    }

    private static W3CEndpointReference getRegistrationCoordinator(String registrationCoordinatorURI, String identifier) {
        final W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(CoordinationConstants.REGISTRATION_SERVICE_QNAME);
        builder.endpointName(CoordinationConstants.REGISTRATION_ENDPOINT_QNAME);
        // strictly we shouldn't need to set the address because we are in the same web app as the
        // coordinator but we have to as the W3CEndpointReference implementation is incomplete
        builder.address(registrationCoordinatorURI);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, identifier);
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

    public final Object createSubordinate () throws NoActivityException, InvalidProtocolException, SystemException
    {
        try
        {
            CoordinatorServiceImple coordManager = (CoordinatorServiceImple) _coordManager;
            BACoordinator subordinateTransaction = coordManager.createSubordinate();

            /*
             * Now add the registrar for this specific coordinator to the
             * mapper.
             */

            subordinateTransaction.enlistSynchronization(new CleanupSynchronization(subordinateTransaction.get_uid().stringForm(), _theRegistrar));

            _theRegistrar.associate(subordinateTransaction);

            return subordinateTransaction;
        }
        catch (Exception ex)
        {
            throw new SystemException(ex.toString());
        }
    }

    public final RegistrarImple registrar ()
    {
        return _theRegistrar;
    }

    private CoordinatorManager                   _coordManager;
    private RegistrarImple _theRegistrar;

}