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
 * (C) 2016,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.jta.recovery.arjunacore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.ActionStatusService;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.List;

/**
 * An XAResourceOrphanFilter which vetos rollback for xids which have an in-flight transaction.
 *
 * Warning: If this is enabled and the recovery manager cannot contact the transaction manager then branches will remain locked.
 */
public class SubordinationManagerXAResourceOrphanFilter implements XAResourceOrphanFilter
{
    @Override
    public Vote checkXid(Xid xid)
    {
        if (xid.getFormatId() != XATxConverter.FORMAT_ID) {
            // we only care about Xids created by the JTA
            return Vote.ABSTAIN;
        }

        if (!TxControl.getXANodeName().equals(XATxConverter.getSubordinateNodeName(new XidImple(xid).getXID()))) {
            // It either doesn't have a subordinate node name or isn't for this server
            return Vote.ABSTAIN;
        }

        XidImple theXid = new XidImple(xid);
        SubordinateTransaction importedTransaction = null;
        try {
            importedTransaction = SubordinationManager.getTransactionImporter().getImportedTransaction(theXid);
        } catch (XAException e) {
            return Vote.LEAVE_ALONE;
        }
        if (importedTransaction != null) {
            return Vote.LEAVE_ALONE;
        } else {
            return Vote.ROLLBACK;
        }
    }
}
