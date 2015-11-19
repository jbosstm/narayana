/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
