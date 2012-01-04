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


import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;



import javax.transaction.xa.Xid;
import java.util.List;

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

        if(_xaRecoveryNodes == null || _xaRecoveryNodes.size() == 0) {
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
