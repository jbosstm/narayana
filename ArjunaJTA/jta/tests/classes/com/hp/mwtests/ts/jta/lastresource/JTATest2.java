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
 */

package com.hp.mwtests.ts.jta.lastresource;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;
import org.junit.Test;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JTATest2 {
    /**
     * Test the following scenario:
     * 2 XA resources, one makes no changes during prepare (ie returns XA_RDONLY),
     * and the other is a LRCO resource (so is processed after normal XA resources) which
     * throws XAException.XA_RBROLLBACK during prepare.
     *
     * The expected outcome is that the transaction throws a RollbackException. Furthermore this exception should
     * contain a suppressed throwable corresponding to the XAException thrown by the LRCO resource.
     * @throws Exception
     */
    @Test
    public void test_RBROLLBACK_OnePhase() throws Exception {
        doTest(XAException.XA_RBROLLBACK);
    }

    private void doTest(final int errorCode) throws IllegalStateException, RollbackException, SystemException, NotSupportedException, SecurityException, HeuristicMixedException,
            HeuristicRollbackException {

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new LastResourceCommitOptimisation() {

            @Override
            public void start(Xid arg0, int arg1) throws XAException {}

            @Override
            public boolean setTransactionTimeout(int arg0) throws XAException {
                return false;
            }

            @Override
            public void rollback(Xid arg0) throws XAException {}

            @Override
            public Xid[] recover(int arg0) throws XAException {
                return null;
            }

            @Override
            public int prepare(Xid arg0) throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource arg0) throws XAException {
                return false;
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public void forget(Xid arg0) throws XAException {

            }

            @Override
            public void end(Xid arg0, int arg1) throws XAException {}

            @Override
            public void commit(Xid arg0, boolean arg1) throws XAException {
                throw new XAException(errorCode);
            }
        }));
        assertTrue(theTransaction.enlistResource(new XAResource() {

            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {

            }

            @Override
            public void end(Xid xid, int flags) throws XAException {

            }

            @Override
            public void forget(Xid xid) throws XAException {

            }

            @Override
            public int getTransactionTimeout() throws XAException {

                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xares) throws XAException {

                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {

                return XA_RDONLY;
            }

            @Override
            public Xid[] recover(int flag) throws XAException {

                return null;
            }

            @Override
            public void rollback(Xid xid) throws XAException {
            }

            @Override
            public boolean setTransactionTimeout(int seconds) throws XAException {

                return false;
            }

            @Override
            public void start(Xid xid, int flags) throws XAException {

            }
        }));

        try {
            tm.commit();
            fail("Commit should have thrown a rollback exception");
        } catch (RollbackException re) {
            // check that the exception contains the XAException from the XA resource that rolled back
            assertTrue("Expected a deferred exception", re.getSuppressed().length > 0);

            Throwable t = re.getSuppressed()[0];

            assertTrue("Expected a deferred XAException", t instanceof XAException);

            assertEquals("Expected a deferred rollback exception",
                    XAException.XA_RBROLLBACK, ((XAException)t).errorCode);
        }
    }
}
