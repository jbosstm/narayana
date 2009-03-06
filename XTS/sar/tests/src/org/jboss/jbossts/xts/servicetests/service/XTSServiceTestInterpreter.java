package org.jboss.jbossts.xts.servicetests.service;

import org.jboss.jbossts.xts.servicetests.service.participant.*;
import org.jboss.jbossts.xts.servicetests.client.XTSServiceTestClient;
import org.jboss.jbossts.xts.servicetests.generated.CommandsType;
import org.jboss.jbossts.xts.servicetests.generated.ResultsType;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.arjuna.wst11.BAParticipantManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.*;
import com.arjuna.wst.SystemException;

import javax.xml.ws.WebServiceException;
import javax.xml.namespace.QName;

/**
 * A class which provides the ability to interpret XTS service test commands. Each of the XTS service test
 * classes imnplementing a specific test extends this class so that it can drive the test from a command
 * script. Each of the XTS service test endpoints employs an instance of this class to process comands
 * which are dispatched to it either from one of the test class instances or recursively via another
 * intermediate service.
 *
 * The interpreter executes a list of Strings which represents one or more commands each with its own
 * sequence of associated arguments. It returns a list of strings indicating the outcome of processing
 * the commands. It throws an exception if command processing fails.
 *
 * A simple command comprises a list of Strings headed by one of a fixed set of command keywords followed by
 * zero or more argument strings. Command arguments are either simple string literals or bound variable references.
 * A variable reference is an alphanumeric variable name bracketed by '{' and '}' characters. The interpreter
 * resolves this to a string value by looking up the variable in its binding list and uses this vale as the
 * argument for th command. It is possible to seed the binding list with default values before processing a
 * sequence of commands. Alternatively, bindings may be added during execution of block commands.
 *
 * A block commands comprises a list of Strings headed by the keyword "block" and terminated by the keyword
 * "endblock". Intervening elements of the list comprise simple commands separated either by "next" keywords
 * or by bind commands terminated with a "next" keyword. A bind commands is a list of strings headed by keyword
 * "bind" and succeeded by a sequence of bind operations. A bind operation operates on the results returned from
 * the last executed command and comes in one of three formats:
 * <ul>
 *   <li>set var idx
 *   <li>check idx value/{var}"
 *   <li>output idx
 * </ul>
 * A set operation binds a variable to the nth result returned in the results list of the previously executed
 * command. A check operation checks that the nth result returned in the results list of the previously executed
 * command has a specific value, throwing an exception if not. An output operation appends the nth result returned
 * in the results list of the previously executed command to the results list for the block command.
 *
 * The command syntax is defined as follows:
 *  
 * <pre>
 * command ::= simple_command | block_command ==> resultList
 *
 * block_command ::= "block" command (block_trailing)* "endblock"
 *
 * block_trailing ::= (bind_command)? "next" command
 *
 * bind_command  ::= "bind" (bind_operation)+ "next"
 *
 * bind_operation ::= "set" <alphanum> <num> |
 *              "check" <num> <alphanum> |
 *              "output" <num>
 *
 * simple_command :=
 *   "enlistDurable" (ATParticipantInstruuction)* ==> DurableId ;
 *   "enlistVolatile"  (ATParticipantInstruuction)* ==> VolatileId ;
 *   "enlistCoordinatorCompletion" (BACoordinatorCompletionInstruuction)* ==> BACoordinatorCompletionId ;
 *   "enlistParticipantCompletion" (BAParticipantCompletionInstruuction)* ==> BAParticipantCompletionId ;
 *   "addCommands" ((DurableId | VolatileId) (ATParticipantInstruuction)* ==> "ok" |
 *              ((BAParticipantCompletionId | BACoordinatorCompletionId) BACoordinatorCompletionInstruuction)*) ==> "ok" ;
 *   "fail" (BAParticipantCompletionId | BACoordinatorCompletionId) ==> "ok" ;
 *   "exit" (BAParticipantCompletionId | BACoordinatorCompletionId) ==> "ok" ;
 *   "cannotComplete" BAParticipantCompletionId ==> "ok" ;
 *   "completed" BAParticipantCompletionId ==> "ok" ;
 *   "serve" URL command ==> resultsList
 *   "subtransaction" ==> subtransactionId ;
 *   "subactivity" ==> subactivityId ;
 *   "subtransactioncommands" subtransactionId serviceURL (command) ;
 *   "subactivitycommands" subactivityId serviceURL (command)
 *
 * ATParticipantInstruuction ::=
 *   "prepare" ;
 *   "prepareSystemException" ;
 *   "prepareWrongStateException" ;
 *   "commit" ;
 *   "commitSystemException" ;
 *   "commitWrongStateException"
 *
 * BAParticipantCompletionInstruuction ::=
 *   "close" ;
 *   "closeSystemException" ;
 *   "closeWrongStateException" ;
 *   "cancel" ;
 *   "cancelSystemException" ;
 *   "cancelWrongStateException" ;
 *   "cancelFaultedException" ;
 *   "compensate" ;
 *   "compensateSystemException" ;
 *   "compensateWrongStateException" ;
 *   "compensateFaultedException"
 *
 * BAParticipantCompletionInstruuction  ::=
 *   "complete" ;
 *   "completeSystemException" ;
 *   "completeWrongStateException" ;
 *
 * DurableId ::=
 *   "org.jboss.jbossts.xts.servicetests.DurableTestParticipant.<num>"
 * VolatileId ::=
 *   "org.jboss.jbossts.xts.servicetests.VolatileTestParticipant.<num>"
 * BACoordinatorCompletionId ::=
 *   "org.jboss.jbossts.xts.servicetests.CoordinatorCompletionTestParticipant.<num>"
 * BAParticipantCompletionId ::=
 *   "org.jboss.jbossts.xts.servicetests.ParticipantCompletionTestParticipant.<num>"
 *
 * subtransactionId is a String of the form
 *   "transaction.at.<num>"
 *
 * subactivityId is a String of the form
 *   "transaction.ba.<num>"
 *</pre>
 * where
 *
 * <num> is a sequence of base 10 digits and <alphanum> is a sequence of
 * alphanumeric characters
 *
 * serviceURL is a URL identifying an instance of the test service to which commands are to be
 * recursively dispatched within the relevant subtransaction or subactivity
 */
