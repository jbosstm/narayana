/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss Inc.
 */

package org.jboss.jbossts.xts.servicetests.service;

import org.jboss.jbossts.xts.servicetests.generated.ObjectFactory;
import org.jboss.jbossts.xts.servicetests.generated.ResultsType;
import org.jboss.jbossts.xts.servicetests.generated.CommandsType;
import org.jboss.jbossts.xts.servicetests.generated.XTSServiceTestPortType;
import org.jboss.jbossts.xts.servicetests.service.participant.*;
import org.jboss.jbossts.xts.servicetests.service.recovery.TestATRecoveryModule;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;
import javax.xml.namespace.QName;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.HashMap;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.SystemException;


/**
 * A general purpose web service used to test the WSAT and WSBA services. It implements
 * a single service method which accepts a command list and returns a reesult list. This
 * can be used to register participants and script their behaviour.  
 */
@WebService(targetNamespace = "http://jbossts.jboss.org/xts/servicetests/generated",
        wsdlLocation = "WEB-INF/wsdl/xtsservicetests.wsdl",
        serviceName = "XTSServiceTest",
        portName = "XTSServiceTestPortType",
        name = "XTSServiceTestPortType"
        )
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
// @EndpointConfig(configName = "Standard WSAddressing Endpoint")
@HandlerChain(file="handlers.xml")
@XmlSeeAlso({
    ObjectFactory.class
})
public class XTSServiceTestPortTypeImpl implements XTSServiceTestPortType
{
    @Resource
    private WebServiceContext wsc;

    @PostConstruct
    public void postContruct()
    {
        System.out.println("XTSServiceTestPortTypeImpl postContruct");
    }
    
