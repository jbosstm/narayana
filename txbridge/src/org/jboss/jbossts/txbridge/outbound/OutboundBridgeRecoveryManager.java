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
package org.jboss.jbossts.txbridge.outbound;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import org.apache.log4j.Logger;
import org.jboss.jbossts.xts.bridge.at.BridgeWrapper;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Integrates with JBossAS MC lifecycle and JBossTS recovery manager to provide
 * recovery services for outbound bridged transactions.
 *
 * @author jonathan.halliday@redhat.com, 2010-03-05
 */
public class OutboundBridgeRecoveryManager implements RecoveryModule
{
    private static final Logger log = Logger.getLogger(OutboundBridgeRecoveryManager.class);

    private final RecoveryManager acRecoveryManager = RecoveryManager.manager();

    private volatile boolean orphanedBridgeWrappersAreIdentifiable = false;

    /**
     * MC lifecycle callback, used to register components with the recovery manager.
     */
    public void start()
    {
        log.info("OutboundBridgeRecoveryManager starting");

        acRecoveryManager.addModule(this);
    }

    /**
     * MC lifecycle callback, used to unregister components from the recovery manager.
     */
    public void stop()
    {
        log.info("OutboundBridgeRecoveryManager stopping");

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
        log.trace("periodicWorkFirstPass()");
    }

    /**
     * Called by the RecoveryManager RECOVERY_BACKOFF_PERIOD seconds
     * after the completion of the first pass
     */
    @Override
    public void periodicWorkSecondPass()
    {
        log.trace("periodicWorkSecondPass()");

        BridgeXAResource.cleanupRecoveredXAResources();

        // the JTA top level tx recovery module is registered and hence run before us. Therefore by the time
        // we get here we know readObject has been called for any BridgeXAResource for which a log exists.
        // thus if it's not in our xaResourcesAwaitingRecovery list by now, it's presumed rollback.
        orphanedBridgeWrappersAreIdentifiable = true;

        BridgeWrapper[] bridgeWrappers = BridgeWrapper.scan(OutboundBridgeManager.BRIDGEWRAPPER_PREFIX);

        for(BridgeWrapper bridgeWrapper : bridgeWrappers) {
            if( !BridgeXAResource.isAwaitingRecovery(bridgeWrapper.getIdentifier()) ) {
                log.info("rolling back orphaned subordinate BridgeWrapper "+bridgeWrapper.getIdentifier());
                bridgeWrapper.rollback();
            }
        }
    }
}
