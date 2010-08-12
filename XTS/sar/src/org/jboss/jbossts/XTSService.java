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
package org.jboss.jbossts;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.common.util.propertyservice.PropertiesFactory;
import org.jboss.jbossts.xts.environment.WSCFEnvironmentBean;
import com.arjuna.mw.wscf.protocols.ProtocolRegistry;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.logging.Logger;
import org.jboss.jbossts.xts.recovery.coordinator.at.ATCoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.coordinator.at.SubordinateATCoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.coordinator.ba.BACoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.coordinator.ba.SubordinateBACoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.at.ATParticipantRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.ba.BAParticipantRecoveryModule;

//import com.arjuna.mw.wst.deploy.WSTXInitialisation;
//import com.arjuna.mw.wst.UserTransaction;
//import com.arjuna.mw.wst.TransactionManager;
//import com.arjuna.mw.wst.UserBusinessActivity;
//import com.arjuna.mw.wst.BusinessActivityManager;
//import com.arjuna.wsc.ContextFactoryMapper;
//import com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl;
//import com.arjuna.wsc.messaging.RegistrationCoordinatorProcessorImpl;
//import com.arjuna.mwlabs.wsc.ContextFactoryMapperImple;
import com.arjuna.services.framework.task.TaskManager;
import com.arjuna.services.framework.startup.Sequencer;
//import com.arjuna.webservices.HandlerRegistry;
//import com.arjuna.webservices.SoapRegistry;
//import com.arjuna.webservices.SoapService;
//import com.arjuna.webservices.SoapClient;
//import com.arjuna.webservices.util.ClassLoaderHelper;
//import com.arjuna.webservices.wsba.policy.CoordinatorCompletionCoordinatorPolicy;
//import com.arjuna.webservices.wsba.policy.CoordinatorCompletionParticipantPolicy;
//import com.arjuna.webservices.wsba.policy.ParticipantCompletionCoordinatorPolicy;
//import com.arjuna.webservices.wsba.policy.ParticipantCompletionParticipantPolicy;
//import com.arjuna.webservices.wsba.BusinessActivityConstants;
//import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
//import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
//import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
//import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
//import com.arjuna.webservices.wsat.policy.CompletionCoordinatorPolicy;
//import com.arjuna.webservices.wsat.policy.CompletionInitiatorPolicy;
//import com.arjuna.webservices.wsat.policy.CoordinatorPolicy;
//import com.arjuna.webservices.wsat.policy.ParticipantPolicy;
//import com.arjuna.webservices.wsat.AtomicTransactionConstants;
//import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
//import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
//import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
//import com.arjuna.webservices.wsarjtx.policy.TerminationParticipantPolicy;
//import com.arjuna.webservices.wsarjtx.policy.TerminationCoordinatorPolicy;
//import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
//import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
//import com.arjuna.webservices.transport.http.HttpClient;
//import com.arjuna.webservices.wsarj.policy.ArjunaPolicy;
//import com.arjuna.webservices.wscoor.policy.ActivationCoordinatorPolicy;
//import com.arjuna.webservices.wscoor.policy.ActivationRequesterPolicy;
//import com.arjuna.webservices.wscoor.policy.RegistrationCoordinatorPolicy;
//import com.arjuna.webservices.wscoor.policy.RegistrationRequesterPolicy;
//import com.arjuna.webservices.wscoor.CoordinationConstants;
//import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;
//import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;
//import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
//import com.arjuna.wst.messaging.*;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.webservices.util.TransportTimer;
import com.arjuna.mwlabs.wsas.activity.ActivityReaper;

import javax.management.MBeanServer;
import java.net.InetAddress;
import java.util.Properties;
//import com.arjuna.ats.arjuna.recovery.RecoveryModule;

/**
 * $Id$
 */
public class XTSService implements XTSServiceMBean {

/*
<?xml version="1.0" encoding="UTF-8"?>
<deployment xmlns="urn:jboss:bean-deployer:2.0">

    <bean name="XTSService" class="org.jboss.transactions.XTSService">
        <annotation>@org.jboss.aop.microcontainer.aspects.jmx.JMX(name="jboss.xts:service=XTSService", exposedInterface=org.jboss.transactions.XTSServiceMBean.class, registerDirectly=true)</annotation>

       <depends>jboss.web:service=WebServer</depends>
       <depends>jboss:service=TransactionManager</depends>
   </bean>

</deployment>

 */

    // TODO expose as bean properties
    private int taskManagerMinWorkerCount = 0;
    private int taskManagerMaxWorkerCount = 10;

    private final Logger log = org.jboss.logging.Logger.getLogger(XTSService.class);

    private ATCoordinatorRecoveryModule acCoordinatorRecoveryModule = null;
    private SubordinateATCoordinatorRecoveryModule atSubordinateCoordinatorRecoveryModule = null;
    private ATParticipantRecoveryModule atParticipantRecoveryModule = null;

    private BACoordinatorRecoveryModule baCoordinatorRecoveryModule = null;
    private SubordinateBACoordinatorRecoveryModule baSubordinateCoordinatorRecoveryModule = null;
    private BAParticipantRecoveryModule baParticipantRecoveryModule = null;

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