    @PreDestroy
    public void preDestroy()
    {
        System.out.println("XTSServiceTestPortTypeImpl preDestroy");
    }
    /**
     *
     * @param commands
     * @return
     *     returns org.jboss.jbossts.xts.servicetests.generated.ResultsType
     */
    @WebMethod
    @WebResult(name = "results", targetNamespace = "http://jbossts.jboss.org/xts/servicetests/generated", partName = "results")
    public ResultsType serve(
        @WebParam(name = "commands", targetNamespace = "http://jbossts.jboss.org/xts/servicetests/generated", partName = "commands")
        CommandsType commands)
    {
        ResultsType results = new ResultsType();
        List<String> resultsList = results.getResultList();
        List<String> commandList = commands.getCommandList();

        for (String s : commandList)
        {
            System.out.println("service  " + this + " :  command " + s);
        }

        int size = commandList.size();
        int idx = 0;

        String command = commandList.get(idx);

        // check against each of the possible commands


        if (command.equals("enlistDurable")) {
// enlistment commands
            String id = participantId("DurableTestParticipant");
            DurableTestParticipant participant = new DurableTestParticipant(id);
            TransactionManager txman = TransactionManagerFactory.transactionManager();
            try {
                txman.enlistForDurableTwoPhase(participant, id);
            } catch (Exception e) {
                throw new WebServiceException("enlistDurable failed ", e);
            }
            for (idx = 1; idx < size; idx++) {
                participant.addCommand(commandList.get(idx));
            }
            participantMap.put(id, participant);
            resultsList.add(id);
        } else if (command.equals("enlistVolatile")) {
            String id = participantId("VolatileTestParticipant");
            VolatileTestParticipant participant = new VolatileTestParticipant(id);
            TransactionManager txman = TransactionManagerFactory.transactionManager();
            try {
                txman.enlistForVolatileTwoPhase(participant, id);
            } catch (Exception e) {
                throw new WebServiceException("enlistVolatile failed ", e);
            }
            for (idx = 1;idx < size; idx++) {
                participant.addCommand(commandList.get(idx));
            }
            participantMap.put(id, participant);
            resultsList.add(id);
        } else if (command.equals("enlistCoordinatorCompletion")) {
            String id = participantId("CoordinatorCompletionParticipant");
            CoordinatorCompletionTestParticipant participant = new CoordinatorCompletionTestParticipant(id);
            BusinessActivityManager baman = BusinessActivityManagerFactory.businessActivityManager();
            try {
                BAParticipantManager partMan;
                partMan = baman.enlistForBusinessAgreementWithCoordinatorCompletion(participant, id);
                managerMap.put(id, partMan);
            } catch (Exception e) {
                throw new WebServiceException("enlistCoordinatorCompletion failed ", e);
            }
            for (idx = 1;idx < size; idx++) {
                participant.addCommand(commandList.get(idx));
            }
            participantMap.put(id, participant);
            resultsList.add(id);
        } else if (command.equals("enlistParticipantCompletion")) {
            String id = participantId("ParticipantCompletionParticipant");
            ParticipantCompletionTestParticipant participant = new ParticipantCompletionTestParticipant(id);
            BusinessActivityManager baman = BusinessActivityManagerFactory.businessActivityManager();
            try {
                BAParticipantManager partMan;
                partMan = baman.enlistForBusinessAgreementWithParticipantCompletion(participant, id);
                managerMap.put(id, partMan);
            } catch (Exception e) {
                throw new WebServiceException("enlistParticipantCompletion failed ", e);
            }
            for (idx = 1;idx < size; idx++) {
                participant.addCommand(commandList.get(idx));
            }
            participantMap.put(id, participant);
            resultsList.add(id);
        } else if (command.equals("commands")) {
// add extra commands
            String id = commandList.get(1);
            ScriptedTestParticipant participant = participantMap.get(id);
            if (participant != null) {
                for (idx = 2; idx < size; idx++)
                {
                    participant.addCommand(commandList.get(idx));
                }
                resultsList.add("ok");
            } else {
                throw new WebServiceException("addCommands failed to find participant " + id);
            }
        } else if (command.equals("exit")) {
// initiate BA manager activities
            String id = commandList.get(1);
            ScriptedTestParticipant participant = participantMap.get(id);
            if (participant != null ) {
                if (participant instanceof ParticipantCompletionTestParticipant) {
                    ParticipantCompletionTestParticipant baparticipant = (ParticipantCompletionTestParticipant)participant;
                    BAParticipantManager manager = managerMap.get(id);
                    try {
                        manager.exit();
                    } catch (Exception e) {
                        throw new WebServiceException("exit " + id + " failed with exception " + e);
                    }
                    resultsList.add("ok");
                } else {
                    throw new WebServiceException("exit invalid participant type " + id);
                }
            } else {
                throw new WebServiceException("exit unknown participant " + id);
            }
        } else if (command.equals("completed")) {
            String id = commandList.get(1);
            ScriptedTestParticipant participant = participantMap.get(id);
            if (participant != null ) {
                if (participant instanceof ParticipantCompletionTestParticipant) {
                    ParticipantCompletionTestParticipant baparticipant = (ParticipantCompletionTestParticipant)participant;
                    BAParticipantManager manager = managerMap.get(id);
                    try {
                        manager.completed();
                        resultsList.add("ok");
                    } catch (Exception e) {
                        throw new WebServiceException("completed " + id + " failed with exception " + e);
                    }
                } else {
                    throw new WebServiceException("completed invalid participant type " + id);
                }
            } else {
                throw new WebServiceException("completed unknown participant " + id);
            }
        } else if (command.equals("fail")) {
            String id = commandList.get(1);
            ScriptedTestParticipant participant = participantMap.get(id);
            if (participant != null ) {
                if (participant instanceof ParticipantCompletionTestParticipant) {
                    ParticipantCompletionTestParticipant baparticipant = (ParticipantCompletionTestParticipant)participant;
                    BAParticipantManager manager = managerMap.get(id);
                    QName qname = new QName("http://jbossts.jboss.org/xts/servicetests/", "fail");
                    try {
                        manager.fail(qname);
                        resultsList.add("ok");
                    } catch (Exception e) {
                        throw new WebServiceException("fail " + id + " failed with exception " + e);
                    }
                } else {
                    throw new WebServiceException("fail invalid participant type " + id);
                }
            } else {
                throw new WebServiceException("fail unknown participant " + id);
            }
        } else if (command.equals("cannotComplete")) {
            String id = commandList.get(1);
            ScriptedTestParticipant participant = participantMap.get(id);
            if (participant != null ) {
                if (participant instanceof ParticipantCompletionTestParticipant) {
                    ParticipantCompletionTestParticipant baparticipant = (ParticipantCompletionTestParticipant)participant;
                    BAParticipantManager manager = managerMap.get(id);
                    try {
                        manager.cannotComplete();
                        resultsList.add("ok");
                    } catch (Exception e) {
                        throw new WebServiceException("cannotComplete " + id + " failed with exception " + e);
                    }
                } else {
                    throw new WebServiceException("cannotComplete invalid participant type " + id);
                }
            } else {
                throw new WebServiceException("cannotComplete unknown participant " + id);
            }
        }

        return results;
    }

    /**
     *  counter used to conjure up participant names
     */

    private static int nextId = 0;

    /**
     * obtain a new participant name starting with a prefix recognised by the recovery code and terminated
     * with the supplied suffix and a unique trailing number
     * @param suffix a component to be added to the name before the counter identifying the type of
     * participant
     * @return
     */
    private synchronized String participantId(String suffix)
    {
        return Constants.PARTICIPANT_ID_PREFIX + suffix + "." + nextId++;
    }

    /**
     * a table used ot retain a handle on enlisted participants so that they can be driven by the client to
     * perform actions not contained in the original command script.
     */
    private HashMap<String, ScriptedTestParticipant> participantMap = new HashMap<String, ScriptedTestParticipant>();

    /**
     * a table used ot retain a handle on managers for enlisted BA  participants.
     */
    private HashMap<String, BAParticipantManager> managerMap = new HashMap<String, BAParticipantManager>();
}