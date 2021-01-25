/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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
