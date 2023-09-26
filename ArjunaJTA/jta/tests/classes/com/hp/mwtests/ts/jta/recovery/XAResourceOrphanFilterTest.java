/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinateJTAXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinationManagerXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.commitmarkable.SimpleXAResource;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XATxConverter;

/**
 * Unit tests for JTA package XAResourceOrphanFilter implementations.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class XAResourceOrphanFilterTest
{
    @Test
    public void testJTANodeNameXAResourceOrphanFilter()
    {
        XAResourceOrphanFilter orphanFilter = new JTANodeNameXAResourceOrphanFilter();

        Xid notJTAFormatId = XATxConverter.getXid(new Uid(), false, 0);
        assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(notJTAFormatId));

        List<String> recoveryNodes = new LinkedList<String>();
        recoveryNodes.add("1");
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(recoveryNodes);

        String notRecoverableNodeName ="2";
        TxControl.setXANodeName(notRecoverableNodeName);
        Xid jtaNotRecoverableNodeName = XATxConverter.getXid(new Uid(), false, XATxConverter.FORMAT_ID);

        assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(jtaNotRecoverableNodeName));

        String recoverableNodeName ="1";
        TxControl.setXANodeName(recoverableNodeName);
        Xid jtaRecoverableNodeName = XATxConverter.getXid(new Uid(), false, XATxConverter.FORMAT_ID);

        assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(jtaRecoverableNodeName));

        recoveryNodes.clear();
        recoveryNodes.add(NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES);
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(recoveryNodes);

        assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(jtaNotRecoverableNodeName));
        assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(jtaRecoverableNodeName));
    }

    @Test
    public void testJTATransactionLogOrphanFilter()
    {
        XAResourceOrphanFilter orphanFilter = new JTATransactionLogXAResourceOrphanFilter();

        Xid notJTAFormatId = XATxConverter.getXid(new Uid(), false, 0);
        assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(notJTAFormatId));

        Xid jtaFormatId = XATxConverter.getXid(new Uid(), false, XATxConverter.FORMAT_ID);
        assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(jtaFormatId));
    }

    @Test
    public void testJTAActionStatusServiceXAResourceOrphanFilter() {
        XAResourceOrphanFilter orphanFilter = new JTAActionStatusServiceXAResourceOrphanFilter();

        List<String> xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        List<String> recoveryNodes = new LinkedList<String>();
        recoveryNodes.add("1");
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(recoveryNodes);
        try {
            Uid uid = new Uid();

            Xid xid = XATxConverter.getXid(uid, false, XATxConverter.FORMAT_ID);
            assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(xid));

            TwoPhaseCoordinator tpc = new TwoPhaseCoordinator(uid);
            try {
                tpc.start();
                assertEquals(XAResourceOrphanFilter.Vote.LEAVE_ALONE, orphanFilter.checkXid(xid));
            } finally {
                tpc.cancel();
            }
            assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(xid));
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(null);
            TwoPhaseCoordinator tpc2 = new TwoPhaseCoordinator(uid);
            tpc2.start();
            assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(xid));
            tpc2.cancel();
        } finally {
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodes);
        }
    }

    @Test
    public void testJTAActionStatusServiceXAResourceOrphanFilterSubordinate() throws HeuristicRollbackException, HeuristicMixedException, HeuristicCommitException, SystemException, RollbackException, XAException {
        XAResourceOrphanFilter orphanFilter = new SubordinationManagerXAResourceOrphanFilter();

        List<String> xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        List<String> recoveryNodes = new LinkedList<String>();
        recoveryNodes.add("1");
        jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(recoveryNodes);
        final List<String> recoveryExtensions = new ArrayList<String>();

        recoveryExtensions.add(com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinateAtomicActionRecoveryModule.class.getName());
        recoveryExtensions.add(com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule.class.getName());
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(recoveryExtensions);
        int recoveryBackoffPeriod = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod();
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        RecoveryManager.manager().scan();
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(recoveryBackoffPeriod);
        try {
            Xid xid = XATxConverter.getXid(Uid.nullUid(), false, XATxConverter.FORMAT_ID);
            assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(xid));
            SubordinateTransaction subordinateTransaction = SubordinationManager.getTransactionImporter().importTransaction(xid);
            final List<Xid> xids = new ArrayList<Xid>();
            XAResource xar = new SimpleXAResource() {
                @Override
                public void start (Xid xid, int flags) throws XAException {
                    super.start(xid, flags);
                    xids.add(xid);
                }
            };
            subordinateTransaction.enlistResource(xar);
            try {
                assertEquals(XAResourceOrphanFilter.Vote.LEAVE_ALONE, orphanFilter.checkXid(xids.get(0)));
            } finally {
                subordinateTransaction.doRollback();
            }
            assertEquals(XAResourceOrphanFilter.Vote.LEAVE_ALONE, orphanFilter.checkXid(xids.get(0)));
            SubordinationManager.getTransactionImporter().removeImportedTransaction(xid);
            assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(xids.get(0)));
        } finally {
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodes);
            recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(null);
        }
    }

    @Test
    public void testSubordinateJTAXAResourceOrphanFilter() throws HeuristicRollbackException, HeuristicMixedException, HeuristicCommitException, SystemException, RollbackException, XAException {
        XAResourceOrphanFilter orphanFilter = new SubordinateJTAXAResourceOrphanFilter();
        XidImple xid = (XidImple) XATxConverter.getXid(Uid.nullUid(), false, XATxConverter.FORMAT_ID);
        XATxConverter.setSubordinateNodeName(xid.getXID(), TxControl.getXANodeName());
        List<String> xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        try {
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Arrays.asList("2"));
            assertEquals(XAResourceOrphanFilter.Vote.ABSTAIN, orphanFilter.checkXid(xid));
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(Arrays.asList("1"));
            assertEquals(XAResourceOrphanFilter.Vote.ROLLBACK, orphanFilter.checkXid(xid));
        } finally {
            jtaPropertyManager.getJTAEnvironmentBean().setXaRecoveryNodes(xaRecoveryNodes);
        }
    }
}