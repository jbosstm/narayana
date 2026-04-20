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
package org.jboss.jbossts.txbridge.tests.outbound.utility;

import com.arjuna.wst.Durable2PCParticipant;
import org.jboss.logging.Logger;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryModule;

import java.io.ObjectInputStream;

/**
 * Implementation of XTSATRecoveryModule for deserializing TestDurableParticipant instances in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 */
public class TestRecoveryModule implements XTSATRecoveryModule
{
    private static final Logger log = Logger.getLogger(TestRecoveryModule.class);

    private final XTSATRecoveryManager xtsATRecoveryManager = XTSATRecoveryManager.getRecoveryManager();

    /**
     * MC lifecycle callback, used to register components with the recovery manager.
     */
    public void start()
    {
        log.info("TestRecoveryModule starting");

        xtsATRecoveryManager.registerRecoveryModule(this);
    }

    /**
     * MC lifecycle callback, used to unregister components from the recovery manager.
     */
    public void stop()
    {
        log.info("TestRecoveryModule stopping");

        xtsATRecoveryManager.unregisterRecoveryModule(this);
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * participant was originally saved using serialization.
     *
     * @param id     the id used when the participant was created
     * @param objectInputStream a stream from which the application should deserialise the participant
     *               if it recognises that the id belongs to the module's application
     * @return
     * @throws Exception if an error occurs deserializing the durable participant
     */
    @Override
    public Durable2PCParticipant deserialize(String id, ObjectInputStream objectInputStream) throws Exception
    {
        log.trace("deserialize(id="+id+")");

        if(id.startsWith(TestDurableParticipant.TYPE_IDENTIFIER))
        {
            Object participant = objectInputStream.readObject();
            TestDurableParticipant testDurableParticipant = (TestDurableParticipant)participant;
            return testDurableParticipant;
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
    public Durable2PCParticipant recreate(String id, byte[] recoveryState) throws Exception
    {
        throw new Exception("recreation not supported - should use deserialization instead.");
    }

    /**
     * participant recovery modules may need to perform special processing when the a recovery scan has
     * completed. in particular it is only after the first recovery scan has completed they can identify
     * whether locally prepared changes are accompanied by a recreated participant and roll back changes
     * for those with no such participant.
     */
    @Override
    public void endScan()
    {
        // unused
    }
}
