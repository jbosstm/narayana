/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.recovery;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ActionTestServer.java 2342 2006-03-30 13:06:17Z  $
 */

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.utils.Utility;

public class ActionTestServer
{
    @Test
    public void test() throws Exception
    {
        assertTrue(test_setup());

        try
        {
            test1();
            test2();
            test3();

            System.out.println(_unit_test + "tests passed: " + _tests_passed
                    + "  tests failed: " + _tests_failed);

            if (_tests_failed > 0)
            {
                fail();
            }

        }
        catch (Exception ex)
        {
            fail(_unit_test + "FATAL EXCEPTION: " + "tests passed: "
                    + _tests_passed + "  tests failed: " + _tests_failed);
        }
    }
    
    private static void test1()
    {
        try {
            System.err.println("test1");
            String test_uid = _test_uid_1.toString();
            String test_type = _test_tran_type_1;
            String reply;

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            _transaction_1.begin();

            CrashAbstractRecordImpl cr1 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_COMMIT);
            CrashAbstractRecordImpl cr2 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_COMMIT);

            int addcr1 = _transaction_1.add(cr1);
            int addcr2 = _transaction_1.add(cr2);

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            _transaction_1.commit();

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            if ((addcr1 == AddOutcome.AR_ADDED) &&
                    (addcr2 == AddOutcome.AR_ADDED)) {
                System.out.println(_unit_test + "test1 passed");
                _tests_passed++;
            } else {
                System.out.println(_unit_test + "test1 failed");
                _tests_failed++;
            }
        }
        catch (Exception ex) {
            System.err.println(_unit_test + " test1 " + ex);
            _tests_failed++;
        }
    }

    private static void test2()
    {
        try {
            System.err.println("test2");
            
            String test_uid = _test_uid_2.toString();
            String test_type = _test_tran_type_2;
            String reply;

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            _transaction_2.begin();

            CrashAbstractRecordImpl cr1 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_PREPARE);
            CrashAbstractRecordImpl cr2 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_PREPARE);

            int addcr1 = _transaction_2.add(cr1);
            int addcr2 = _transaction_2.add(cr2);

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            _transaction_2.commit();

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            if ((addcr1 == AddOutcome.AR_ADDED) &&
                    (addcr2 == AddOutcome.AR_ADDED)) {
                System.out.println(_unit_test + "test2 passed");
                _tests_passed++;
            } else {
                System.out.println(_unit_test + "test2 failed");
                _tests_failed++;
            }
        }
        catch (Exception ex) {
            System.err.println(_unit_test + " test2 " + ex);
            _tests_failed++;
        }
    }

    private static void test3()
    {
        try {
            System.err.println("test3");
            
            String test_uid = _test_uid_3.toString();
            String test_type = _test_tran_type_3;
            String reply;

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            _transaction_3.begin();

            CrashAbstractRecordImpl cr1 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.NO_CRASH);
            CrashAbstractRecordImpl cr2 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.NO_CRASH);

            int addcr1 = _transaction_3.add(cr1);
            int addcr2 = _transaction_3.add(cr2);

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            _transaction_3.commit();

            // send transaction id to client
            _to_client.println(test_type);
            _to_client.println(test_uid);
            _to_client.flush();

            if ((addcr1 == AddOutcome.AR_ADDED) &&
                    (addcr2 == AddOutcome.AR_ADDED)) {
                System.out.println(_unit_test + "test3 passed");
                _tests_passed++;
            } else {
                System.out.println(_unit_test + "test3 failed");
                _tests_failed++;
            }
        }
        catch (Exception ex) {
            System.err.println(_unit_test + " test3 " + ex);
            _tests_failed++;
        }
    }
    
    /**
     * Pre-test setup.
     */
    
    private static boolean test_setup()
    {
        boolean setupOk = false;

        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            _test_service_socket = new ServerSocket(_port);
            _test_socket = new Socket(host, _port);

            _from_client = new BufferedReader(new InputStreamReader
                    (_test_socket.getInputStream()));
            _to_client = new PrintWriter(new OutputStreamWriter
                    (_test_socket.getOutputStream()));

            _to_client.write(Utility.getProcessUid().stringForm()+"\n");
            _to_client.write(Utility.intToHexString(Utility.getpid())+"\n");
            
            _to_client.flush();

            _transaction_1 = new AtomicAction();
            _transaction_2 = new AtomicAction();
            _transaction_3 = new AtomicAction();

            _test_tran_type_1 = _transaction_1.type();
            _test_tran_type_2 = _transaction_2.type();
            _test_tran_type_3 = _transaction_3.type();

            _test_uid_1 = _transaction_1.getSavingUid();
            _test_uid_2 = _transaction_2.getSavingUid();
            _test_uid_3 = _transaction_3.getSavingUid();

            setupOk = true;
        }
        catch (Exception ex) {
            System.err.println("test_setup: Failed " + ex);
        }

        return setupOk;
    }
    
    private static final String _unit_test = "com.hp.mwtests.ts.arjuna.recovery.ActionTestServer: ";
    
    private static int _port = 4321;
    
    private static Socket _test_socket;
    private static ServerSocket _test_service_socket;

    private static BufferedReader _from_client;
    private static PrintWriter _to_client;

    private static AtomicAction _transaction_1;
    private static AtomicAction _transaction_2;
    private static AtomicAction _transaction_3;

    private static Uid _test_uid_1;
    private static Uid _test_uid_2;
    private static Uid _test_uid_3;

    private static String _test_tran_type_1;
    private static String _test_tran_type_2;
    private static String _test_tran_type_3;

    private static int _tests_passed = 0;
    private static int _tests_failed = 0;
}