    public void start() throws Exception
    {
        log.info("JBossTS XTS Transaction Service - starting");

        // now it is safe to let the Sequencer class run any initialisation routines it needs

        Sequencer.unlatch();

        TaskManagerInitialisation(); // com.arjuna.services.framework.admin.TaskManagerInitialisation : initialise the Task Manager


        // install the AT recovery modules
        
        // ok, now the main module

        acCoordinatorRecoveryModule = new ATCoordinatorRecoveryModule();

        // ensure Implementations are installed into the inventory before we register the module

        acCoordinatorRecoveryModule.install();

        // now the recovery module for subordinate AT transactions

        atSubordinateCoordinatorRecoveryModule = new SubordinateATCoordinatorRecoveryModule();

        // we don't need to install anything in the Inventory for this recovery module as it
        // uses the same records as those employed by ACCoordinatorRecoveryModule

        atSubordinateCoordinatorRecoveryModule.install();

        // now the module for AT participants
        // we don't need to install anything in the Inventory for this recovery module as it
        // manages its own ObjectStore records but we do need it to create the recovery manager
        // singleton.

        atParticipantRecoveryModule = new ATParticipantRecoveryModule();

        atParticipantRecoveryModule.install();

        // ok, now the recovery mocules for the BA coordinator and participant

        baCoordinatorRecoveryModule = new BACoordinatorRecoveryModule();

        // ensure Implementations are installed into the inventory before we register the module

        baCoordinatorRecoveryModule.install();

        // now the recovery module for subordinate BA transactions

        baSubordinateCoordinatorRecoveryModule = new SubordinateBACoordinatorRecoveryModule();

        // we don't need to install anything in the Inventory for this recovery module as it
        // uses the same records as those employed by ACCoordinatorRecoveryModule

        baSubordinateCoordinatorRecoveryModule.install();

        // now the module for BA participants
        // we don't need to install anything in the Inventory for this recovery module as it
        // manages its own ObjectStore records but we do need it to create the recovery manager
        // singleton.

        baParticipantRecoveryModule = new BAParticipantRecoveryModule();

        baParticipantRecoveryModule.install();

        // we assume the tx manager has started, hence initializing the recovery manager.
        // to guarantee this our mbean should depend on the tx mgr mbean. (but does that g/tee start or just load?)

        //  recovery should perform better if we register partiicpants first since this allows the XTS client
        // recovery module to have a try at recreating the participant before its coordinator attempts to
        // talk to it when they are both in the same VM. it also means the participant nay attempt bottom-up
        // recovery before the coordinator is ready but coordinator recovery is probably going to happen quicker.

        // similarly, it is better to recreate subordinate coordinators before recreating ordinary coordinators
        // because the latter may need the former to be present when the parent and subordinate are both in the
        // same VM.

        // note also that the current implementation relies upon this registration order since the main
        // coordinator recovery modules flag completion of the first coordinator recovery pass and this
        // should only happen once both modules have had a crack at the log

        RecoveryManager.manager().addModule(atParticipantRecoveryModule);
        RecoveryManager.manager().addModule(baParticipantRecoveryModule);

        RecoveryManager.manager().addModule(atSubordinateCoordinatorRecoveryModule);
        RecoveryManager.manager().addModule(baSubordinateCoordinatorRecoveryModule);

        RecoveryManager.manager().addModule(acCoordinatorRecoveryModule);
        RecoveryManager.manager().addModule(baCoordinatorRecoveryModule);
    }

    public void stop() throws Exception
    {
        log.info("JBossTS XTS Transaction Service - stopping");

        if (baCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(baCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            baCoordinatorRecoveryModule.uninstall();
        }
        if (acCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(acCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            acCoordinatorRecoveryModule.uninstall();
        }
        if (atSubordinateCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(atSubordinateCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            atSubordinateCoordinatorRecoveryModule.uninstall();
        }
        if (baSubordinateCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(baSubordinateCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            baSubordinateCoordinatorRecoveryModule.uninstall();
        }
        if (baParticipantRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(baParticipantRecoveryModule, true);
            // call uninstall even though it is currently a null op for this module
            baParticipantRecoveryModule.uninstall();
        }
        if (atParticipantRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(atParticipantRecoveryModule, true);
            // call uninstall even though it is currently a null op for this module
            atParticipantRecoveryModule.uninstall();
        }

        TaskManager.getManager().shutdown() ; // com.arjuna.services.framework.admin.TaskManagerInitialisation

        // shutdown the activity service reaper

        ActivityReaper.shutdown();
        
        /*
         * this will be done by the servlet shutdown code
        // HttpClientInitialisation
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        soapRegistry.removeSoapClient("http") ;
        soapRegistry.removeSoapClient("https") ;
        */
    }

    private void TaskManagerInitialisation()
    {
        final TaskManager taskManager = TaskManager.getManager() ;
        taskManager.setMinimumWorkerCount(taskManagerMinWorkerCount) ;
        taskManager.setMaximumWorkerCount(taskManagerMaxWorkerCount) ;
    }

    public MBeanServer getMbeanServer()
    {
       return mbeanServer;
    }

    public void setMbeanServer(MBeanServer mbeanServer)
    {
       this.mbeanServer = mbeanServer;
    }

    private MBeanServer mbeanServer = null;

    ///////////////

    // These setters are used to allow MC/ServiceBindingManger to relay information from the Web server
    // seee bindings.xml and jboss-beans.xml

    public InetAddress getHttpBindInetAddress() {
        return httpBindInetAddress;
    }

    public void setHttpBindInetAddress(InetAddress httpBindInetAddress) {
        this.httpBindInetAddress = httpBindInetAddress;
    }

    private InetAddress httpBindInetAddress = null;

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    int httpPort = 0;

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    int httpsPort = 0;
}