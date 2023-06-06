/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package org.jboss.jbossts.xts.servicetests.test.at;

import org.jboss.jbossts.xts.servicetests.service.XTSServiceTestServiceManager;
import org.jboss.jbossts.xts.servicetests.client.XTSServiceTestClient;
import org.jboss.jbossts.xts.servicetests.generated.CommandsType;
import org.jboss.jbossts.xts.servicetests.generated.ResultsType;
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
 * Starts a transaction and enlist a single participants for each of several web services with instructions to
 * prepare and commit without error
 */
public class MultiServicePrepareAndCommitTest extends XTSServiceTestBase implements XTSServiceTest
{
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

        addDefaultBinding("service1", serviceURL1);
        addDefaultBinding("service2", serviceURL2);
        addDefaultBinding("service3", serviceURL3);

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

        // invoke the service and tell it to prepare and  commit
        List<String> commands = new ArrayList<String>();
        List<String> results = new ArrayList<String>();

        commands.add("block");
        commands.add("serve");
        commands.add("{service1}");
        commands.add("enlistDurable");
        commands.add("prepare");
        commands.add("commit");
        commands.add("next");
        commands.add("serve");
        commands.add("{service2}");
        commands.add("enlistDurable");
        commands.add("prepare");
        commands.add("commit");
        commands.add("next");
        commands.add("serve");
        commands.add("{service3}");
        commands.add("enlistDurable");
        commands.add("prepare");
        commands.add("commit");
        commands.add("endblock");

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