/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss Inc.
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

/*
 * @author Mark Little (mark@arjuna.com)
 * @since 3.0.
 */

/*
 * If the caller doesn't want to be informed of heuristics during completion
 * then it's possible the application (or admin) may still want to be informed.
 * So special participants can be registered with the transaction which are
 * triggered during the Synchronization phase and given the true outcome of
 * the transaction. We do not dictate a specific implementation for what these
 * participants do with the information (e.g., OTS allows for the CORBA Notification Service
 * to be used).
 */

public abstract class HeuristicNotification implements SynchronizationRecord
{
    public abstract void heuristicOutcome (int actionStatus);
    
    public Uid get_uid ()
    {
        return _uid;
    }

    public boolean beforeCompletion ()
    {
        return true;
    }

    public boolean afterCompletion (int status)
    {
        return true;
    }
    
    private Uid _uid = new Uid();
}