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
package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSEntryBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceImple;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XARecoveryResourceMBean;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * Extension of an XAResource record for exposing the underlying XAResource which is protected
 *
 * @author Mike Musgrove
 */
/**
 * @Deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTSXAResourceRecordWrapper extends OSEntryBean implements XARecoveryResourceMBean {
    int heuristic;
    boolean committed;
    XidImple xidImple;
    OSEntryBean bean;

    private class XARecoveryResourceWrapper extends XARecoveryResourceImple {

        public XARecoveryResourceWrapper(UidWrapper wrapper) {
            super(wrapper.getUid()); // calls loadState which in turn calls restoreState
        }

        public boolean restoreState(InputObjectState os) {
            InputObjectState copy = new InputObjectState(os);
            try {
                heuristic = copy.unpackInt();
                committed = copy.unpackBoolean();
                xidImple = new XidImple(XidImple.unpack(copy));

                return super.restoreState(os);
            } catch (IOException e) {
                return false;
            }
        }
    }

    public JTSXAResourceRecordWrapper(UidWrapper wrapper) {
        super(wrapper);
        // initialise heuristic and  xidImple
        new XARecoveryResourceWrapper(wrapper);
    }

    public byte[] getGlobalTransactionId() {
        return xidImple.getGlobalTransactionId();
    }

    public byte[] getBranchQualifier() {
        return xidImple.getBranchQualifier();
    }

    public int getFormatId() {
        return xidImple.getFormatId();
    }

    public String getNodeName() {
        return XATxConverter.getNodeName(xidImple.getXID());
    }

    public int getHeuristicValue() {
        return heuristic;
    }

/*    public boolean isCommitted() {
        return committed;
    }*/
}
