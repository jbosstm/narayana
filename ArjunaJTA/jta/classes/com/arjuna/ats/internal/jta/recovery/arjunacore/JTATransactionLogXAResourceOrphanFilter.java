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
package com.arjuna.ats.internal.jta.recovery.arjunacore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;



import javax.transaction.xa.Xid;

/**
 * An XAResourceOrphanFilter which vetos rollback for xids owned by top level JTA transactions.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class JTATransactionLogXAResourceOrphanFilter implements XAResourceOrphanFilter
{
    @Override
    public Vote checkXid(Xid xid)
    {
        if(xid.getFormatId() != XATxConverter.FORMAT_ID) {
            // we only care about Xids created by the JTA
            return Vote.ABSTAIN;
        }

        if(transactionLog(xid)) {
            // it's owned by a logged transaction which
            // will recover it top down in due course
            return Vote.LEAVE_ALONE;
        }

        return Vote.ABSTAIN;
    }

    /**
	 * Is there a log file for this transaction?
	 *
	 * @param xid the transaction to check.
	 *
	 * @return <code>boolean</code>true if there is a log file,
	 *         <code>false</code> if there isn't.
	 */
    private boolean transactionLog(Xid xid)
    {
        ObjectStore transactionStore = TxControl.getStore();
        String transactionType = new AtomicAction().type();

        XidImple theXid = new XidImple(xid);
        Uid u = theXid.getTransactionUid();

        if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("Checking whether Xid "
                    + theXid + " exists in ObjectStore.");
        }

        if (!u.equals(Uid.nullUid()))
        {
            try
            {

                if (jtaLogger.logger.isDebugEnabled()) {
                    jtaLogger.logger.debug("Looking for " + u + " and " + transactionType);
                }

                if (transactionStore.currentState(u, transactionType) != StateStatus.OS_UNKNOWN)
                {
                    if (jtaLogger.logger.isDebugEnabled()) {
                        jtaLogger.logger.debug("Found record for " + theXid);
                    }

                    return true;
                }
                else
                {
                    if (jtaLogger.logger.isDebugEnabled()) {
                        jtaLogger.logger.debug("No record found for " + theXid);
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            jtaLogger.i18NLogger.info_recovery_notaxid(XAHelper.xidToString(xid));
        }

        return false;
    }
}
