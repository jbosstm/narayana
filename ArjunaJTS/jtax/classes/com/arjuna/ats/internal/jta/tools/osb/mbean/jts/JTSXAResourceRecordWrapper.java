/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSEntryBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceImple;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * Extension of an XAResource record for exposing the underlying XAResource which is protected
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTSXAResourceRecordWrapper extends OSEntryBean implements JTSXAResourceRecordWrapperMBean {
    private final XARecoveryResourceWrapper record;
    int heuristic;
    boolean committed;
    boolean rollback;
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
                rollback = copy.unpackBoolean();
                xidImple = new XidImple(XidImple.unpack(copy));

                return super.restoreState(os);
            } catch (IOException e) {
                return false;
            }
        }

        public void updateHeuristic(int h) {
            heuristic = h;
            updateState(heuristic);
        }
    }

    public JTSXAResourceRecordWrapper(UidWrapper wrapper) {
        super(wrapper);
        // initialise heuristic and  xidImple
        record = new XARecoveryResourceWrapper(wrapper);
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

    public void clearHeuristic() {
        heuristic = 0;
        record.updateHeuristic(heuristic);
    }

/*    public boolean isCommitted() {
        return committed;
    }*/
}