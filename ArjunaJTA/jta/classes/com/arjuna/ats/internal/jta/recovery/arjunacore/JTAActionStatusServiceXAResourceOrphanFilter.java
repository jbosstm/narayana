/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import java.util.List;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.recovery.ActionStatusService;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

/**
 * An XAResourceOrphanFilter which vetos rollback for xids which have an in-flight transaction.
 *
 * Warning: If this is enabled and the recovery manager cannot contact the transaction manager then branches will remain locked.
 */
public class JTAActionStatusServiceXAResourceOrphanFilter implements XAResourceOrphanFilter
{
    @Override
    public Vote checkXid(Xid xid)
    {
        if (xid.getFormatId() != XATxConverter.FORMAT_ID) {
            // we only care about Xids created by the JTA
            return Vote.ABSTAIN;
        }

        XidImple theXid = new XidImple(xid);
        Uid u = theXid.getTransactionUid();

        List<String> xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();
        String nodeName = XAUtils.getXANodeName(xid);
        if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("node name of " + xid + " is " + nodeName);
        }
        if (xaRecoveryNodes == null || xaRecoveryNodes.isEmpty() || (!xaRecoveryNodes.contains(nodeName) && !xaRecoveryNodes.contains(NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES))) {
            return Vote.ABSTAIN;
        }

        String process_id = u.getHexPid();

        if (process_id.equals(LOCAL_UID.getHexPid())) {

            ActionStatusService ass = new ActionStatusService();
            int transactionStatus = ass.getTransactionStatus("", u.stringForm());

            if (transactionStatus == ActionStatus.ABORTED) {
                // Known about and completed
                return Vote.ROLLBACK;
            } else if (transactionStatus == ActionStatus.NO_ACTION) {
                // Not used by current implementation but possible in protocol
                return Vote.ABSTAIN;
            } else {
                // Local transaction in-flight
                return Vote.LEAVE_ALONE;
            }
        } else {
            // For a different JVM
            return Vote.ABSTAIN;
        }
    }

    private static final Uid LOCAL_UID = new Uid();
}