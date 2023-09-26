/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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