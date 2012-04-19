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

package org.jboss.jbossts.xts.servicetests.test.ba.subordinate;

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
 * Starts a transaction and enlists a cooprdinator completion participant with instructions to complete and close
 * without error then gets the web service to start a subordinate transaction and enlist several cooprdinator
 * completion participants for a second web service in the subtransaction with no instructions. The subordinate
 * participants are then told to exit. This checks that the subordinate coordinator works ok when all its
 * participants are read only and hence that no subordinate tx is logged at commit
 */
public class MultiParticipantParticipantCompletionParticipantCloseAndExitTest extends XTSServiceTestBase implements XTSServiceTest
{
    public void run() {

        // wait a while so the service has time to start

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignore
        }

        String serviceURL1;
        String subserviceURL1;

        serviceURL1 = System.getProperty(XTSServiceTest.SERVICE_URL1_KEY);

        if (serviceURL1 == null) {
            serviceURL1 = "http://localhost:8080/xtstest/xtsservicetest1";
        }

        subserviceURL1 = System.getProperty(XTSServiceTest.SUBORDINATE_SERVICE_URL1_KEY);

        if (subserviceURL1 == null) {
            subserviceURL1 = "http://localhost:8080/xtstest/xtssubordinateservicetest1";
        }

        addDefaultBinding("service1", serviceURL1);
        addDefaultBinding("subservice1", subserviceURL1);

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

        commands.add("block");
        commands.add("serve");
        commands.add("{service1}");
        commands.add("enlistParticipantCompletion");
        commands.add("bindings");
        commands.add("bind");
        commands.add("P1");
        commands.add("0");
        commands.add("next");
        commands.add("serve");
        commands.add("{subservice1}");
        commands.add("enlistParticipantCompletion");
        commands.add("bindings");
        commands.add("bind");
        commands.add("P2");
        commands.add("0");
        commands.add("next");
        commands.add("serve");
        commands.add("{subservice1}");
        commands.add("enlistParticipantCompletion");
        commands.add("bindings");
        commands.add("bind");
        commands.add("P3");
        commands.add("0");
        commands.add("next");
        commands.add("serve");
        commands.add("{service1}");
        commands.add("completed");
        commands.add("{P1}");
        commands.add("next");
        commands.add("serve");
        commands.add("{subservice1}");
        commands.add("exit");
        commands.add("{P2}");
        commands.add("next");
        commands.add("serve");
        commands.add("{subservice1}");
        commands.add("exit");
        commands.add("{P3}");
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

        // now close the activity

        try {
            Thread.sleep(10000);
            ba.close();
        } catch (TransactionRolledBackException e) {
            exception = e;
        } catch (UnknownTransactionException e) {
            exception = e;
        } catch (SystemException e) {
            exception = e;
        } catch (WrongStateException e) {
            exception = e;
        } catch (InterruptedException e) {
            exception = e;
        }

        if (exception != null) {
            error("commit failure " + exception);
        }

        message("completed");

        isSuccessful = (exception == null);
    }
}