/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.xa.XATxConverter;

/**
 * An XAResourceOrphanFilter for JTA top level transactions, which uses node name information
 * encoded in the xid to determine if they should be rolled back or not.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public class JTANodeNameXAResourceOrphanFilter implements XAResourceOrphanFilter
{
    private final XAResourceOrphanFilter nodeNameFilter = new NodeNameXAResourceOrphanFilter();

    protected final int myFormatId = XATxConverter.FORMAT_ID;

    @Override
    public Vote checkXid(Xid xid)
    {
        if(xid.getFormatId() != myFormatId) {
            return Vote.ABSTAIN;
        }

        return nodeNameFilter.checkXid(xid);
    }
}