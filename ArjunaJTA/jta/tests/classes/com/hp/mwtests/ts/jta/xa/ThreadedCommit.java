package com.hp.mwtests.ts.jta.xa;


import org.junit.Test;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThreadedCommit {
    @Test
    public void test() throws Exception {
        AtomicInteger prepareCalled = new AtomicInteger(0);
        AtomicInteger commitCalled = new AtomicInteger(0);

        XAResource xaResource = new XAResource() {
            private int threadCount;

            @Override
            public synchronized void start(Xid xid, int i) throws XAException {
                threadCount++;
                if (threadCount == 1) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        fail("Could not wait");
                    }
                } else {
                    notify();
                }
            }

            @Override
            public synchronized void end(Xid xid, int i) throws XAException {
                threadCount--;
                if (threadCount == 1) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        fail("Could not wait");
                    }
                } else {
                    notify();
                }
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return xaResource == this;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                prepareCalled.incrementAndGet();
                return 0;
            }

            @Override
            public void commit(Xid xid, boolean b) throws XAException {
                commitCalled.incrementAndGet();
            }

            @Override
            public void forget(Xid xid) throws XAException {
            }

            @Override
            public int getTransactionTimeout() throws XAException {
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
        };

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        Transaction theTransaction = tm.suspend();

        Thread worker1 = new Thread(() -> {
            process(tm, theTransaction, xaResource);
        });
        Thread worker2 = new Thread(() -> {
            process(tm, theTransaction, xaResource);
        });

        worker2.start();
        worker1.start();
        worker1.join();
        worker2.join();

        tm.resume(theTransaction);
        tm.commit();

        assertTrue("Prepare was called: " + prepareCalled.get(), prepareCalled.get() == 0);
        assertTrue("Commit was called: " + commitCalled.get(), commitCalled.get() == 1);
    }

    private static void process(TransactionManager tm, Transaction theTransaction, XAResource xaResource) {
        try {
            tm.resume(theTransaction);
            tm.getTransaction().enlistResource(xaResource);
            tm.getTransaction().delistResource(xaResource, XAResource.TMSUCCESS);
        } catch (final Exception ex) {
            fail("Could not handleXAResource");
        }
    }
}
