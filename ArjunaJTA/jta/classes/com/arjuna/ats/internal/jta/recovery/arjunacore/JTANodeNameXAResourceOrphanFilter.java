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