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
package com.hp.mwtests.ts.jta.basic;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;

import javax.transaction.TransactionSynchronizationRegistry;

import com.hp.mwtests.ts.jta.common.Synchronization;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Exercise the TransactionSynchronizationRegistry implementation.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
public class TransactionSynchronizationRegistryTest
{
    @Test
    public void testTSR() throws Exception {

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        TransactionSynchronizationRegistry tsr = new TransactionSynchronizationRegistryImple();

        assertNull(tsr.getTransactionKey());

        assertEquals(tm.getStatus(), tsr.getTransactionStatus());

        tm.begin();

        assertNotNull(tsr.getTransactionKey());
        assertEquals(tm.getStatus(), tsr.getTransactionStatus());

        String key = "key";
        Object value = new Object();
        assertNull(tsr.getResource(key));
        tsr.putResource(key, value);
        assertEquals(value, tsr.getResource(key));

        Synchronization synchronization = new Synchronization();
        tsr.registerInterposedSynchronization(synchronization);

        assertFalse(tsr.getRollbackOnly());
        tsr.setRollbackOnly();
        assertTrue(tsr.getRollbackOnly());

        boolean gotExpectedException = false;
        try {
            tsr.registerInterposedSynchronization(synchronization);
        } catch(IllegalStateException e) {
            gotExpectedException = true;
        }
        assertTrue(gotExpectedException);

        tm.rollback();

        assertEquals(tm.getStatus(), tsr.getTransactionStatus());
        
    }
}
