/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.inbound.utility;

import org.jboss.logging.Logger;

import jakarta.transaction.Synchronization;

/**
 * Implementation of Synchronization for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class TestSynchronization implements Synchronization {
    private static Logger log = Logger.getLogger(TestSynchronization.class);

    public void beforeCompletion() {
        log.trace("TestSynchronization.beforeCompletion()");
    }

    public void afterCompletion(int i) {
        log.trace("TestSynchronization.afterCompletion(" + i + ")");
    }
}