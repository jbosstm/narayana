/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.basic;

import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import org.junit.Test;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// validate that it is possible to override a drivers' implementation of XAResource#isSameRM
public class TestXAROverride {
    @Test
    public void test() throws SystemException, NotSupportedException, RollbackException, XAException {
        final XAResource xar1 = new XAResourceOverride(); // this resource returns false from isSameRM
        final XAResource xar2 = new XAResource() { // and this resource returns true from isSameRM
            @Override
            public void commit(Xid xid, boolean b) throws XAException {
            }

            @Override
            public void end(Xid xid, int i) throws XAException {
            }

            @Override
            public void forget(Xid xid) throws XAException {
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return true; // return the opposite of what the xaResourceIsSameRMClassNames property dictates
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {
            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {
            }
        };

        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceIsSameRMClassNames(
                Collections.singletonList(xar2.getClass().getName()));

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        // sanity check that both resources can be enlisted
        assertTrue(theTransaction.enlistResource(xar2));
        assertTrue(theTransaction.enlistResource(xar1));

        assertFalse(xar1.isSameRM(xar2)); // xar1 always returns false
        assertTrue(xar2.isSameRM(xar1)); // xar2 always returns true
        assertFalse(XAUtils.isSameRM(xar1, xar2)); // verify that the override works for xar1
        assertFalse(XAUtils.isSameRM(xar2, xar1)); // verify that the override works for xar2

        tm.rollback();
    }

    static class XAResourceOverride implements XAResource {
        @Override
        public void commit(Xid xid, boolean b) throws XAException {
        }

        @Override
        public void end(Xid xid, int i) throws XAException {
        }

        @Override
        public void forget(Xid xid) throws XAException {
        }

        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        @Override
        public boolean isSameRM(XAResource xaResource) throws XAException {
            return false;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return 0;
        }

        @Override
        public Xid[] recover(int i) throws XAException {
            return new Xid[0];
        }

        @Override
        public void rollback(Xid xid) throws XAException {
        }

        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return false;
        }

        @Override
        public void start(Xid xid, int i) throws XAException {
        }
    }
}
