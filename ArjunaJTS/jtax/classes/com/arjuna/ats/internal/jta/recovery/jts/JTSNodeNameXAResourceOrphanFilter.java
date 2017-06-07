/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.ats.internal.jta.recovery.jts;

import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.recovery.arjunacore.NodeNameXAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;

/**
 * An XAResourceOrphanFilter for JTS transactions, which uses node name information
 * encoded in the xid to determine if they should be rolled back or not.
 */
public class JTSNodeNameXAResourceOrphanFilter implements XAResourceOrphanFilter
{
    private final XAResourceOrphanFilter nodeNameFilter = new NodeNameXAResourceOrphanFilter();

    @Override
    public Vote checkXid(Xid xid)
    {
        if(xid.getFormatId() != com.arjuna.ats.jts.extensions.Arjuna.XID()) {
            return Vote.ABSTAIN;
        }

        return nodeNameFilter.checkXid(xid);
    }
}
