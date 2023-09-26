/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.txbridge.tests.common;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.byteman.contrib.dtest.Instrumentor;

/**
 * Interface used within crash recovery tests for Byteman rule
 * being defined at the start of the application server.
 */
public interface OnServerRebootInstrumentator {
    /**
     * Instrumentation to be done on server reboot.
     * For details see @link {@link AbstractCrashRecoveryTests#rebootServer(ContainerController)}
     *
     * Note: Before each server restart it is necessary to store the instrumentation into a file and then start up the server
     * with that file as a parameter for Byteman to ensure the appropriate classes are instrumented once the server is up.
     */
    void instrument(Instrumentor instrumentor) throws Exception;
}