/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.utility;

import com.arjuna.wst.Durable2PCParticipant;
import org.jboss.logging.Logger;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryModule;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.io.ObjectInputStream;

/**
 * Implementation of XTSATRecoveryModule for deserializing TestDurableParticipant instances in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 */
@Singleton
@Startup
public class TestRecoveryModule implements XTSATRecoveryModule {
    private static final Logger log = Logger.getLogger(TestRecoveryModule.class);

    /**
     * MC lifecycle callback, used to register components with the recovery manager.
     */
    @PostConstruct
    public void postConstruct() {
        log.info("TestRecoveryModule starting");

        XTSATRecoveryManager.getRecoveryManager().registerRecoveryModule(this);
    }

    /**
     * MC lifecycle callback, used to unregister components from the recovery manager.
     */
    @PreDestroy
    public void preDestroy() {
        log.info("TestRecoveryModule stopping");

        XTSATRecoveryManager.getRecoveryManager().unregisterRecoveryModule(this);
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * participant was originally saved using serialization.
     *
     * @param id                the id used when the participant was created
     * @param objectInputStream a stream from which the application should deserialise the participant
     *                          if it recognises that the id belongs to the module's application
     * @return the participant
     * @throws Exception if an error occurs deserializing the durable participant
     */
    @Override
    public Durable2PCParticipant deserialize(String id, ObjectInputStream objectInputStream) throws Exception {
        log.trace("deserialize(id=" + id + ")");

        if (id.startsWith(TestDurableParticipant.TYPE_IDENTIFIER)) {
            Object participant = objectInputStream.readObject();
            TestDurableParticipant testDurableParticipant = (TestDurableParticipant) participant;
            return testDurableParticipant;
        } else {
            return null; // it belongs to some other XTS app, ignore it.
        }
    }

    /**
     * Unused recovery callback. We use serialization instead, so this method will always throw an exception if called.
     */
    @Override
    public Durable2PCParticipant recreate(String id, byte[] recoveryState) throws Exception {
        throw new Exception("recreation not supported - should use deserialization instead.");
    }

    /**
     * participant recovery modules may need to perform special processing when the a recovery scan has
     * completed. in particular it is only after the first recovery scan has completed they can identify
     * whether locally prepared changes are accompanied by a recreated participant and roll back changes
     * for those with no such participant.
     */
    @Override
    public void endScan() {
        // unused
    }
}