/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.outbound;

import org.jboss.jbossts.txbridge.utils.txbridgeLogger;
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

import jakarta.transaction.Synchronization;
import jakarta.transaction.Status;

import com.arjuna.ats.jta.utils.JTAHelper;

/**
 * Provides method call mapping between JTA parent coordinator and WS-AT subordinate transaction.
 *
 * @author jonathan.halliday@redhat.com, 2009-06-01
 */
public class BridgeSynchronization implements Synchronization
{
    private final BridgeWrapper bridgeWrapper;

    public BridgeSynchronization(BridgeWrapper bridgeWrapper)
    {
        txbridgeLogger.logger.trace("BridgeSynchronization.<ctor>(BridgeWrapper="+bridgeWrapper+")");

        this.bridgeWrapper = bridgeWrapper;
    }

    /**
     * The beforeCompletion method is called by the transaction manager prior to the start of the two-phase transaction commit process.
     */
    public void beforeCompletion()
    {
        txbridgeLogger.logger.trace("BridgeSynchronization.beforeCompletion()");

        if(!bridgeWrapper.prepareVolatile())
        {
            // JTA does not explicitly provide for beforeCompletion signalling problems, but in
            // our impl the engine will set the tx rollbackOnly if beforeCompletion throw an exception
            // Note com.arjuna.ats.jta.TransactionManager.getTransaction().setRollbackOnly may also work.
            txbridgeLogger.i18NLogger.error_bridge_wrapper_prepare_volatile(bridgeWrapper);
            throw new RuntimeException("BridgeWrapper.prepareVolatile() returned false");
        }
    }

    /**
     * This method is called by the transaction manager after the transaction is committed or rolled back.
     *
     * @param status the jakarta.transaction.Status representing the tx outcome.
     */
    public void afterCompletion(int status)
    {
        txbridgeLogger.logger.trace("BridgeSynchronization.afterCompletion(status="+status+"/"+ JTAHelper.stringForm(status)+")");

        switch(status)
        {
            case Status.STATUS_COMMITTED:
                bridgeWrapper.commitVolatile();
                break;
            case Status.STATUS_ROLLEDBACK:
                bridgeWrapper.rollbackVolatile();
                break;
            default:
                txbridgeLogger.i18NLogger.warn_obs_unexpectedstatus(Integer.toString(status));
                bridgeWrapper.rollbackVolatile();
        }
    }
}