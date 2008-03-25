/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2007,
 * @author JBoss Inc.
 */
package org.jboss.transactions;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;
import org.jboss.transactions.xts.recovery.ACCoordinatorRecoveryModule;

import com.arjuna.mw.wsas.utils.Configuration;
import com.arjuna.mw.wst.deploy.WSTXInitialisation;
import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.wsc.ContextFactoryMapper;
import com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl;
import com.arjuna.wsc.messaging.RegistrationCoordinatorProcessorImpl;
import com.arjuna.mwlabs.wsc.ContextFactoryMapperImple;
import com.arjuna.services.framework.task.TaskManager;
import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.SoapClient;
import com.arjuna.webservices.util.ClassLoaderHelper;
import com.arjuna.webservices.wsba.policy.CoordinatorCompletionCoordinatorPolicy;
import com.arjuna.webservices.wsba.policy.CoordinatorCompletionParticipantPolicy;
import com.arjuna.webservices.wsba.policy.ParticipantCompletionCoordinatorPolicy;
import com.arjuna.webservices.wsba.policy.ParticipantCompletionParticipantPolicy;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.webservices.wsat.policy.CompletionCoordinatorPolicy;
import com.arjuna.webservices.wsat.policy.CompletionInitiatorPolicy;
import com.arjuna.webservices.wsat.policy.CoordinatorPolicy;
import com.arjuna.webservices.wsat.policy.ParticipantPolicy;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices.wsarjtx.policy.TerminationParticipantPolicy;
import com.arjuna.webservices.wsarjtx.policy.TerminationCoordinatorPolicy;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices.transport.http.HttpClient;
import com.arjuna.webservices.wsarj.policy.ArjunaPolicy;
import com.arjuna.webservices.wscoor.policy.ActivationCoordinatorPolicy;
import com.arjuna.webservices.wscoor.policy.ActivationRequesterPolicy;
import com.arjuna.webservices.wscoor.policy.RegistrationCoordinatorPolicy;
import com.arjuna.webservices.wscoor.policy.RegistrationRequesterPolicy;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.wst.messaging.*;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;

import java.io.InputStream;

/**
 * $Id$
 */
public class XTSService extends ServiceMBeanSupport implements XTSServiceMBean {

    // TODO expose as bean properties
    private int taskManagerMinWorkerCount = 0;
    private int taskManagerMaxWorkerCount = 10;

    private ACCoordinatorRecoveryModule acCoordinatorRecoveryModule = null;

    // TODO: how to use a (per application) remote coordinator?
    // does the http servlet param indicate its own location and the
    // coordinatorURL indicate the coord??

    // ./HashedActionStore/defaultStore/StateManager/BasicAction/AtomicAction/TwoPhaseCoordinator/TwoPhase/ACCoordinator/

//    	    <property
//	    name="com.arjuna.ats.arjuna.recovery.recoveryExtension1"
//	    value="com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule"/>
    // recovers /StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction
    // not what we need.

    // TODO: new recovery mod, inheriting from existing base version e.g. AtomicActionRecoveryModule - change typex

    public XTSService() {}

