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

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper;
import com.arjuna.ats.internal.jts.recovery.transactions.*;

import org.junit.Test;

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
    public void testArjunaTransactionImpleWrapper() {
        assertBeanWasCreated(new ArjunaTransactionImpleWrapper(new Uid()));
    }
    @Test
    public void testAssumedCompleteHeuristicServerTransaction() {
        assertBeanWasCreated(new AssumedCompleteHeuristicServerTransaction(new Uid()));
    }
    @Test
    public void testAssumedCompleteHeuristicTransaction() {
        assertBeanWasCreated(new AssumedCompleteHeuristicTransaction(new Uid()));
    }
    @Test
    public void testAssumedCompleteServerTransaction() {
        assertBeanWasCreated(new AssumedCompleteServerTransaction(new Uid()));
    }
    @Test
    public void testAssumedCompleteTransaction() {
        assertBeanWasCreated(new AssumedCompleteTransaction(new Uid()));
    }
    @Test
    public void testRecoveredServerTransaction() {
        assertBeanWasCreated(new RecoveredServerTransaction(new Uid()));
    }
    @Test
    public void testRecoveredTransaction() {
        assertBeanWasCreated(new RecoveredTransaction(new Uid()));
    }
    @Test
    public void testServerTransaction() {
        RecoveringServerTransaction txn = new RecoveringServerTransaction(new Uid()); //  , null);
        assertBeanWasCreated(txn);
    }

    //"CosTransactions/XAResourceRecord",

    private static class RecoveringServerTransaction extends com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction {
        protected RecoveringServerTransaction(Uid recoveringActUid) {
            super(recoveringActUid);
        }
    }
}