public class XTSServiceTestInterpreter
{
    /// public API

    /**
     * lookup (or create and index) then return a service implementation keyed by a specific servlet path
     * @param servletPath the key used to identify a service obtained from an HTTP request's servlet path
     * @return the associated service instance
     */
    public static synchronized XTSServiceTestInterpreter getService(String servletPath)
    {
        XTSServiceTestInterpreter service = serviceMap.get(servletPath);
        if (service == null) {
            service = new XTSServiceTestInterpreter();
            serviceMap.put(servletPath, service);
        }

        return service;
    }

    /**
     * simple command interpreter which executes the commands in the command list, inserting the
     * corresponding results in the results list. n.b. this method should only ever be invoked
     * from within an AT or BA transaction.
     *
     * @param commandList a list of command strings
     * @param resultsList a list into which results strings are to be inserted
     */
    public void processCommands(List<String> commandList, List<String> resultsList)
            throws WebServiceException
    {
        HashMap<String, String> bindings = new HashMap<String, String>(defaultBindings);
        processCommands(commandList, resultsList, bindings);
    }

    /**
     * add a binding to the default binding set used to supply command parameters
     * @param var
     * @param val
     */
    public void addDefaultBinding(String var, String val)
    {
        defaultBindings.put(var, val);
    }

    /// overrideable methods

    /**
     * set up a specific service instance with all the values it requires
     */

    protected XTSServiceTestInterpreter()
    {
        participantMap = new HashMap<String, ScriptedTestParticipant>();
        managerMap = new HashMap<String, BAParticipantManager>();
        subordinateTransactionMap = new HashMap<String, TxContext>();
        subordinateActivityMap = new HashMap<String, TxContext>();
        defaultBindings = new HashMap<String, String>();
        client = null;
    }

    /// implementation of the interpreter functionality
    
