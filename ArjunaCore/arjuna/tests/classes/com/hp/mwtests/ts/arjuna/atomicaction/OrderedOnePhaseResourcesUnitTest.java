/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.atomicaction;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.hp.mwtests.ts.arjuna.abstractrecords.OrderedOnePhaseAbstractRecord;

public class OrderedOnePhaseResourcesUnitTest
{
    @Test
    public void testOrderedOnePhase() throws Exception
    {
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "jbossts-properties.xml");

        Uid firstToCommit = new Uid();
        Uid secondToCommit = new Uid();
        AtomicAction A = new AtomicAction();

        // These user defined records could wrap anything such as 1PC JDBC or messages
        OrderedOnePhaseAbstractRecord rec1 = new OrderedOnePhaseAbstractRecord(secondToCommit);
        OrderedOnePhaseAbstractRecord rec2 = new OrderedOnePhaseAbstractRecord(firstToCommit);

        A.begin();

        // Doesn't matter of the order
        A.add(rec1);
        A.add(rec2);

        // Do some work you could save some concept of the work in the abstract record save_state
        // rec1.sendMessage
        // rec2.doSQL

        // This is just so we can see the opportunity for failure recovery
        rec1.causeTransientFailure();

        // Commit, this would make sure the database (rec2) was committed first
        A.commit();

        // This shows recovery working, we should see Uid2 called again, if you had encoded some information in rec2 you could
        // maybe
        // retry the sending of a message or similar
        RecordTypeManager.manager().add(new RecordTypeMap() {

            @Override
            public int getType() {
                return OrderedOnePhaseAbstractRecord.typeId();
            }

            @Override
            public Class<? extends AbstractRecord> getRecordClass() {
                return OrderedOnePhaseAbstractRecord.class;
            }
        });
        recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
        RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT).addModule(new AtomicActionRecoveryModule());
        RecoveryManager.manager().scan();
    }
}
