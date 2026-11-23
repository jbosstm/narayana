/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
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