    protected void startService() throws Exception
    {
        getLog().info("JBossTS XTS Transaction Service - starting");

        // read unified properties file (replaces wscf.xml and wstx.xml)
        Configuration.initialise("/jbossxts.xml");

        //// wscf.war:

        WSCFInitialisation();  // com.arjuna.mw.wsc.deploy.WSCFInitialisation: Initialise WSCF

        //// ws-c.war:

        TaskManagerInitialisation(); // com.arjuna.services.framework.admin.TaskManagerInitialisation : initialise the Task Manager
        ActivationCoordinatorInitialisation(); // com.arjuna.webservices.wscoor.server.ActivationCoordinatorInitialisation : Activate the Activation Coordinator service
        ActivationRequesterInitialisation(); // com.arjuna.webservices.wscoor.server.ActivationRequesterInitialisation : Activate the Activation Requester service
        RegistrationCoordinatorInitialisation(); // com.arjuna.webservices.wscoor.server.RegistrationCoordinatorInitialisation : Activate the Registration Coordinator service
        RegistrationRequesterInitialisation(); // com.arjuna.webservices.wscoor.server.RegistrationRequesterInitialisation : Activate the Registration Requester service
        CoordinationInitialisation(); // com.arjuna.wsc.messaging.deploy.CoordinationInitialisation : Initialise the coordination services.
        HttpClientInitialisation(); // com.arjuna.webservices.transport.http.HttpClientInitialisation : initialise the HTTP clients.
        // TODO: HTTP SOAP Service Multiplexor Servlet

        //// ws-t.war:

        TerminationParticipantInitialisation(); // com.arjuna.webservices.wsarjtx.server.TerminationParticipantInitialisation : Arjuna TX - Activate the Terminator Participant  service
        TerminationCoordinatorInitialisation(); // com.arjuna.webservices.wsarjtx.server.TerminationCoordinatorInitialisation : Arjuna TX - Activate the Terminator Coordinator service

        CompletionCoordinatorInitialisation(); // com.arjuna.webservices.wsat.server.CompletionCoordinatorInitialisation : WS-AT - Activate the Completion Coordinator service
        CompletionInitiatorInitialisation(); // com.arjuna.webservices.wsat.server.CompletionInitiatorInitialisation : WS-AT - Activate the Completion Initiator service
        CoordinatorInitialisation(); // com.arjuna.webservices.wsat.server.CoordinatorInitialisation : WS-AT: Activate the Coordinator service
        ParticipantInitialisation(); // com.arjuna.webservices.wsat.server.ParticipantInitialisation : WS-AT - Activate the Participant service

        CoordinatorCompletionCoordinatorInitialisation(); // com.arjuna.webservices.wsba.server.CoordinatorCompletionCoordinatorInitialisation : WS-BA - Activate the Coordinator Completion Coordinator service
        CoordinatorCompletionParticipantInitialisation(); // com.arjuna.webservices.wsba.server.CoordinatorCompletionParticipantInitialisation : WS-BA - Activate the Coordinator Completion Participant service
        ParticipantCompletionCoordinatorInitialisation(); // com.arjuna.webservices.wsba.server.ParticipantCompletionCoordinatorInitialisation : WS-BA - Activate the Participant Completion Coordinator service
        ParticipantCompletionParticipantInitialisation(); // com.arjuna.webservices.wsba.server.ParticipantCompletionParticipantInitialisation : WS-BA - Activate the Participant Completion Participant service

        TransactionInitialisation(); // com.arjuna.wst.messaging.deploy.TransactionInitialisation : WS-T - Initialise the transaction services.

        //// wstx.war:

        WSTXInitialisation(); // com.arjuna.mw.wst.deploy.WSTXInitialisation : Initialise WSTX

        acCoordinatorRecoveryModule = new ACCoordinatorRecoveryModule();
        // we assume the tx manager has started, hence initializing the recovery manager.
        // to guarantee this our mbean should depend on the tx mgr mbean. (but does that g/tee start or just load?)
        RecoveryManager.manager().addModule(acCoordinatorRecoveryModule);

    }

    protected void stopService() throws Exception
    {
        getLog().info("JBossTS XTS Transaction Service - stopping");

        if (acCoordinatorRecoveryModule != null) {
            RecoveryManager.manager().removeModule(acCoordinatorRecoveryModule);             
        }
        TaskManager.getManager().shutdown() ; // com.arjuna.services.framework.admin.TaskManagerInitialisation

        // HttpClientInitialisation
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        soapRegistry.removeSoapClient("http") ;
        soapRegistry.removeSoapClient("https") ;
    }


    ///////////////////////////////

    private void WSCFInitialisation() throws Exception
    {
        //Configuration.initialise("/wscf.xml");

        final ContextFactoryMapper wscfImpl = ContextFactoryMapper.getFactory() ;

        wscfImpl.setSubordinateContextFactoryMapper(new ContextFactoryMapperImple());
    }

    private void TaskManagerInitialisation()
    {
        final TaskManager taskManager = TaskManager.getManager() ;
        taskManager.setMinimumWorkerCount(taskManagerMinWorkerCount) ;
        taskManager.setMaximumWorkerCount(taskManagerMaxWorkerCount) ;
    }

