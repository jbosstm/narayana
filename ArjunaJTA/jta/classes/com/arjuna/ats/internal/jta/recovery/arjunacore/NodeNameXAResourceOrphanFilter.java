/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;


import java.util.List;

import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;

/**
 * An XAResourceOrphanFilter which uses node name information encoded in the xid to determine if
 * they should be rolled back or not.
 *
 * Note that this filter does not check xid format id, and therefore may attempt to extract node name
 * information from foreign xids, resulting in random behaviour. Probably best combined with a filter
 * that verifies formatIds.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class NodeNameXAResourceOrphanFilter implements XAResourceOrphanFilter
{
    public static final String RECOVER_ALL_NODES = "*";

    @Override
    public Vote checkXid(Xid xid)
    {
        List<String> _xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();

        if(_xaRecoveryNodes == null || _xaRecoveryNodes.isEmpty()) {
            doWarning();
            return Vote.ABSTAIN;
        }

        if ((_xaRecoveryNodes.contains(RECOVER_ALL_NODES)))
        {
            if (jtaLogger.logger.isDebugEnabled()) {
                jtaLogger.logger.debug("Ignoring node name. Will recover " + xid);
            }

            return Vote.ROLLBACK;
        }

        String nodeName = XAUtils.getXANodeName(xid);

        if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("node name of " + xid + " is " + nodeName);
        }

        if (_xaRecoveryNodes.contains(nodeName))
        {
            return Vote.ROLLBACK;
        }
        else
        {
            return Vote.ABSTAIN;
        }
    }

    private void doWarning() {
        jtaLogger.i18NLogger.info_recovery_noxanodes();
    }
}