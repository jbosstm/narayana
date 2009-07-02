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
 * $Id: ActionStatusServiceTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.recovery.ActionStatusService;

import org.junit.Test;
import static org.junit.Assert.*;

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
