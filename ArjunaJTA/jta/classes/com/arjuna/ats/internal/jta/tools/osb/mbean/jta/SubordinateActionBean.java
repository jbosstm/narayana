package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;

public class SubordinateActionBean extends JTAActionBean implements SubordinateActionBeanMBean {
    public SubordinateActionBean(UidWrapper w) {
        super(w);
    }

    public String getXid() {
        try {
            SubordinateAtomicAction sub = (SubordinateAtomicAction) ra.getAction();

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
