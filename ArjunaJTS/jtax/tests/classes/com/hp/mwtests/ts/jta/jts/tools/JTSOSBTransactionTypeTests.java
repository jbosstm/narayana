/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import javax.management.MBeanException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransaction;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTSOSBTransactionTypeTests extends JTSOSBTestBase {
    @Test
    public void testArjunaTransactionImpleWrapper() throws MBeanException {
        // We need to set the transaction type as a thread local to work around an issue in BasicAction constructor
        // calling overridable methods before the object is fully constructed
        UidWrapper.setRecordWrapperTypeName(ArjunaTransactionImpleWrapper.typeName());
        assertBeanWasCreated(new ArjunaTransactionImpleWrapper(new Uid()));
    }
    @Test
    public void testAssumedCompleteHeuristicServerTransaction() throws MBeanException {
        assertBeanWasCreated(new AssumedCompleteHeuristicServerTransaction(new Uid()));
    }
    @Test
    public void testAssumedCompleteHeuristicTransaction() throws MBeanException {
        assertBeanWasCreated(new AssumedCompleteHeuristicTransaction(new Uid()));
    }
    @Test
    public void testAssumedCompleteServerTransaction() throws MBeanException {
        assertBeanWasCreated(new AssumedCompleteServerTransaction(new Uid()));
    }
    @Test
    public void testAssumedCompleteTransaction() throws MBeanException {
        assertBeanWasCreated(new AssumedCompleteTransaction(new Uid()));
    }
    @Test
    public void testRecoveredServerTransaction() throws MBeanException {
        assertBeanWasCreated(new RecoveredServerTransaction(new Uid()));
    }
    @Test
    public void testRecoveredTransaction() throws MBeanException {
        assertBeanWasCreated(new RecoveredTransaction(new Uid()));
    }
    @Test
    public void testServerTransaction() throws MBeanException {
        assertBeanWasCreated(new RecoveringServerTransaction(new Uid()));
    }

    private static class RecoveringServerTransaction extends com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction {
        protected RecoveringServerTransaction(Uid recoveringActUid) {
            super(recoveringActUid);
        }
    }
}