    private void ActivationCoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add Activation coordinator.
        ActivationCoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(CoordinationConstants.SERVICE_ACTIVATION_COORDINATOR, handlerRegistry);
    }

    private void ActivationRequesterInitialisation()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add Activation coordinator.
        ActivationRequesterPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(CoordinationConstants.SERVICE_ACTIVATION_REQUESTER, handlerRegistry);
    }

    private void RegistrationCoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Registration coordinator.
        RegistrationCoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR, handlerRegistry);
    }

    private void RegistrationRequesterInitialisation()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add Registration coordinator.
        RegistrationRequesterPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(CoordinationConstants.SERVICE_REGISTRATION_REQUESTER, handlerRegistry);
    }

    private void CoordinationInitialisation()
    {
        ActivationCoordinatorProcessor.setCoordinator(new ActivationCoordinatorProcessorImpl()) ;
        RegistrationCoordinatorProcessor.setCoordinator(new RegistrationCoordinatorProcessorImpl()) ;
    }

    private void HttpClientInitialisation()
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final SoapClient client = new HttpClient() ;
        soapRegistry.registerSoapClient("http", client) ;
        soapRegistry.registerSoapClient("https", client) ;
    }

    private void TerminationParticipantInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Terminator coordinator.
        TerminationParticipantPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(ArjunaTXConstants.SERVICE_TERMINATION_PARTICIPANT, handlerRegistry);
    }

    private void TerminationCoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Terminator participant.
        TerminationCoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(ArjunaTXConstants.SERVICE_TERMINATION_COORDINATOR, handlerRegistry);
    }

    private void CompletionCoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Completion coordinator.
        CompletionCoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(AtomicTransactionConstants.SERVICE_COMPLETION_COORDINATOR, handlerRegistry);
    }

    private void CompletionInitiatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Completion initiator.
        CompletionInitiatorPolicy.register(handlerRegistry);

        addToSOAPRegistry(AtomicTransactionConstants.SERVICE_COMPLETION_INITIATOR, handlerRegistry);
    }

    private void CoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add coordinator.
        CoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(AtomicTransactionConstants.SERVICE_COORDINATOR, handlerRegistry);
    }

    private void ParticipantInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Participant.
        ParticipantPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(AtomicTransactionConstants.SERVICE_PARTICIPANT, handlerRegistry);
    }

    private void CoordinatorCompletionCoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Coordinator Completion coordinator.
        CoordinatorCompletionCoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_COORDINATOR, handlerRegistry);
    }

    private void CoordinatorCompletionParticipantInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Coordinator Completion participant.
        CoordinatorCompletionParticipantPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_PARTICIPANT, handlerRegistry);
    }

    private void ParticipantCompletionCoordinatorInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Participant Completion coordinator.
        ParticipantCompletionCoordinatorPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR, handlerRegistry);
    }

    private void ParticipantCompletionParticipantInitialisation()
    {
        final HandlerRegistry handlerRegistry = getHandlerRegistry();

        // Add Participant Completion participant.
        ParticipantCompletionParticipantPolicy.register(handlerRegistry) ;

        addToSOAPRegistry(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT, handlerRegistry);
    }

    private void TransactionInitialisation()
    {
        CompletionCoordinatorProcessor.setProcessor(new CompletionCoordinatorProcessorImpl()) ;
        ParticipantProcessor.setProcessor(new ParticipantProcessorImpl()) ;
        CoordinatorProcessor.setProcessor(new CoordinatorProcessorImpl()) ;
        TerminationCoordinatorProcessor.setProcessor(new TerminatorParticipantProcessorImpl()) ;
        CoordinatorCompletionParticipantProcessor.setProcessor(new CoordinatorCompletionParticipantProcessorImpl()) ;
        ParticipantCompletionParticipantProcessor.setProcessor(new ParticipantCompletionParticipantProcessorImpl()) ;
        CoordinatorCompletionCoordinatorProcessor.setProcessor(new CoordinatorCompletionCoordinatorProcessorImpl()) ;
        ParticipantCompletionCoordinatorProcessor.setProcessor(new ParticipantCompletionCoordinatorProcessorImpl()) ;
    }

    private void WSTXInitialisation() throws Exception
    {
        // we don't know if the servlet is inited yet since its deploy is async,
        // so play it safe and set the URL here too since UserTransactionImple needs it.
        System.setProperty("com.arjuna.mw.wst.coordinatorURL", "http://localhost:8080/jbossxts/soap/ActivationCoordinator");

        // wst.xml ignored. TODO: make these configurable again (mbean properties?):
        UserTransaction.setUserTransaction(new com.arjuna.mwlabs.wst.at.remote.UserTransactionImple());
        TransactionManager.setTransactionManager(new com.arjuna.mwlabs.wst.at.remote.TransactionManagerImple());
        UserBusinessActivity.setUserBusinessActivity(new com.arjuna.mwlabs.wst.ba.remote.UserBusinessActivityImple());
        BusinessActivityManager.setBusinessActivityManager(new com.arjuna.mwlabs.wst.ba.remote.BusinessActivityManagerImple());

        // TODO: should this really be after the above? At least one property from this file (coordinatorURL)
        // would seem to be required at an earlier stage.
        //Configuration.initialise("/wstx.xml");

    }
    ////

    private HandlerRegistry getHandlerRegistry() {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add Arjuna handlers
        ArjunaPolicy.register(handlerRegistry) ;

        return handlerRegistry;
    }

    private void addToSOAPRegistry(String serviceName, HandlerRegistry handlerRegistry)
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        soapRegistry.registerSoapService(serviceName, new SoapService(handlerRegistry)) ;
    }
}