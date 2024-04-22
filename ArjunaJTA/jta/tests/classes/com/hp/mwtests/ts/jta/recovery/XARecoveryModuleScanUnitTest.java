/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.XAResourceWrapper;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jta.xa.XidImple;

public class XARecoveryModuleScanUnitTest
{

    /*
     * XAResource.TMENDRSCAN should be invoked during periodicWorkFirstPass
     *
     * This guarantees that no idle connections are left after periodicWorkFirstPass
     */
    @Test
    public void testEndRecoveryScanIsCalled() throws Exception {
        int orphanSafetyInterval = jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval();
        List<String> xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(0);
        jtaPropertyManager.getJTAEnvironmentBean()
                .setXaRecoveryNodes(Arrays.asList(new String[] { NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES }));
        XARecoveryModule xarm = new XARecoveryModule();
        xarm.addXAResourceOrphanFilter(new JTANodeNameXAResourceOrphanFilter());
        XaResourceWrapperImpl xaResImpl = new XaResourceWrapperImpl();
        XAResourceRecoveryHelper xaRRH = new XAResourceRecoveryHelper() {

            XAResource[] xares = new XAResource[] { xaResImpl };
            @Override
            public boolean initialise(String p) throws Exception {
                return false;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                return xares;
            }
        };
        xarm.addXAResourceRecoveryHelper(xaRRH);
        assertEquals(XAResource.TMNOFLAGS, xaResImpl.getTmScanStatus());
        xarm.periodicWorkFirstPass();
        assertEquals(XAResource.TMENDRSCAN, xaResImpl.getTmScanStatus());
        xarm.periodicWorkSecondPass();
        assertEquals(XAResource.TMENDRSCAN, xaResImpl.getTmScanStatus());
        jtaPropertyManager.getJTAEnvironmentBean().setOrphanSafetyInterval(orphanSafetyInterval);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodes);
    }

    class XaResourceWrapperImpl implements XAResourceWrapper {

        @Override
        public XAResource getResource() {
            return null;
        }

        @Override
        public String getProductName() {
            return null;
        }

        @Override
        public String getProductVersion() {
            return null;
        }

        @Override
        public String getJndiName() {
            return "test";
        }
        Xid xid = new XidImple(new Uid());
        int tmScanStatus = 0;
        boolean rolledback = false;

        public int getTmScanStatus() {
            return tmScanStatus;
        }

        public void setTmScanStatus(int tmScanStatus) {
            this.tmScanStatus = tmScanStatus;
        }

        @Override
        public void commit(Xid xid, boolean b) throws XAException {
        }

        @Override
        public void end(Xid xid, int i) throws XAException {
        }

        @Override
        public void forget(Xid xid) throws XAException {
        }

        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        @Override
        public boolean isSameRM(XAResource xaResource) throws XAException {
            return false;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return 0;
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            setTmScanStatus(i);
            return new Xid[] { xid };
        }

        @Override
        public void rollback(Xid xid) throws XAException {
            rolledback = true;
        }
        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return false;
        }

        @Override
        public void start(Xid xid, int i) throws XAException {
        }
    }
}