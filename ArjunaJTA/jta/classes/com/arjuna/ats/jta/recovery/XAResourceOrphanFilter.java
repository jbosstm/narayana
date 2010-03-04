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
package com.arjuna.ats.jta.recovery;

import javax.transaction.xa.Xid;

/**
 * Interface used by the XARecoveryModule to allow plugins to vote in the handling of in-doubt Xids.
 *
 *  @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public interface XAResourceOrphanFilter
{
    public enum Vote { ABSTAIN, ROLLBACK, LEAVE_ALONE }

    /**
     * Called by the XARecoveryModule for each in-doubt Xid.
     * Implementations should return
     *   Vote.ROLLBACK if they recognize the xid and believe it should be aborted.
     *   Vote.LEAVE_ALONE if they recognize the xid and do not want the XARecovery module to roll it back.
     *   Vote.ABSTAIN if they do not recognize the xid.
     * Each registered XAResourceOrphanFilter will be consulted before any rollback on each recovery pass,
     * so they may change their mind over time e.g. if new information becomes available due to other recovery
     * activity.
     *
     * @param xid The in-doubt xid.
     * @return a Vote in accordance with the guidelines above.
     */
    public Vote checkXid(Xid xid);
}