/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package com.hp.mwtests.ts.jts.recovery;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransaction;

/**
 * @author <a href="gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
public final class RecoveredServerTransactionUnitTest {

    private RecoveredServerTransaction recoveredServerTransaction;
    
    @Before
    public void before() {
        recoveredServerTransaction = new RecoveredServerTransaction(new Uid());
    }
    
    @Test
    @BMRule(name = "Return TwoPhaseOutcome.PREPARE_OK from BasicAction.getHeuristicDecision",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
        targetMethod = "getHeuristicDecision",
        action = "return com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome.PREPARE_OK",
        targetLocation = "AT ENTRY")
    public void testAssumeCompleteNoHeuristic() {
        Assert.assertTrue(recoveredServerTransaction.assumeComplete());
        Assert.assertEquals(AssumedCompleteServerTransaction.typeName(), recoveredServerTransaction.type());
    }
    
    @Test
    @BMRule(name = "Return TwoPhaseOutcome.HEURISTIC_COMMIT from BasicAction.getHeuristicDecision",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
        targetMethod = "getHeuristicDecision",
        action = "return com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome.HEURISTIC_COMMIT",
        targetLocation = "AT ENTRY")
    public void testAssumeCompleteHeuristicCommit() {
        Assert.assertTrue(recoveredServerTransaction.assumeComplete());
        Assert.assertEquals(AssumedCompleteHeuristicServerTransaction.typeName(), recoveredServerTransaction.type());
    }
    
    @Test
    @BMRule(name = "Return TwoPhaseOutcome.HEURISTIC_ROLLBACK from BasicAction.getHeuristicDecision",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
        targetMethod = "getHeuristicDecision",
        action = "return com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome.HEURISTIC_ROLLBACK",
        targetLocation = "AT ENTRY")
    public void testAssumeCompleteHeuristicRollback() {
        Assert.assertTrue(recoveredServerTransaction.assumeComplete());
        Assert.assertEquals(AssumedCompleteHeuristicServerTransaction.typeName(), recoveredServerTransaction.type());
    }
    
    @Test
    @BMRule(name = "Return TwoPhaseOutcome.HEURISTIC_MIXED from BasicAction.getHeuristicDecision",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
        targetMethod = "getHeuristicDecision",
        action = "return com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome.HEURISTIC_MIXED",
        targetLocation = "AT ENTRY")
    public void testAssumeCompleteHeuristicMixed() {
        Assert.assertTrue(recoveredServerTransaction.assumeComplete());
        Assert.assertEquals(AssumedCompleteHeuristicServerTransaction.typeName(), recoveredServerTransaction.type());
    }
    
    @Test
    @BMRule(name = "Return TwoPhaseOutcome.HEURISTIC_HAZARD from BasicAction.getHeuristicDecision",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
        targetMethod = "getHeuristicDecision",
        action = "return com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome.HEURISTIC_HAZARD",
        targetLocation = "AT ENTRY")
    public void testAssumeCompleteHeuristicHazard() {
        Assert.assertTrue(recoveredServerTransaction.assumeComplete());
        Assert.assertEquals(AssumedCompleteHeuristicServerTransaction.typeName(), recoveredServerTransaction.type());
    }

}
