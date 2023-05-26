/*
 * SPDX short identifier: Apache-2.0
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