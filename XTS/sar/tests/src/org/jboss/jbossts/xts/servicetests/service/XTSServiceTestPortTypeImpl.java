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
import org.jboss.jbossts.xts.servicetests.client.XTSServiceTestClient;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.namespace.QName;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.HashMap;

import com.arjuna.mw.wst11.*;
import com.arjuna.mw.wst.TxContext;
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

    @Resource WebServiceContext context;

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

        MessageContext messageContext = context.getMessageContext();
        HttpServletRequest servletRequest = ((HttpServletRequest)messageContext.get(MessageContext.SERVLET_REQUEST));
        String path = servletRequest.getServletPath();

        System.out.println("service " + path);
        for (String s : commandList)
        {
            System.out.println("  command " + s);
        }

        int size = commandList.size();
        int idx = 0;

        String command = commandList.remove(idx);
        size--;

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
            for (idx = 0; idx < size; idx++) {
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
            for (idx = 0; idx < size; idx++) {
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
            for (idx = 0; idx < size; idx++) {
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
            for (idx = 0; idx < size; idx++) {
                participant.addCommand(commandList.get(idx));
            }
            participantMap.put(id, participant);
            resultsList.add(id);
        } else if (command.equals("commands")) {
// add extra commands to a participant script
            String id = commandList.remove(idx);
            size--;
            ScriptedTestParticipant participant = participantMap.get(id);
            if (participant != null) {
                for (idx = 0; idx < size; idx++)
                {
                    participant.addCommand(commandList.get(idx));
                }
                resultsList.add("ok");
            } else {
                throw new WebServiceException("addCommands failed to find participant " + id);
            }
        } else if (command.equals("exit")) {
// initiate BA manager activities
            String id = commandList.remove(idx);
            size--;
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
            String id = commandList.remove(idx);
            size--;
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
            String id = commandList.remove(idx);
            size--;
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
            String id = commandList.remove(idx);
            size--;
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
        } else if (command.equals("subtransaction")) {
// create subordinate AT transaction
            TxContext currentTx;
            TxContext newTx;
            try {
                currentTx = TransactionManager.getTransactionManager().currentTransaction();
            } catch (SystemException e) {
                throw new WebServiceException("subtransaction currentTransaction() failed with exception " + e);
            }

            try {
                UserTransaction userTransaction = UserTransactionFactory.userTransaction();
                userTransaction.beginSubordinate();
                newTx = TransactionManager.getTransactionManager().currentTransaction();
            } catch (Exception e) {
                throw new WebServiceException("subtransaction begin() failed with exception " + e);
            }
            String id = transactionId("at");
            subordinateTransactionMap.put(id, newTx);
            resultsList.add(id);
        } else if (command.equals("subactivity")) {
// create subordinate BA transaction
            TxContext currentTx;
            TxContext newTx;
            try {
                currentTx = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();
            } catch (SystemException e) {
                throw new WebServiceException("subtransaction currentTransaction() failed with exception " + e);
            }

            try {
                UserBusinessActivity userBusinessActivity = UserBusinessActivityFactory.userBusinessActivity();
                // this is nto implemented yet!!!
                // userBusinessActivity.beginSubordinate();
                // and this will fail with a WrongStateException
                userBusinessActivity.begin();
                newTx = BusinessActivityManager.getBusinessActivityManager().currentTransaction();
            } catch (Exception e) {
                throw new WebServiceException("subtransaction begin() failed with exception " + e);
            }
            String id = transactionId("ba");
            subordinateActivityMap.put(id, newTx);
            resultsList.add(id);
        } else if (command.equals("subtransactioncommands")) {
// dispatch commands in a subordinate transaction or activity
            // we should find the id of a subordinate transaction, a web service URL
            // and a list of commands to dispatch to that transaction
            String txId = commandList.remove(idx);
            size--;
            String url = commandList.remove(idx);
            size--;

            TxContext newTx = subordinateTransactionMap.get(txId);
            if (newTx != null) {
                try {
                    TransactionManager.getTransactionManager().resume(newTx);
                } catch (Exception e) {
                    throw new WebServiceException("subtransactioncommands resume() failed with exception " + e);
                }
            } else {
                throw new WebServiceException("subtransactioncommands unknown subordinate transaction id " + txId);
            }
            // ok, now we install the relevant transaction and then just pass the commands on to
            // the web service

            CommandsType newCommands = new CommandsType();
            List<String> newCommandList = newCommands.getCommandList();
            for (int i = 0; i < size; i++) {
                newCommandList.add(commandList.get(i));
            }
            ResultsType subResults = serveSubordinate(url, newCommands);
            List<String> subResultsList = subResults.getResultList();
            size = subResultsList.size();
            for (idx = 0; idx < size; idx++) {
                resultsList.add(subResultsList.get(idx));
            }
        } else if (command.equals("subactivitycommands")) {
// dispatch commands in a subordinate transaction or activity
            // we should find the id of a subordinate transaction, a web service URL
            // and a list of commands to dispatch to that transaction
            String txId = commandList.remove(idx);
            size--;
            String url = commandList.remove(idx);
            size--;

            TxContext newTx = subordinateActivityMap.get(txId);
            if (newTx != null) {
                try {
                    TransactionManager.getTransactionManager().resume(newTx);
                } catch (Exception e) {
                    throw new WebServiceException("subactivitycommands resume() failed with exception " + e);
                }
            } else {
                throw new WebServiceException("subactivitycommands unknown subordinate transaction id " + txId);
            }
            // ok, now we install the relevant transaction and then just pass the commands on to
            // the web service

            CommandsType newCommands = new CommandsType();
            List<String> newCommandList = newCommands.getCommandList();
            for (int i = 0; i < size; i++) {
                newCommandList.add(commandList.get(i));
            }
            ResultsType subResults = serveSubordinate(url, newCommands);
            List<String> subResultsList = subResults.getResultList();
            size = subResultsList.size();
            for (idx = 0; idx < size; idx++) {
                resultsList.add(subResultsList.get(idx));
            }
        }

        return results;
    }

    /**
     * utiilty method provided to simplify recursive dispatch of commands to another web service. this is
     * intended to be used to create and drive partiicpants in subordinate transactions
     * @param url
     * @param commands
     * @return
     */
    private ResultsType serveSubordinate(String url, CommandsType commands)
    {
        return getClient().serve(url, commands);
    }

    private synchronized XTSServiceTestClient getClient()
    {
        if (client == null) {
            client = new XTSServiceTestClient();
        }

        return client;
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
     * obtain a new transaction name starting with a transaction prefix and terminated
     * with the supplied suffix and a unique trailing number
     * @param suffix a component to be added to the name before the counter identifying the type of
     * transaction
     * @return
     */
    private synchronized String transactionId(String suffix)
    {
        return Constants.TRANSACTION_ID_PREFIX + suffix + "." + nextId++;
    }

    /**
     * a table used to retain a handle on enlisted participants so that they can be driven by the client to
     * perform actions not contained in the original command script.
     */
    private static HashMap<String, ScriptedTestParticipant> participantMap = new HashMap<String, ScriptedTestParticipant>();

    /**
     * a table used to retain a handle on managers for enlisted BA  participants.
     */
    private static HashMap<String, BAParticipantManager> managerMap = new HashMap<String, BAParticipantManager>();

    /**
     * a table used to retain a handle on AT subordinate transactions
     */
    private static HashMap<String, TxContext> subordinateTransactionMap = new HashMap<String, TxContext>();

    /**
     * a table used to retain a handle on BA subactivities
     */
    private static HashMap<String, TxContext> subordinateActivityMap = new HashMap<String, TxContext>();

    /**
     * a client used to propagate requests recursively from within subtransactions or subactivities
     */

    private static XTSServiceTestClient client = null;
}