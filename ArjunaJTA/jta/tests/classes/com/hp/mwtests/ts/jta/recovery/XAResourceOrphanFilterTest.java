/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter;
import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XATxConverter;

import javax.transaction.xa.Xid;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

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
}
