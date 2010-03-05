/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * (C) 2009 @author Red Hat Middleware LLC
 */
package org.jboss.jbossts.txbridge.inbound;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import org.apache.log4j.Logger;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;
import com.arjuna.wst.Durable2PCParticipant;

import javax.resource.spi.XATerminator;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * Integrates with JBossAS MC lifecycle and JBossTS recovery manager to provide
 * recovery services for inbound bridged transactions.
 *
 * @author jonathan.halliday@redhat.com, 2009-02-10
 */
public class InboundBridgeRecoveryManager implements XTSATRecoveryModule, RecoveryModule, XAResourceOrphanFilter
{
    private static final Logger log = Logger.getLogger(InboundBridgeRecoveryManager.class);

    private final XTSATRecoveryManager xtsATRecoveryManager = XTSATRecoveryManager.getRecoveryManager();
    private final RecoveryManager acRecoveryManager = RecoveryManager.manager();
    private final XATerminator xaTerminator = SubordinationManager.getXATerminator();

    private final List<BridgeDurableParticipant> participantsAwaitingRecovery =
            Collections.synchronizedList(new LinkedList<BridgeDurableParticipant>());
    private volatile boolean orphanedXAResourcesAreIdentifiable = false;

    /**
     * MC lifecycle callback, used to register components with the recovery manager.
     */
    public void start()
    {
        log.info("InboundBridgeRecoveryManager starting");

        xtsATRecoveryManager.registerRecoveryModule(this);
        acRecoveryManager.addModule(this);

        XARecoveryModule xaRecoveryModule = getXARecoveryModule();
        xaRecoveryModule.addXAResourceOrphanFilter(this);
    }

    /**
     * MC lifecycle callback, used to unregister components from the recovery manager.
     */
    public void stop()
    {
        log.info("InboundBridgeRecoveryManager stopping");

        xtsATRecoveryManager.unregisterRecoveryModule(this);
        acRecoveryManager.removeModule(this, false);
        
        XARecoveryModule xaRecoveryModule = getXARecoveryModule();
        xaRecoveryModule.removeXAResourceOrphanFilter(this);
    }

