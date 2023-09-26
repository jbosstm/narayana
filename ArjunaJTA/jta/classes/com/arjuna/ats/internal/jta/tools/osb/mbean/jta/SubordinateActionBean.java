/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;

/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class SubordinateActionBean extends JTAActionBean implements SubordinateActionBeanMBean {
    public SubordinateActionBean(UidWrapper w) {
        super(w);
    }

    public String getXid() {
        try {
            SubordinateAtomicAction sub = (SubordinateAtomicAction) ra.getAction();

            if (sub.getXid() == null) {
                return "Error: The objectstore record could not be activated";
            }
            return sub.getXid().toString();
        } catch (ClassCastException e) {
            if (tsLogger.logger.isDebugEnabled()) {
                BasicAction ba = ra.getAction();

    		    tsLogger.logger.debug("unable to cast " + ba.toString() + e.getMessage());
            }

            return e.getMessage();
        }
    }

    public String getParentNodeName() {
        try {
            SubordinateAtomicAction sub = (SubordinateAtomicAction) ra.getAction();

            return sub.getParentNodeName();
        } catch (ClassCastException e) {
            if (tsLogger.logger.isDebugEnabled()) {
                BasicAction ba = ra.getAction();

    		    tsLogger.logger.debug("unable to cast " + (ba == null ? "null" : ba.toString()) + ": " + e.getMessage());
            }

            return e.getMessage();
        }
    }
}