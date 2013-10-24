package com.arjuna.ats.jta.cdi;

import com.arjuna.ats.jta.logging.jtaLogger;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TransactionScopeCleanup<T> implements Synchronization {
    private Set<TransactionScopedBean> beans;
    private TransactionContext context;
    private Transaction transaction;

    public TransactionScopeCleanup(TransactionContext context, Transaction transaction) {
        this.context = context;
        this.transaction = transaction;
        this.beans = new CopyOnWriteArraySet<TransactionScopedBean>();

        try {
            transaction.registerSynchronization(this);
        } catch (RollbackException e) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_syncwhenaborted());
        } catch (SystemException e) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_transaction_arjunacore_nullparam());
        }
    }

    public void registerBean(Contextual<T> contextual, CreationalContext<T> creationalContext, T bean) {
        beans.add(new TransactionScopedBean(contextual, creationalContext, bean));
    }

    @Override
    public void beforeCompletion() {
    }

    @Override
    public void afterCompletion(int i) {
        for (TransactionScopedBean bean : beans)
            bean.destroy();

        context.cleanupScope(transaction);
    }

    private class TransactionScopedBean<T> {
        Contextual<T> contextual;
        CreationalContext<T> creationalContext;
        T bean;

        private TransactionScopedBean(Contextual<T> contextual, CreationalContext<T> creationalContext, T bean) {
            this.contextual = contextual;
            this.creationalContext = creationalContext;
            this.bean = bean;
        }

        public <T> void destroy() {
            contextual.destroy(bean, creationalContext);
        }
    }


}
