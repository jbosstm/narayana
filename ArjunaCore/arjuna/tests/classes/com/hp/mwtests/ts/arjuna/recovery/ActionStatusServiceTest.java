/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * $Id: ActionStatusServiceTest.java 2342 2006-03-30 13:06:17Z  $
 */

import java.io.*;
import java.net.*;
import java.util.*;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.recovery.Service;
import com.arjuna.ats.arjuna.recovery.ActionStatusService;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.recovery.Listener;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem;
import com.arjuna.ats.internal.arjuna.Implementations;
import com.arjuna.mwlabs.testframework.unittest.Test;

public class ActionStatusServiceTest extends Test
{
    private static boolean test_setup()
    {
        boolean setupOk = false;

        try
        {
            _tests_passed = 0;
            _tests_failed = 0;

            _test_service = new ActionStatusService();

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
        catch (Exception ex)
        {
            System.err.println("test_setup: Failed " + ex);
        }

        return setupOk;
    }

    public void run(String[] args)
    {
        if (test_setup())
        {
            try
            {
                test1();
                test2();
                test3();

                System.out.println(_unit_test + "tests passed: " + _tests_passed +
                        "  tests failed: " + _tests_failed);

                if ( _tests_failed > 0 )
                    assertFailure();
                else
                    assertSuccess();
            }
            catch (Exception ex)
            {
                System.err.println(_unit_test + "FATAL EXCEPTION: " +
                        "tests passed: " + _tests_passed +
                        "  tests failed: " + _tests_failed);
                assertFailure();
            }
        }
    }

    private static void test1()
    {
        try
        {
            String test_uid = _test_uid_1.toString();
            String test_type = _test_tran_type_1;

            int test_status1 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status2 = _test_service.getTransactionStatus("", test_uid);

            _transaction_1.begin();

            CrashAbstractRecordImpl cr1 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_COMMIT);
            CrashAbstractRecordImpl cr2 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_COMMIT);

            int addcr1 = _transaction_1.add(cr1);
            int addcr2 = _transaction_1.add(cr2);

            int test_status3 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status4 = _test_service.getTransactionStatus("", test_uid);

            _transaction_1.commit();

            int test_status5 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status6 = _test_service.getTransactionStatus("", test_uid);

            if ((addcr1 == AddOutcome.AR_ADDED) &&
                    (addcr2 == AddOutcome.AR_ADDED) &&
                    (ActionStatus.stringForm(test_status1).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status2).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status3).equals("ActionStatus.RUNNING")) &&
                    (ActionStatus.stringForm(test_status4).equals("ActionStatus.RUNNING")) &&
                    (ActionStatus.stringForm(test_status5).equals("ActionStatus.COMMITTED")) &&
                    (ActionStatus.stringForm(test_status6).equals("ActionStatus.COMMITTED")))
            {
                System.out.println(_unit_test + "test1: passed");
                _tests_passed++;
            }
            else
            {
                System.out.println(_unit_test + "test1: failed");
                _tests_failed++;
            }
        }
        catch (Exception ex)
        {
            System.err.println(_unit_test + " test1 " + ex);
            _tests_failed++;
        }
    }

    private static void test2()
    {
        try
        {
            String test_uid = _test_uid_2.toString();
            String test_type = _test_tran_type_2;

            int test_status1 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status2 = _test_service.getTransactionStatus("", test_uid);

            _transaction_2.begin();

            CrashAbstractRecordImpl cr1 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_PREPARE);
            CrashAbstractRecordImpl cr2 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.CRASH_IN_PREPARE);

            int addcr1 = _transaction_2.add(cr1);
            int addcr2 = _transaction_2.add(cr2);

            int test_status3 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status4 = _test_service.getTransactionStatus("", test_uid);

            _transaction_2.commit();

            int test_status5 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status6 = _test_service.getTransactionStatus("", test_uid);

            if ((addcr1 == AddOutcome.AR_ADDED) &&
                    (addcr2 == AddOutcome.AR_ADDED) &&
                    (ActionStatus.stringForm(test_status1).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status2).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status3).equals("ActionStatus.RUNNING")) &&
                    (ActionStatus.stringForm(test_status4).equals("ActionStatus.RUNNING")) &&
                    (ActionStatus.stringForm(test_status5).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status6).equals("ActionStatus.ABORTED")))
            {
                System.out.println(_unit_test + "test2: passed");
                _tests_passed++;
            }
            else
            {
                System.out.println(_unit_test + "test2: failed");
                _tests_failed++;
            }
        }
        catch (Exception ex)
        {
            System.err.println(_unit_test + " test2 " + ex);
            _tests_failed++;
        }
    }

    private static void test3()
    {
        try
        {
            String test_uid = _test_uid_3.toString();
            String test_type = _test_tran_type_3;

            int test_status1 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status2 = _test_service.getTransactionStatus("", test_uid);

            _transaction_3.begin();

            CrashAbstractRecordImpl cr1 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.NO_CRASH);
            CrashAbstractRecordImpl cr2 =
                    new CrashAbstractRecordImpl(CrashAbstractRecordImpl.NO_CRASH);

            int addcr1 = _transaction_3.add(cr1);
            int addcr2 = _transaction_3.add(cr2);

            int test_status3 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status4 = _test_service.getTransactionStatus("", test_uid);

            _transaction_3.abort();

            int test_status5 = _test_service.getTransactionStatus(test_type, test_uid);
            int test_status6 = _test_service.getTransactionStatus("", test_uid);

            if ((addcr1 == AddOutcome.AR_ADDED) &&
                    (addcr2 == AddOutcome.AR_ADDED) &&
                    (ActionStatus.stringForm(test_status1).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status2).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status3).equals("ActionStatus.RUNNING")) &&
                    (ActionStatus.stringForm(test_status4).equals("ActionStatus.RUNNING")) &&
                    (ActionStatus.stringForm(test_status5).equals("ActionStatus.ABORTED")) &&
                    (ActionStatus.stringForm(test_status6).equals("ActionStatus.ABORTED")))
            {
                System.out.println(_unit_test + "test3: passed");
                _tests_passed++;
            }
            else
            {
                System.out.println(_unit_test + "test3: failed");
                _tests_failed++;
            }
        }
        catch (Exception ex)
        {
            System.err.println(_unit_test + " test3 " + ex);
            _tests_failed++;
        }
    }

    private static final String _unit_test = "com.hp.mwtests.ts.arjuna.recovery.ActionStatusServiceTest: ";

    private static ActionStatusService _test_service;

    private static AtomicAction _transaction_1;
    private static AtomicAction _transaction_2;
    private static AtomicAction _transaction_3;

    private static Uid _test_uid_1;
    private static Uid _test_uid_2;
    private static Uid _test_uid_3;

    private static int _test_status_1;
    private static int _test_status_2;
    private static int _test_status_3;

    private static String _test_tran_type_1;
    private static String _test_tran_type_2;
    private static String _test_tran_type_3;

    private static int _tests_passed = 0;
    private static int _tests_failed = 0;
}
