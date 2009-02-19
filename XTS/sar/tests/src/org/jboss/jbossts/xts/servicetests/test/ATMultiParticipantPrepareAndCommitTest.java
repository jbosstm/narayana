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
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

/**
 * Starts a transaction and enlist several participants for the same web service with instructions to
 * prepare and commit without error
 */
public class ATMultiParticipantPrepareAndCommitTest extends XTSServiceTestBase implements XTSServiceTest
{
    public void run() {

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

        UserTransaction tx = UserTransactionFactory.userTransaction();


        // invoke the service via the client

        XTSServiceTestClient client = new XTSServiceTestClient();
        CommandsType commands = new CommandsType();
        ResultsType results = null;

        // wait a while so the service has time to start

        // start the transaction

        try {
            tx.begin();
        } catch (WrongStateException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : txbegin failure " + exception);
            return;
        }

        // invoke the service and tell it to prepare and  commit
        commands = new CommandsType();
        commands.getCommandList().add("enlistDurable");
        commands.getCommandList().add("prepare");
        commands.getCommandList().add("commit");

        // call the same web service multiple times -- it's ok to use the samew commands list

        try {
            results = client.serve(serviceURL1, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : enlistDurable " + s);
        }

        try {
            results = client.serve(serviceURL1, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : enlistDurable " + s);
        }

        try {
            results = client.serve(serviceURL1, commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("ATMultiParticipantPrepareAndCommitTest : enlistDurable " + s);
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
            System.out.println("ATMultiParticipantPrepareAndCommitTest : commit failure " + exception);
        }

        System.out.println("ATMultiParticipantPrepareAndCommitTest : completed");

        isSuccessful = (exception == null);
    }
}