    /**
     * Lookup the XARecoveryModule, required for (de-)registration of XAResourceOrphanFilter.
     * @return the RecoveryManager's XARecoveryModule instance.
     */
    private XARecoveryModule getXARecoveryModule()
    {
        // at some stage we should probably consider extending atsintegration's
        // RecoveryManagerService (and maybe the app server's tm integration spi) to
        // expose orphan filters directly, as with e.g. [add|remove]XAResourceRecovery.

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector<RecoveryModule>)acRecoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            throw new IllegalStateException("no XARecoveryModule found");
        }
        return xaRecoveryModule;
    }

    /**
     * Called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * participant was originally saved using serialization.
     *
     * @param id the id used when the participant was created
     * @param objectInputStream a stream from which the application should deserialize the participant
     * if it recognises that the id belongs to the module's application
     * @return the deserialized Participant object
     * @throws Exception if an error occurs deserializing the durable participant
     */
    @Override
    public Durable2PCParticipant deserialize(String id, ObjectInputStream objectInputStream) throws Exception
    {
        log.trace("deserialize(id="+id+")");

        // Inbound bridge transactions don't have an independent log - their state is inlined into the
        // XTS Participant log and this callback is used to recover that state.
        // We keep a handle on it for later use, as we have no other means of determining which Xids
        // represent uncompleted transactions.
        if(id.startsWith(BridgeDurableParticipant.TYPE_IDENTIFIER))
        {
            Object participant = objectInputStream.readObject();
            BridgeDurableParticipant bridgeDurableParticipant = (BridgeDurableParticipant)participant;
            participantsAwaitingRecovery.add(bridgeDurableParticipant);
            return bridgeDurableParticipant;
        }
        else
        {
            return null; // it belongs to some other XTS app, ignore it.
        }
    }

    /**
     * Unused recovery callback. We use serialization instead, so this method will always throw an exception if called.
     */
    @Override
    public Durable2PCParticipant recreate(String s, byte[] bytes) throws Exception
    {
        throw new Exception("recreation not supported - should use deserialization instead.");
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

        cleanupRecoveredParticipants();

        // the XTS recovery module is registered and hence run before us. Therefore by the time we get here
        // we know deserialize has been called for any BridgeDurableParticipant for which a log exists.
        // thus if it's not in our participantsAwaitingRecovery list by now, it's presumed rollback.
        orphanedXAResourcesAreIdentifiable = true;

        // Inbound tx may have a JCA tx log but no corresponding XTS Participant (i.e. BridgeDurableParticipant) log.
        // these can now be identified and rolled back.
        List<Xid> indoubtSubordinates = getIndoubtSubordinates();
        for(Xid xid : indoubtSubordinates) {
            if(checkXid(xid) == XAResourceOrphanFilter.Vote.ROLLBACK) {
                log.trace("rolling back orphaned subordinate tx "+xid);
                try {
                    xaTerminator.rollback(xid);
                } catch(XAException e) {
                    log.error("problem rolling back orphaned subordinate tx "+xid, e);
                }
            }
        }

    }

    /**
     * Run a recovery scan to identify any in-doubt JTA subordinates.
     *
     * @return a possibly empty but non-null list of xids corresponding to outstanding
     * JTA subordinate transactions owned by the txbridge.
     */
    private List<Xid> getIndoubtSubordinates()
    {
        log.trace("getIndoubtSubordinates()");

        Xid[] allSubordinateXids = null;
        try {
            allSubordinateXids = xaTerminator.recover(XAResource.TMSTARTRSCAN);
        } catch(XAException e) {
            log.error("Problem whilst scanning for in-doubt subordinate transactions", e);
        } finally {
            try {
                xaTerminator.recover(XAResource.TMENDRSCAN);
            } catch(XAException e) {}
        }

        LinkedList<Xid> mySubordinateXids = new LinkedList<Xid>();

        if(allSubordinateXids == null) {
            return mySubordinateXids;
        }

        for(Xid xid : allSubordinateXids) {
            if(xid.getFormatId() == BridgeDurableParticipant.XARESOURCE_FORMAT_ID) {
                mySubordinateXids.add(xid);
                log.trace("in-doubt subordinate, xid: "+xid);
            }
        }

        return mySubordinateXids;
    }

    /**
     * Release any BridgeDurableParticipant instances that have been driven
     * through to completion by their parent XTS transaction.
     */
    private void cleanupRecoveredParticipants()
    {
        log.trace("cleanupRecoveredParticipants()");

        synchronized(participantsAwaitingRecovery) {
            Iterator<org.jboss.jbossts.txbridge.inbound.BridgeDurableParticipant> iter = participantsAwaitingRecovery.iterator();
            while(iter.hasNext()) {
                BridgeDurableParticipant participant = iter.next();
                if(!participant.isAwaitingRecovery()) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Used to identify inbound bridged Xids in either the RM log (when called by XARecoveryModule) or
     * the JCA subordinate tx log (when called internally from this class) which have or have not got a
     * remaining transaction that may still drive them to completion.
     * 
     * @param xid The in-doubt xid.
     * @return a Vote on the handling of the xid (to roll it back or not).
     */
    @Override
    public Vote checkXid(Xid xid)
    {
        log.trace("checkXid("+xid+")");

        if(xid.getFormatId() != BridgeDurableParticipant.XARESOURCE_FORMAT_ID) {
            return Vote.ABSTAIN; // it's not one of ours, ignore it.
        }

        if(!orphanedXAResourcesAreIdentifiable) {
            // recovery system not in stable state yet - we don't yet know if it's orphaned or not.
            return Vote.LEAVE_ALONE;
        }

        // check if it's owned by a recovered tx that may still commit.
        synchronized(participantsAwaitingRecovery) {
            for(BridgeDurableParticipant participant : participantsAwaitingRecovery) {
                if(participant.getXid().equals(xid)) {
                    return Vote.LEAVE_ALONE;
                }
            }
        }

        // presumed abort:
        return Vote.ROLLBACK;
    }
}
