/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import com.hp.mwtests.ts.arjuna.resources.TestBase;
import org.junit.Test;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class AtomicActionAsyncTest extends AtomicActionTestBase
{
    // NOTE: The following bean properties can only be set once (because TxControl takes a static copy of them)
    @BeforeClass
    public static void init() {
        AtomicActionTestBase.init(true);
    }

    @Test
    @Override
    public void testCommit() throws Exception {
        super.testCommit();
    }

    @Test
    @Override
    public void testAbort() throws Exception {
        super.testAbort();
    }

    @Test
    public void testCompletionWithFailuresAsyncAfterCompletion() throws Exception {
        AtomicActionTestBase.init(false);
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncAfterSynchronization(true);
        super.testCompletionWithFailures();
        AtomicActionTestBase.init(true);
    }

    @Test
    @Override
    public void testPrepareWithLRRSuccess() {
        super.testPrepareWithLRRSuccess();
    }

    @Test
    @Override
    public void testPrepareWithLRRFailOn2PCAwareResourcePrepare() {
        super.testPrepareWithLRRFailOn2PCAwareResourcePrepare();
    }

    @Test
    @Override
    public void testPrepareWithLRRFailOn2PCUnawareResourcePrepare() {
        super.testPrepareWithLRRFailOn2PCUnawareResourcePrepare();
    }

    @Test
    @Override
    public void testPrepareWithLRRFailOn2PCAwareResourceCommit() {
        super.testPrepareWithLRRFailOn2PCAwareResourceCommit();
    }

    @Test
    @Override
    public void testCompletionWithoutFailures() throws Exception {
        super.testCompletionWithoutFailures();
    }

    @Test
    @Override
    public void testCompletionWithFailures() throws Exception {
        super.testCompletionWithFailures();
    }

    @Test
    @Override
    public void testCompletionWithException() throws Exception {
        super.testCompletionWithException();
    }

    @Test
    public void testHeuristicNotification() throws Exception {
        // NOTE: cannot test for heuristics with asynchronous commit
        // - setting reportHeuristics during commit will override the asynchronous commit config setting
        super.testHeuristicNotification(true);
    }

    @Test
    @Override
    public void testRegistrationDuringCompletion() throws Exception {
        super.testRegistrationDuringCompletion();
    }

    @Test
    @Override
    public void testRegistrationDuringCompletion2() throws Exception {
        super.testRegistrationDuringCompletion2();
    }

    @Test
    @Override
    public void testRegistrationDuringCompletion2b() throws Exception {
        super.testRegistrationDuringCompletion2b();
    }

    @Test
    @Override
    public void testRegistrationDuringCompletion3() throws Exception {
        super.testRegistrationDuringCompletion3();
    }

    @Test
    @Override
    public void testRegistrationDuringCompletion4() throws Exception {
        super.testRegistrationDuringCompletion4();
    }

    @Test
    @Override
    public void testRegistrationDuringCompletion4b() throws Exception {
        super.testRegistrationDuringCompletion4b();
    }
}