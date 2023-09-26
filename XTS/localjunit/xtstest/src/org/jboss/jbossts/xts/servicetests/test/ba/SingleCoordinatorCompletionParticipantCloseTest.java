/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.xts.servicetests.test.ba;

import org.jboss.jbossts.xts.servicetests.service.XTSServiceTestServiceManager;
import org.jboss.jbossts.xts.servicetests.client.XTSServiceTestClient;
import org.jboss.jbossts.xts.servicetests.generated.CommandsType;
import org.jboss.jbossts.xts.servicetests.generated.ResultsType;
import org.jboss.jbossts.xts.servicetests.test.XTSServiceTestBase;
import org.jboss.jbossts.xts.servicetests.test.XTSServiceTest;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

import java.util.List;
import java.util.ArrayList;

/**
 * Starts a transaction and enlists a single participant with instructions to prepare and commit
 * without error
 */
public class SingleCoordinatorCompletionParticipantCloseTest extends XTSServiceTestBase implements XTSServiceTest
{
    public void run() {

        // wait a while so the service has time to start

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignore
        }

        String serviceURL1;

        serviceURL1 = System.getProperty(XTSServiceTest.SERVICE_URL1_KEY);

        if (serviceURL1 == null) {
            serviceURL1 = "http://localhost:8080/xtstest/xtsservicetest1";
        }

        addDefaultBinding("service1", serviceURL1);

        UserBusinessActivity ba = UserBusinessActivityFactory.userBusinessActivity();

        // start the transaction

        try {
            ba.begin();
        } catch (WrongStateException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        }

        if (exception != null) {
            error("txbegin failure " + exception);
            return;
        }

        List<String> resultsList;
        String participantId;

        List<String> commands = new ArrayList<String>();
        List<String> results = new ArrayList<String>();

        commands.add("serve");
        commands.add("{service1}");
        commands.add("enlistCoordinatorCompletion");
        commands.add("complete");
        commands.add("close");

        try {
            processCommands(commands, results);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            error("test failure " + exception);
            return;
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
            error("commit failure " + exception);
        }

        message("completed");

        isSuccessful = (exception == null);
    }
}