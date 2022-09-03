package com.hp.mwtests.ts.jta.timeout;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import org.junit.Assert;
import org.junit.Test;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
public class TestSimultaneousRollbackWithSynchronizationRegistry {
    @Test
    public void test(){
        TransactionManager tm = new TransactionManagerImple();
        int i = 0;
        final AtomicInteger x = new AtomicInteger(0);
        final AtomicInteger y = new AtomicInteger(0);

        while(i++ < 10) {
            try {
                tm.setTransactionTimeout(1);
                tm.begin();
                Transaction tx = tm.getTransaction();
                while (tx != null && tx.getStatus() == Status.STATUS_ACTIVE) {
                    try {
                        tx.registerSynchronization(new Synchronization() {
                            @Override
                            public void beforeCompletion() {
                            }

                            @Override
                            public void afterCompletion(int i) {
                                y.getAndIncrement();
                            }
                        });
                        x.getAndIncrement();
                    } catch (Exception e) {
                        // Reaper aborted TX so can't rollback
                    }
                }
                tm.commit();
            } catch (Exception e) {
                Assert.assertEquals(x.get(), y.get());
                x.set(0);
                y.set(0);
            }
        }

    }
}
