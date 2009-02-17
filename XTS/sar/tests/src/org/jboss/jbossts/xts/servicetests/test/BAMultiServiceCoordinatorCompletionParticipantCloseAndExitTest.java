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

package org.jboss.jbossts.xts.servicetests.test;

import org.jboss.jbossts.xts.servicetests.service.XTSServiceTestServiceManager;
import org.jboss.jbossts.xts.servicetests.client.XTSServiceTestClient;
import org.jboss.jbossts.xts.servicetests.generated.CommandsType;
import org.jboss.jbossts.xts.servicetests.generated.ResultsType;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import java.util.List;

/**
 * Starts a transaction and enlists a single participant in each of multiple services with instructions to
 * prepare and commit without error
 */
public class BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest implements XTSServiceTest
{
    private boolean isSuccessful = false;
    private Exception exception;

    public void run() {

        // wait a while so the service has time to start

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignore
        }

        String serviceURL1;
        String serviceURL2;
        String serviceURL3;

        serviceURL1 = System.getProperty(XTSServiceTest.SERVICE_URL1_KEY);
        serviceURL2 = System.getProperty(XTSServiceTest.SERVICE_URL2_KEY);
        serviceURL3 = System.getProperty(XTSServiceTest.SERVICE_URL3_KEY);

        if (serviceURL1 == null) {
            serviceURL1 = "http://localhost:8080/xtstest/xtsservicetest1";
        }

        if (serviceURL2 == null) {
            serviceURL2 = "http://localhost:8080/xtstest/xtsservicetest2";
        }

        if (serviceURL3 == null) {
            serviceURL3 = "http://localhost:8080/xtstest/xtsservicetest3";
        }

        UserBusinessActivity ba = UserBusinessActivityFactory.userBusinessActivity();


        // invoke the service via the client

        XTSServiceTestClient client = new XTSServiceTestClient();
        CommandsType commands = new CommandsType();
        ResultsType results = null;
        List<String> resultsList;
        String participantId1;


        // start the transaction

        try {
            ba.begin();
        } catch (WrongStateException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : txbegin failure " + exception);
            return;
        }

        // invoke the service to create a coordinaator completion participant and script it to complete and close
        commands = new CommandsType();
        commands.getCommandList().add("enlistCoordinatorCompletion");
        commands.getCommandList().add("complete");
        commands.getCommandList().add("close");

        try {
            results = client.serve(serviceURL1, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : server failure " + exception);
            return;
        }

        resultsList = results.getResultList();
        participantId1 = resultsList.get(0);

        for (String s : results.getResultList()) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : enlistCoordinatorCompletion " + s);
        }

        // invoke the second service to create a coordinator completion participant
        // and close
        commands = new CommandsType();
        commands.getCommandList().add("enlistCoordinatorCompletion");

        try {
            results = client.serve(serviceURL2, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : enlistCoordinatorCompletion " + s);
        }

        // invoke the third service to create a coordinaator completion participant and script it to
        // complete and close

        commands = new CommandsType();
        commands.getCommandList().add("enlistCoordinatorCompletion");
        commands.getCommandList().add("complete");
        commands.getCommandList().add("close");

        try {
            results = client.serve(serviceURL3, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : enlistCoordinatorCompletion " + s);
        }

        // invoke the service scripting the first participant to exit
        commands = new CommandsType();
        commands.getCommandList().add("exit");
        commands.getCommandList().add(participantId1);

        try {
            results = client.serve(serviceURL1, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BAMultiParticipantCoordinatorCompletionParticipantCloseAndExitTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("BAMultiParticipantCoordinatorCompletionParticipantCloseAndExitTest : exit " + participantId1 + " " + s);
        }

        // now close the activity

        try {
            ba.close();
        } catch (TransactionRolledBackException e) {
            exception = e;
        } catch (UnknownTransactionException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        } catch (WrongStateException e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : commit failure " + exception);
        }

        System.out.println("BAMultiServiceCoordinatorCompletionParticipantCloseAndExitTest : completed");

        isSuccessful = (exception == null);
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public Exception getException() {
        return exception;
    }
}