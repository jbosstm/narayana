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

/**
 * Starts a transaction and enlists a single participant with instructions to prepare and commit
 * without error
 */
public class BASingleCoordinatorCompletionParticipantCloseTest implements XTSServiceTest
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

        UserBusinessActivity ba = UserBusinessActivityFactory.userBusinessActivity();


        // invoke the service via the client

        XTSServiceTestClient client = new XTSServiceTestClient();
        CommandsType commands = new CommandsType();
        ResultsType results = null;

        // start the transaction

        try {
            ba.begin();
        } catch (WrongStateException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BASingleCoordinatorCompletionParticipantCloseTest : txbegin failure " + exception);
            return;
        }

        // invoke the service to create a coordinaator completion participant and script it to complete and close
        commands = new CommandsType();
        commands.getCommandList().add("enlistCoordinatorCompletion");
        commands.getCommandList().add("complete");
        commands.getCommandList().add("close");

        try {
            results = client.serve("http://localhost:8080/xtstest/xtsservicetest1", commands);
        } catch (Exception e) {
            exception = e;
        }

        if (exception != null) {
            System.out.println("BASingleCoordinatorCompletionParticipantCloseTest : server failure " + exception);
            return;
        }

        for (String s : results.getResultList()) {
            System.out.println("BASingleCoordinatorCompletionParticipantCloseTest : enlistCoordinatorCompletion " + s);
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
            System.out.println("BASingleCoordinatorCompletionParticipantCloseTest : commit failure " + exception);
        }

        System.out.println("BASingleCoordinatorCompletionParticipantCloseTest : completed");
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public Exception getException() {
        return exception;
    }
}