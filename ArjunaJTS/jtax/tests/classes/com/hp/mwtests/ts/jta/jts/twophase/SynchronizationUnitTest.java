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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.twophase;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jta.resources.jts.LocalCleanupSynchronization;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

import static org.junit.Assert.*;

public class SynchronizationUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        TransactionImple tx = new TransactionImple();
        LocalCleanupSynchronization sync = new LocalCleanupSynchronization(tx);
        
        assertTrue(sync.beforeCompletion());
        assertTrue(sync.afterCompletion(ActionStatus.COMMITTED));
        
        assertTrue(sync.get_uid().notEquals(Uid.nullUid()));
        assertTrue(sync.compareTo(new LocalCleanupSynchronization(null)) != 0);
    }
}
