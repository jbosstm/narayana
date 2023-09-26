/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jbossatx.jta;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.tm.XAResourceRecovery;

import javax.transaction.xa.XAResource;

/**
 * Simple adaptor class that converts the JBossAS transaction integration SPI
 * version of the recovery interface (org.jboss.tm.XAResourceRecovery) into
 * the ArjunaJTA version (com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper)
 * so it can then be registered with the Recovery system (XARecoveryModule)
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class XAResourceRecoveryHelperWrapper implements XAResourceRecoveryHelper
{
    private XAResourceRecovery xaResourceRecovery;

    public XAResourceRecoveryHelperWrapper(XAResourceRecovery xaResourceRecovery) {
        this.xaResourceRecovery = xaResourceRecovery;
    }

    public boolean initialise(String p) throws Exception
    {
        return true;
    }

    public XAResource[] getXAResources() throws Exception
    {
        return xaResourceRecovery.getXAResources();
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XAResourceRecoveryHelperWrapper that = (XAResourceRecoveryHelperWrapper) o;

        if (!xaResourceRecovery.equals(that.xaResourceRecovery)) return false;

        return true;
    }

    public int hashCode()
    {
        return xaResourceRecovery.hashCode();
    }
}