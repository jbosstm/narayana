/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import javax.management.MBeanException;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * MBean implementation of a transaction participant corresponding to a JTA XAResource
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class XAResourceRecordBean extends LogRecordWrapper implements XAResourceRecordBeanMBean {
    String className = "unavailable";
    String eisProductName = "unavailable";
    String eisProductVersion = "unavailable";
    String jndiName = "unavailable";
    int timeout = 0;
    JTAXAResourceRecordWrapper xares;
    XidImple xidImple;
    int heuristic;

    public XAResourceRecordBean(UidWrapper w) {
        super(w.getUid());
        init();
        xares = new JTAXAResourceRecordWrapper(w.getUid());
        xidImple = new XidImple(new XID());
        heuristic = -1;
    }

    public XAResourceRecordBean(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
        super(parent, rec, listType);
        init();
        xares = new JTAXAResourceRecordWrapper(rec);
        xidImple = xares.xidImple;
        heuristic = xares.heuristic;
    }

    private void init() {
        jndiName = getUid().stringForm();
        className = "unavailable";
        eisProductName = "unavailable";
        eisProductVersion = "unavailable";
        timeout = 0;
    }

    public boolean activate() {
        boolean ok = super.activate();

        className = rec.getClass().getName();

        if (rec instanceof XAResourceRecord) {
            XAResourceRecord xarec = (XAResourceRecord) rec;

            eisProductName = xarec.getProductName();
            eisProductVersion = xarec.getProductVersion();
            jndiName = xarec.getJndiName();
        }

        if (rec.value() != null) {
            XAResource xar = (XAResource) rec.value();

            className = xar.getClass().getName();

            try {
                timeout = xar.getTransactionTimeout();
            } catch (Exception e) {
            }
        }

        return ok;
    }

    public String getClassName() { return className; }
    public String getEisProductName() { return eisProductName; }
    public String getEisProductVersion() { return eisProductVersion; }
    public String getJndiName() { return jndiName; }
    public int getTimeout() { return timeout; }

    @Override
    public String getHeuristicStatus() {
        return HeuristicStatus.intToStatus(xares.heuristic).name();
    }

    @Override
    public byte[] getGlobalTransactionId() {
        return xidImple.getGlobalTransactionId();
    }
    @Override
    public byte[] getBranchQualifier() {
        return xidImple.getBranchQualifier();
    }
    @Override
    public int getFormatId() {
        return xidImple.getFormatId();
    }
    @Override
    public String getNodeName() {
        return XATxConverter.getNodeName(xidImple.getXID());
    }
    @Override
    public int getHeuristicValue() {
        return heuristic;
    }

    @Override
    public boolean forget() {
        return xares.forgetHeuristic();
    }

    @Override
    public String remove() throws MBeanException {
        return super.remove(); // note the remove should invoke the forget operation
    }

    /**
     * Extension of an XAResource record for exposing the underlying XAResource which is protected
     */
    class JTAXAResourceRecordWrapper extends com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord {
        XidImple xidImple = null;
        int heuristic = -1;
        AbstractRecord rec;
        XAResourceRecord xarec;

        public JTAXAResourceRecordWrapper(Uid uid) {
            super(uid);

            xidImple = new XidImple(getXid());
        }

        public JTAXAResourceRecordWrapper(AbstractRecord rec) {
            this(rec.order());
            this.rec = rec;
            this.xarec = (XAResourceRecord) rec;
            heuristic = ((XAResourceRecord) rec).getHeuristic();
        }

        @Override
        public boolean activate() {
            return super.activate();
        }

        public boolean restore_state(InputObjectState os, int t) {
            InputObjectState copy = new InputObjectState(os);
            try {
                heuristic = copy.unpackInt();
            } catch (IOException e) {
            }

            return super.restore_state(os, t);
        }

        @Override
        public boolean forgetHeuristic() {
            if (xarec != null) {
                xarec.forgetHeuristic();
                return xarec.isForgotten();
            }

            return super.forgetHeuristic();
        }
    }

}
