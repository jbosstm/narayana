/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;



import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.recovery.ActionStatusService;

public class ActionStatusServiceTest
{
    @Test
    public void test1() {
        int crashType = CrashAbstractRecordImpl.CRASH_IN_COMMIT;
        String[] expectedStatus = new String[] { "ActionStatus.ABORTED", "ActionStatus.RUNNING", "ActionStatus.COMMITTED" };
        runTest(crashType, expectedStatus, false);
    }

    @Test
    public void test2() {
        int crashType = CrashAbstractRecordImpl.CRASH_IN_PREPARE;
        String[] expectedStatus = new String[] { "ActionStatus.ABORTED", "ActionStatus.RUNNING", "ActionStatus.ABORTED" };
        runTest(crashType, expectedStatus, false);
    }

    @Test
    public void test3() {
        int crashType = CrashAbstractRecordImpl.NO_CRASH;
        String[] expectedStatus = new String[] { "ActionStatus.ABORTED", "ActionStatus.RUNNING", "ActionStatus.ABORTED" };
        runTest(crashType, expectedStatus, true);
    }

    public void runTest(int crashType, String[] expectedStatus, boolean doAbort)
    {
        ActionStatusService _test_service = new ActionStatusService();
        AtomicAction _transaction_1 = new AtomicAction();
        String _test_tran_type_1 = _transaction_1.type();
        Uid _test_uid_1 = _transaction_1.getSavingUid();

        String test_uid = _test_uid_1.toString();
        String test_type = _test_tran_type_1;

        assertEquals(expectedStatus[0], ActionStatus.stringForm( _test_service.getTransactionStatus(test_type, test_uid) ));
        assertEquals(expectedStatus[0], ActionStatus.stringForm( _test_service.getTransactionStatus("", test_uid) ));

        _transaction_1.begin();

        CrashAbstractRecordImpl cr1 =
                new CrashAbstractRecordImpl(crashType);
        CrashAbstractRecordImpl cr2 =
                new CrashAbstractRecordImpl(crashType);

        assertEquals(AddOutcome.AR_ADDED, _transaction_1.add(cr1) );
        assertEquals(AddOutcome.AR_ADDED, _transaction_1.add(cr2) );

        assertEquals(expectedStatus[1], ActionStatus.stringForm( _test_service.getTransactionStatus(test_type, test_uid) ));
        assertEquals(expectedStatus[1], ActionStatus.stringForm( _test_service.getTransactionStatus("", test_uid) ));

        if(doAbort) {
            _transaction_1.abort();
        } else {
            _transaction_1.commit();
        }

        assertEquals(expectedStatus[2], ActionStatus.stringForm( _test_service.getTransactionStatus(test_type, test_uid) ));
        assertEquals(expectedStatus[2], ActionStatus.stringForm( _test_service.getTransactionStatus("", test_uid) ));
    }
}