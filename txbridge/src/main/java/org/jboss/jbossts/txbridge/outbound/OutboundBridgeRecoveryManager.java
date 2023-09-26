/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.outbound;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import org.jboss.jbossts.txbridge.utils.txbridgeLogger;
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

/**
 * Integrates with JBossAS MC lifecycle and JBossTS recovery manager to provide
 * recovery services for outbound bridged transactions.
 *
 * @author jonathan.halliday@redhat.com, 2010-03-05
 */
public class OutboundBridgeRecoveryManager implements RecoveryModule
{
    private final RecoveryManager acRecoveryManager = RecoveryManager.manager();

    private volatile boolean orphanedBridgeWrappersAreIdentifiable = false;

    /**
     * MC lifecycle callback, used to register components with the recovery manager.
     */
    public void start()
    {
        txbridgeLogger.i18NLogger.info_obrm_start();

        acRecoveryManager.addModule(this);
    }

    /**
     * MC lifecycle callback, used to unregister components from the recovery manager.
     */
    public void stop()
    {
        txbridgeLogger.i18NLogger.info_obrm_stop();

        acRecoveryManager.removeModule(this, false);
    }

    /**
     * Called by the RecoveryManager at start up, and then
     * PERIODIC_RECOVERY_PERIOD seconds after the completion, for all RecoveryModules,
     * of the second pass
     */
    @Override
    public void periodicWorkFirstPass()
    {
        txbridgeLogger.logger.trace("OutboundBridgeRecoveryManager.periodicWorkFirstPass()");
    }

    /**
     * Called by the RecoveryManager RECOVERY_BACKOFF_PERIOD seconds
     * after the completion of the first pass
     */
    @Override
    public void periodicWorkSecondPass()
    {
        txbridgeLogger.logger.trace("OutboundBridgeRecoveryManager.periodicWorkSecondPass()");

        BridgeXAResource.cleanupRecoveredXAResources();

        // the JTA top level tx recovery module is registered and hence run before us. Therefore by the time
        // we get here we know readObject has been called for any BridgeXAResource for which a log exists.
        // thus if it's not in our xaResourcesAwaitingRecovery list by now, it's presumed rollback.
        orphanedBridgeWrappersAreIdentifiable = true;

        BridgeWrapper[] bridgeWrappers = BridgeWrapper.scan(OutboundBridgeManager.BRIDGEWRAPPER_PREFIX);

        for(BridgeWrapper bridgeWrapper : bridgeWrappers) {
            if( !BridgeXAResource.isAwaitingRecovery(bridgeWrapper.getIdentifier()) ) {
                txbridgeLogger.logger.trace("rolling back orphaned subordinate BridgeWrapper "+bridgeWrapper.getIdentifier());
                bridgeWrapper.rollback();
            }
        }
    }
}