    /**
     * simple command interpreter which executes the commands in the command list, inserting the
     * corresponding results in the results list and using the supplied bindings list
     * to provide values for any parameters supplied in the commands and to bind any results
     * obtained by executing the commands
     *
     * @param commandList a list of command strings
     * @param resultsList a list into which results strings are to be inserted
     * @param bindings a list of bound variables to be substituted into commands
     * or updated with new bindings
     */
    private void processCommands(List<String> commandList, List<String> resultsList, HashMap<String, String> bindings)
            throws WebServiceException
    {
        int size = commandList.size();
        String command = commandList.remove(0);
        size--;

        // check against each of the possible commands

        if (command.equals("block")) {
            // we don't bind the command block immediately since bindings may be produced
            // by intermediate bind commands
            processCommandBlock(commandList, resultsList, bindings);
        } else {
            int idx = 0;
            if (command.equals("enlistDurable")) {
// enlistment commands
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
            } else if (command.equals("addCommands")) {
// add extra commands to a participant script
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
                bindCommands(commandList, bindings);
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
            } else if (command.equals("serve")) {
// dispatch commands to a server for execution
                // we should find a web service URL and a list of commands to dispatch to that service
                String url = commandList.remove(idx);
                size--;
                // we throw an error if the server url is unbound but we allow
                // unbound variables in the command list passed on to the server
                // since they may be bound by bind commands in the command list
                // being served.

                url = bindCommand(url, bindings, true);

                bindCommands(commandList, bindings, false);

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
            } else if (command.equals("subtransaction")) {
// create subordinate AT transaction
// this is surplus to requirements since we should really be running against a service which uses
// the subordinate interposition JaxWS handler to install a subordinate transaction before
// entering the service method. we ought to test that handler rather than hand crank the
// interposition in the service
                TxContext currentTx;
                TxContext newTx;
                try {
                    currentTx = TransactionManager.getTransactionManager().currentTransaction();
                } catch (SystemException e) {
                    throw new WebServiceException("subtransaction currentTransaction() failed with exception " + e);
                }

                try {
                    UserTransaction userTransaction = UserTransactionFactory.userSubordinateTransaction();
                    userTransaction.begin();
                    newTx = TransactionManager.getTransactionManager().currentTransaction();
                } catch (Exception e) {
                    throw new WebServiceException("subtransaction begin() failed with exception " + e);
                }
                String id = transactionId("at");
                subordinateTransactionMap.put(id, newTx);
                resultsList.add(id);
            } else if (command.equals("subactivity")) {
// create subordinate BA transaction
// this is surplus ot requirements since we should really be running against a service which uses
// the subordinate interposition JaxWS handler to install a subordinate activity before
// entering the service method. we ought to test that handler rather than hand crank the
// interposition in the service
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
            } else if (command.equals("subtransactionserve")) {
// dispatch commands in a subordinate transaction or activity
                // we should find the id of a subordinate transaction, a web service URL
                // and a list of commands to dispatch to that transaction
                // the txid and url must be resolved if supplied as bindings
                String txId = bindCommand(commandList.remove(idx), bindings, true);
                size--;
                String url = bindCommand(commandList.remove(idx), bindings,  true);
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

                // we allow unresolved variable references in the rest of the command list as
                // they may be satisfied by embedded bind commands

                bindCommands(commandList,  bindings,  false);

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
            } else if (command.equals("subactivityserve")) {
// dispatch commands in a subordinate transaction or activity
                // we should find the id of a subordinate transaction, a web service URL
                // and a list of commands to dispatch to that transaction
                // the txid and url must be resolved if supplied as bindings
                String txId = bindCommand(commandList.remove(idx), bindings, true);
                size--;
                String url = bindCommand(commandList.remove(idx), bindings,  true);
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

                // we allow unresolved variable references in the rest of the command list as
                // they may be satisfied by embedded bind commands

                bindCommands(commandList, bindings,  false);

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
        }
    }

    /**
     * execute a block of commands by recursively executing each embedded command list. results are
     * verified and bound to command variables in accordance with embedded bind commands. command
     * variable references in embedded command lists are substituted with the corresponding bound
     * values before recursive execution is performed.
     *
     * @param commandList
     * @param resultsList
     * @throws WebServiceException
     */
    private void processCommandBlock(List<String> commandList, List<String> resultsList, HashMap<String, String> bindings)
            throws WebServiceException
    {
        // break up the command block into a list of nested command lists. successive command lists will
        // be separated by a "next" or "bind" command. the block will be terminated by an "endblock" command.
        // nested commands may themselves be block commands.

        List<List<String>> subcommandsList = new ArrayList<List<String>>();
        List<String> subcommands = new ArrayList<String>();
        // each subcommand list needs to start with next or bind
        subcommands.add("next");
        int size = commandList.size();
        int depth = 0;
        int idx;
        for (idx = 0; idx < size; idx++)
        {
            String command = commandList.get(idx);
            if (depth > 0) {
                // track nesting levels
                if (command.equals("block")) {
                    depth++;
                } else if (command.equals("endblock")) {
                    depth--;
                }
                subcommands.add(command);
            } else {
                if (command.equals("block")) {
                    // add nested block command to sublist
                    subcommands.add(command);
                    depth++;
                } else if (command.equals("next")) {
                    // create new sublist starting with next
                    subcommandsList.add(subcommands);
                    subcommands = new ArrayList<String>();
                    subcommands.add(command);
                } else if (command.equals("bind")) {
                    // create new sublist starting with bind
                    subcommandsList.add(subcommands);
                    subcommands = new ArrayList<String>();
                    subcommands.add(command);
                } else if (command.equals("endblock")) {
                    // should be at end of commandlist
                    if (idx != size - 1) {
                        throw new WebServiceException("block commands reached endblock before end of block list");
                    }
                    subcommandsList.add(subcommands);
                }
            }
        }
        // ok, we should have a list of command lists all starting with either next or bind
        // now we execute each next command list in turn after substituting bound variables
        // of the form $xxx and we execute each bind command list by testing result values
        // against literals or binding result values to variables and possibly outputting
        // results to the global results list

        List<String> subresultsList = null;
        size = subcommandsList.size();

        for (idx = 0; idx < size; idx++) {
            subcommands = subcommandsList.get(idx);
            String command = subcommands.remove(0);
            if (command.equals("next")) {
                subresultsList = new ArrayList<String>();
                bindCommands(subcommands, bindings);
                processCommands(subcommands, subresultsList);
            } else if (command.equals("bind")) {
                bindResults(subcommands, subresultsList, resultsList, bindings);
            }
        }

        resultsList.addAll(subresultsList);
    }

    /**
     * for each command in the command list which contains variable references substitute a command containing
     * the value for the variable found in bindings. variables are mentioned by wrappng their name in braces.
     *
     * @param commands the list of commands to be processed
     * @param bindings a map from variable names to the associated values
     * @throws WebServiceException if a variable reference is invalidly formatted or refers
     * to an unbound variable
     */
    private void bindCommands(List<String> commands, HashMap<String, String> bindings)
            throws WebServiceException
    {
        bindCommands(commands, bindings, true);
    }
    /**
     * for each command in the command list which contains variable references substitute a command containing
     * the value for the variable found in bindings. variables are mentioned by wrappng their name in braces.
     *
     * @param commands the list of commands to be processed
     * @param bindings a map from variable names to the associated values
     * @param mustResolve is true if references to unbound variables should result in a thrown exception and false
     * if they should be tolerated by being left unsubstituted
     * @throws WebServiceException if a variable reference is invalidly formatted or refers
     * to an unbound variable when mustResolve is true
     */
    private void bindCommands(List<String> commands, HashMap<String, String> bindings, boolean mustResolve)
            throws WebServiceException
    {
        int size = commands.size();
        int idx;
        for (idx = 0; idx < size; idx++) {
            // pop the command and append either the original or a substituted copy
            String command = commands.remove(0);
            String newCommand = bindCommand(command, bindings, mustResolve);
            commands.add(newCommand.toString());
        }
    }

    /**
     * substitute any bound variables found in the supplied command
     * @param command the command to be substituted
     * @param bindings the map from currently bound variables to their string values
     * @param mustResolve is true if references to unbound variables should result in a thrown exception and false
     * if they should be tolerated by being left unsubstituted
     * @return the substituted command or the original command if it contains no variable references
     * @throws WebServiceException if a variable reference is invalidly formatted or refers
     * to an unbound variable
     */
    private String bindCommand(String command, HashMap<String, String> bindings, boolean mustResolve)
            throws WebServiceException
    {
        if (command.contains("{")) {
            StringBuffer newCommandBuffer = new StringBuffer();
            int len = command.length();
            int pos =  0;
            while (pos < len) {
                char c = command.charAt(pos);
                if (c == '{') {
                    // must have room for at least one character and a closing brace
                    if (pos > len-2) {
                        throw new WebServiceException("bindCommand : invalid variable reference " + command + " @ " + pos);
                    }
                    // brace cannot be next character so start search at pos+2
                    int endpos = command.indexOf('}', pos + 2);
                    if (endpos < 0) {
                        throw new WebServiceException("bindCommand : invalid variable reference " + command + " @ " + pos);
                    }
                    String var = command.substring(pos + 1, endpos);
                    // var must be alphanumeric
                    if (!var.matches("[0-9a-zA-Z]+")) {
                        throw new WebServiceException("bindCommand : invalid variable name " + command + " @ " + pos);
                    }
                    String val = bindings.get(var);
                    if (val == null) {
                        if (mustResolve) {
                            throw new WebServiceException("bindCommand : unbound variable " + command + " @ " + pos);
                        } else {
                            newCommandBuffer.append('{');
                            newCommandBuffer.append(var);
                            newCommandBuffer.append('}');
                        }
                    } else {
                        newCommandBuffer.append(val);
                    }
                    pos = endpos+1;
                } else {
                    newCommandBuffer.append(c);
                }
            }
            return newCommandBuffer.toString();
        } else {
            return command;
        }
    }

    /**
     * process each command in the binding commands list. binding commands include "set var idx"
     * which binds a variable to a value obtained from inResults, "check idx value" which checks that a
     * value obtaind from inResults has a given value and "output idx", which inserts a value obtained
     * from inResults into outResults.
     * @param commands
     * @param inResults
     * @param outResults
     * @param bindings
     * @throws WebServiceException
     */
    private void bindResults(List<String> commands, List<String> inResults, List<String> outResults, HashMap<String, String> bindings)
            throws WebServiceException
    {
        int size = commands.size();
        int idx;
        for (idx = 0; idx < size; idx++) {
            String command = commands.get(idx);
            String[] tokens = command.split(" +");
            if (tokens[0].equals("set")) {
                // set var idx : var <== inResults[idx]
                if ((tokens.length != 3) || !tokens[1].matches("[0-9a-zA-Z]+") || !tokens[2].matches("[0-9]")) {
                    throw new WebServiceException("bindResults : invalid set format " + command);
                }
                String var = tokens[1];
                Integer resultIdx;
                try {
                    resultIdx = Integer.valueOf(tokens[2]);
                } catch (NumberFormatException nfe) {
                    throw new WebServiceException("bindResults : invalid set index " + command);
                }
                if (resultIdx < 0 || resultIdx >= inResults.size()) {
                    throw new WebServiceException("bindResults : invalid set index " + command);
                }
                bindings.put(var, inResults.get(resultIdx));
            } else  if (tokens[0].equals("check")) {
                // test value idx : ensure inResults[idx] == value
                if ((tokens.length != 3) || !tokens[1].matches("[0-9a-zA-Z]+") || !tokens[2].matches("[0-9]")) {
                    throw new WebServiceException("bindResults : invalid check format " + command);
                }
                String val = tokens[1];
                Integer resultIdx;
                try {
                    resultIdx = Integer.valueOf(tokens[2]);
                } catch (NumberFormatException nfe) {
                    throw new WebServiceException("bindResults : invalid check index " + command);
                }
                if (resultIdx < 0 || resultIdx >= inResults.size()) {
                    throw new WebServiceException("bindResults : invalid check index " + command);
                }
                String result = inResults.get(idx);
                if (!result.equals(val)) {
                    throw new WebServiceException("bindResults : check failed, expecting  " + val + " got " + result);
                }
            } else  if (tokens[0].equals("output")) {
                // output idx : outResults add inResults[idx];
                if ((tokens.length != 2) || !tokens[1].matches("[0-9]")) {
                    throw new WebServiceException("bindResults : invalid output format " + command);
                }
                Integer resultIdx;
                try {
                    resultIdx = Integer.valueOf(tokens[1]);
                } catch (NumberFormatException nfe) {
                    throw new WebServiceException("bindResults : invalid output index " + command);
                }
                if (resultIdx < 0 || resultIdx >= inResults.size()) {
                    throw new WebServiceException("bindResults : invalid output index " + command);
                }
                String result = inResults.get(idx);
                outResults.add(result);
            } else {
                throw new WebServiceException("bindResults : invalid bind commmand " + command);
            }
        }
    }

    /**
     * utiilty method provided to simplify recursive dispatch of commands to another web service. this is
     * intended to be used to create and drive participants in subordinate transactions
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
     * map used to identify specific service implementations with server url paths
     */

    static private HashMap<String, XTSServiceTestInterpreter> serviceMap = new HashMap<String, XTSServiceTestInterpreter>();

    /**
     * a table used to retain a handle on enlisted participants so that they can be driven by the client to
     * perform actions not contained in the original command script.
     */
    private HashMap<String, ScriptedTestParticipant> participantMap;

    /**
     * a table used to retain a handle on managers for enlisted BA  participants.
     */
    private HashMap<String, BAParticipantManager> managerMap;

    /**
     * a table used to retain a handle on AT subordinate transactions
     */
    private HashMap<String, TxContext> subordinateTransactionMap;

    /**
     * a table used to retain a handle on BA subactivities
     */
    private HashMap<String, TxContext> subordinateActivityMap;

    /**
     * a table of default command variable bindings
     */
    private HashMap<String, String> defaultBindings;

    /**
     * a client used to propagate requests recursively from within subtransactions or subactivities
     */

    private XTSServiceTestClient client;
}
