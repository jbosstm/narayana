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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.Synchronization;

public class SynchronizationUnitTest
{
    @Test
    public void testInvalid()
    {
        SynchronizationImple sync = new SynchronizationImple(null);
        
        assertTrue(sync.get_uid().notEquals(Uid.nullUid()));
        
        assertFalse(sync.beforeCompletion());
        assertFalse(sync.afterCompletion(Status.STATUS_COMMITTED));
    }
    
    @Test
    public void testValid()
    {
        SynchronizationImple sync = new SynchronizationImple(new Synchronization());
        
        assertTrue(sync.get_uid().notEquals(Uid.nullUid()));
        
        assertTrue(sync.beforeCompletion());
        assertTrue(sync.afterCompletion(Status.STATUS_COMMITTED));
        
        SynchronizationImple comp = new SynchronizationImple(new Synchronization());
        
        assertTrue(comp.compareTo(sync) != 0);
        assertTrue(sync.toString() != null);
    }

    @Test
    public void testSynchronizationFailure() throws Exception
    {
        TransactionImple tx = new TransactionImple(0);
        DummyXA res = new DummyXA(false) {
            public void rollback (Xid xid) throws XAException
            {
                super.rollback(xid);
                throw new XAException(XAException.XA_RETRY);
            }
        };
        tx.enlistResource(res);

        final String exceptionError = "intentional testing exception";
        tx.registerSynchronization(new jakarta.transaction.Synchronization() {
            @Override
            public void beforeCompletion() {
                throw new RuntimeException(exceptionError);
            }
            @Override
            public void afterCompletion(int status) {
            }
        });

        try {
            tx.commit();
        } catch (Exception e) {
            Throwable exceptionToCheck = e;
            while(exceptionToCheck != null) {
                if(exceptionToCheck.getMessage().equals(exceptionError)) return;
                exceptionToCheck = exceptionToCheck.getCause();
            }
            throw e;
        }
    }
}
