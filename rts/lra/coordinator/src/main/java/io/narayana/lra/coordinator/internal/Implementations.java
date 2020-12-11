/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
 */
package io.narayana.lra.coordinator.internal;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import io.narayana.lra.coordinator.domain.model.LRAChildAbstractRecord;
import io.narayana.lra.coordinator.domain.model.LRAParentAbstractRecord;
import io.narayana.lra.coordinator.domain.model.LRAParticipantRecord;

public class Implementations {
    private static boolean added;
    private static RecordTypeMap participantRecordTypeMap;
    private static RecordTypeMap parentAbstractRecordTypeMap;
    private static RecordTypeMap childAbstractRecord;

    public static synchronized void install() {
        if (!added) {
            participantRecordTypeMap = new RecordTypeMap() {
                @Override
                public Class<? extends AbstractRecord> getRecordClass() {
                    return LRAParticipantRecord.class;
                }

                @Override
                public int getType() {
                    return RecordType.LRA_RECORD;
                }
            };

            parentAbstractRecordTypeMap = new RecordTypeMap() {
                @Override
                public Class<? extends AbstractRecord> getRecordClass() {
                    return LRAParentAbstractRecord.class;
                }

                @Override
                public int getType() {
                    return RecordType.LRA_PARENT_RECORD;
                }
            };

            childAbstractRecord = new RecordTypeMap() {
                @Override
                public Class<? extends AbstractRecord> getRecordClass() {
                    return LRAChildAbstractRecord.class;
                }

                @Override
                public int getType() {
                    return RecordType.LRA_CHILD_RECORD;
                }
            };

            RecordTypeManager.manager().add(participantRecordTypeMap);
            RecordTypeManager.manager().add(parentAbstractRecordTypeMap);
            RecordTypeManager.manager().add(childAbstractRecord);
            added = true;
        }
    }

    public static synchronized void uninstall() {
        if (added) {
            RecordTypeManager.manager().remove(participantRecordTypeMap);
            RecordTypeManager.manager().remove(parentAbstractRecordTypeMap);
            RecordTypeManager.manager().remove(childAbstractRecord);
            added = false;
        }
    }

    private Implementations() {
    }
}
