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
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.jta.common.jtaPropertyManager;

public class JTATest {

    private XAException exception;
    protected boolean resource1Rollback;
    protected boolean resource2Rollback;
    
    @Test
    public void testRMFAILcommit1PC() throws Exception
    {
        XAResource theResource = new XAResource() {

            @Override
            public void start(Xid xid, int flags) throws XAException {
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {
                throw new XAException(XAException.XAER_RMFAIL);
            }

            @Override
            public void rollback(Xid xid) throws XAException {
            }

            @Override
            public void forget(Xid xid) throws XAException {
            }

            @Override
            public Xid[] recover(int flag) throws XAException {
                return null;
            }

            @Override
            public boolean isSameRM(XAResource xaRes) throws XAException {
                return false;
            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean setTransactionTimeout(int seconds) throws XAException {
                return false;
            }
        };

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(theResource));

        try {
            tm.commit();
            fail();
        } catch (javax.transaction.HeuristicMixedException e) {
            // Expected
        }
    }

	@Test
	public void test() throws Exception {

		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		javax.transaction.Transaction theTransaction = tm.getTransaction();

		assertTrue(theTransaction.enlistResource(new XARMERRXAResource(false)));
		XARMERRXAResource rollbackCalled = new XARMERRXAResource(true);
		assertTrue(theTransaction.enlistResource(rollbackCalled));

		tm.rollback();
		assertTrue(rollbackCalled.getRollbackCalled());
	}
	
    /**
     * This is none-spec behaviour that some resource managers perform where they throw a RTE instead of return an XAException
     * This test verifies that RTE will result in rollback in Narayana
     *  
     * @throws SecurityException
     * @throws IllegalStateException
     * @throws HeuristicMixedException
     * @throws HeuristicRollbackException
     * @throws SystemException
     * @throws NotSupportedException
     * @throws RollbackException
     */
	@Test
    public void testRollbackRTE() throws SecurityException, IllegalStateException, HeuristicMixedException, HeuristicRollbackException, SystemException, NotSupportedException, RollbackException {

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
            .transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {
            @Override
            public int prepare(Xid xid) throws XAException {
                throw new RuntimeException();
            }

            @Override
            public void rollback(Xid xid) throws XAException {
                resource1Rollback = true;
            }
        }));
        
        assertTrue(theTransaction.enlistResource(new SimpleXAResource() {

            @Override
            public void rollback(Xid xid) throws XAException {
                resource2Rollback = true;
            }
        }));

        try {
            tm.commit();
            fail("Should not have committed");
        } catch (RollbackException e) {
            assertTrue(resource1Rollback);
            assertTrue(resource2Rollback);
        }
    }
	
	
	@Test
	public void testHeuristicRollbackSuppressedException() throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException {

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        tm.begin();

        javax.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue(theTransaction.enlistResource(new XAResource() {

            @Override
            public void start(Xid xid, int flags) throws XAException {

                
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {

                
            }

            @Override
            public int prepare(Xid xid) throws XAException {

                return 0;
            }

            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {
                exception = new XAException(XAException.XA_HEURRB);
                throw exception;
            }

            @Override
            public void rollback(Xid xid) throws XAException {

                
            }

            @Override
            public void forget(Xid xid) throws XAException {

                
            }

            @Override
            public Xid[] recover(int flag) throws XAException {

                return null;
            }

            @Override
            public boolean isSameRM(XAResource xaRes) throws XAException {

                return false;
            }

            @Override
            public int getTransactionTimeout() throws XAException {

                return 0;
            }

            @Override
            public boolean setTransactionTimeout(int seconds) throws XAException {

                return false;
            }}));
        assertTrue(theTransaction.enlistResource(new XAResource() {

            @Override
            public void start(Xid xid, int flags) throws XAException {

                
            }

            @Override
            public void end(Xid xid, int flags) throws XAException {

                
            }

            @Override
            public int prepare(Xid xid) throws XAException {

                return 0;
            }

            @Override
            public void commit(Xid xid, boolean onePhase) throws XAException {

                
            }

            @Override
            public void rollback(Xid xid) throws XAException {

                
            }

            @Override
            public void forget(Xid xid) throws XAException {

                
            }

            @Override
            public Xid[] recover(int flag) throws XAException {

                return null;
            }

            @Override
            public boolean isSameRM(XAResource xaRes) throws XAException {

                return false;
            }

            @Override
            public int getTransactionTimeout() throws XAException {

                return 0;
            }

            @Override
            public boolean setTransactionTimeout(int seconds) throws XAException {

                return false;
            }}));

        try {
            tm.commit();
            fail();
        } catch (RollbackException e) {
            e.printStackTrace();
            assertTrue(e.getSuppressed()[0] == exception);
        }
	    
	}

	private class XARMERRXAResource implements XAResource {

		private boolean returnRMERROutOfEnd;
		private boolean rollbackCalled;

		public XARMERRXAResource(boolean returnRMERROutOfEnd) {
			this.returnRMERROutOfEnd = returnRMERROutOfEnd;
		}

		public boolean getRollbackCalled() {
			return rollbackCalled;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			if (returnRMERROutOfEnd) {
				throw new XAException(XAException.XAER_RMERR);
			}
		}

		@Override
		public void forget(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getTransactionTimeout() throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isSameRM(XAResource xares) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int prepare(Xid xid) throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			rollbackCalled = true;
		}

		@Override
		public boolean setTransactionTimeout(int seconds) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
			// TODO Auto-generated method stub

		}

	}
	
	private abstract class SimpleXAResource implements XAResource {

        @Override
        public void start(Xid xid, int flags) throws XAException {

        }

        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {

            return false;
        }

        @Override
        public void rollback(Xid xid) throws XAException {
        }

        @Override
        public Xid[] recover(int flag) throws XAException {

            return null;
        }

        @Override
        public int prepare(Xid xid) throws XAException {
            return XAResource.XA_OK;
        }

        @Override
        public boolean isSameRM(XAResource xares) throws XAException {

            return false;
        }

        @Override
        public int getTransactionTimeout() throws XAException {

            return 0;
        }

        @Override
        public void forget(Xid xid) throws XAException {

        }

        @Override
        public void end(Xid xid, int flags) throws XAException {

        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {

        }
    }
}
