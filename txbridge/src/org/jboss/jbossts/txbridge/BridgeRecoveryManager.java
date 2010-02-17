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
package org.jboss.jbossts.txbridge;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import org.apache.log4j.Logger;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;
import com.arjuna.wst.Durable2PCParticipant;

import javax.resource.spi.XATerminator;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.ObjectInputStream;

/**
 * Integrates with JBossAS MC lifecycle to provide recovery services.
 *
 * @author jonathan.halliday@redhat.com, 2009-02-10
 */
public class BridgeRecoveryManager implements XTSATRecoveryModule, RecoveryModule
{
    private static Logger log = Logger.getLogger(BridgeRecoveryManager.class);

    private final XTSATRecoveryManager xtsATRecoveryManager = XTSATRecoveryManager.getRecoveryManager();
    private final RecoveryManager acRecoveryManager = RecoveryManager.manager();
    private final XATerminator xaTerminator = SubordinationManager.getXATerminator();

    /**
     * MC lifecycle callback, used to register the recovery module with the transaction manager.
     */
    public void start()
    {
        log.info("BridgeRecoveryManager starting");

        xtsATRecoveryManager.registerRecoveryModule(this);
        acRecoveryManager.addModule(this);
    }

    /**
     * MC lifecycle callback, used to unregister the recovery module from the transaction manager.
     */
    public void stop()
    {
        log.info("BridgeRecoveryManager stopping");

        xtsATRecoveryManager.unregisterRecoveryModule(this);
        acRecoveryManager.removeModule(this, false);
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

        if(id.startsWith(BridgeDurableParticipant.TYPE_IDENTIFIER))
        {
            Object participant = objectInputStream.readObject();
            return (BridgeDurableParticipant)participant;
        }
        else
        {
            return null;
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

        if(!xtsATRecoveryManager.isParticipantRecoveryStarted()) {
            // can't do anything until XTS Participant recovery has run.
            return;
        }

        Xid[] xids = null;
        try {
            xids = xaTerminator.recover(XAResource.TMSTARTRSCAN);
            xaTerminator.recover(XAResource.TMENDRSCAN);

        } catch(XAException e) {
            log.error("Problem whilst scanning for in-doubt subordinate transactions", e);
        }

        if(xids == null) {
            return;
        }

        for(Xid xid : xids) {
           log.trace("in-doubt Xid: "+xid);
        }

        // TODO: finish me

    }

    /**
     * Called by the RecoveryManager RECOVERY_BACKOFF_PERIOD seconds
     * after the completion of the first pass
     */
    @Override
    public void periodicWorkSecondPass()
    {
        log.trace("periodicWorkSecondPass()");
    }
}
