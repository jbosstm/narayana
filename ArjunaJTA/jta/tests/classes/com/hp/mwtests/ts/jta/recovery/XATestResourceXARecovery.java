/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import java.sql.SQLException;

/**
 * XAResourceRecovery that returns unrecoverable and recoverable resources
 */
public class XATestResourceXARecovery implements XAResourceRecovery {
    private static final int MAX_COUNT = 2;

    private static boolean useFaultyResources;

    private int count = 0;

    public static void setUseFaultyResources(boolean useFaultyResources) {
        XATestResourceXARecovery.useFaultyResources = useFaultyResources;
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        count++;

        if (count == 1 && useFaultyResources)
            return new XATestResource(XATestResource.FAULTY_JNDI_NAME, true);

        return new XATestResource(XATestResource.OK_JNDI_NAME, false);
    }

    @Override
    public boolean initialise(String p) throws SQLException {
        return true;
    }

    @Override
    public boolean hasMoreResources() {
        if (count < MAX_COUNT)
            return true;

        count = 0;

        return false;
    }
}
