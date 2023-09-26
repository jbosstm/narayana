/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.xts.servicetests.test.at;

import org.jboss.jbossts.xts.servicetests.test.XTSServiceTestBase;
import org.jboss.jbossts.xts.servicetests.test.XTSServiceTest;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.mw.wst11.UserTransaction;
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
public class SingleParticipantPrepareAndCommitTest extends XTSServiceTestBase implements XTSServiceTest
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

        UserTransaction tx = UserTransactionFactory.userTransaction();

        // start the transaction

        try {
            tx.begin();
        } catch (WrongStateException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        }

        if (exception != null) {
            error("txbegin failure " + exception);
            return;
        }

        List<String> commands = new ArrayList<String>();
        List<String> results = new ArrayList<String>();

        commands.add("serve");
        commands.add("{service1}");
        commands.add("enlistDurable");
        commands.add("prepare");
        commands.add("commit");

        try {
            processCommands(commands, results);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            error("test failure " + exception);
            return;
        }

        // now commit the transaction

        try {
            tx.commit();
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