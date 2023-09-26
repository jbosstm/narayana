/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import org.jboss.jbossts.txbridge.utils.txbridgeLogger;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.Transaction;

/**
 * Manages Thread association of the interposed coordinator.
 * Typically called from handlers in the WS stack.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class InboundBridge
{
    /**
     * Identifier for the subordinate transaction.
     */
    private final Xid xid;
    private final int timeout;

    /**
     * Create a new InboundBridge to manage the given subordinate JTA transaction.
     *
     * @param xid the subordinate transaction id
     * @throws XAException
     * @throws SystemException
     */
    InboundBridge(Xid xid, int timeout) throws XAException, SystemException
    {
        txbridgeLogger.logger.trace("InboundBridge.<ctor>(Xid="+xid+")");

        this.xid = xid;
        this.timeout = timeout;

        getTransaction(); // ensures transaction is initialized
    }

    public Xid getXid() {
        return xid;
    }

    /**
     * Associate the JTA transaction to the current Thread.
     * Typically used by a server side inbound handler.
     *
     * @throws XAException
     * @throws SystemException
     * @throws InvalidTransactionException
     */
    public void start() throws XAException, SystemException, InvalidTransactionException
    {
        txbridgeLogger.logger.trace("InboundBridge.start(Xid="+xid+")");

        Transaction tx = getTransaction();

        TransactionManager.transactionManager().resume(tx);
    }

    /**
     * Disassociate the JTA transaction from the current Thread.
     * Typically used by a server side outbound handler.
     *
     * @throws XAException
     * @throws SystemException
     * @throws InvalidTransactionException
     */
    public void stop() throws XAException, SystemException, InvalidTransactionException
    {
        txbridgeLogger.logger.trace("InboundBridge.stop("+xid+")");

        TransactionManager.transactionManager().suspend();
    }

    public void setRollbackOnly() throws XAException, SystemException
    {
        txbridgeLogger.logger.trace("InboundBridge.setRollbackOnly("+xid+")");

        getTransaction().setRollbackOnly();
    }

    /**
     * Get the JTA Transaction which corresponds to the Xid of the instance.
     *
     * @return
     * @throws XAException
     * @throws SystemException
     */
    private Transaction getTransaction()
            throws XAException, SystemException
    {
        int importingTimeout = timeout > 0 ? timeout : TxControl.getDefaultTimeout();
        Transaction tx = SubordinationManager.getTransactionImporter()
                .importTransaction(xid, importingTimeout);

        switch (tx.getStatus())
        {
            // TODO: other cases?

            case Status.STATUS_ACTIVE:
            case Status.STATUS_MARKED_ROLLBACK:
                break;
            default:
                throw new IllegalStateException("Transaction not in state ACTIVE");
        }
        return tx;
    }
}