/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.resource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordListIterator;
import com.arjuna.ats.arjuna.coordinator.RecordType;

import java.util.List;

public class RecoveringTransaction extends Transaction {
    public RecoveringTransaction(Uid uid) {
        super(uid);
    }

    protected int lookupStatus() {
        return ActionStatus.COMMITTING;
    }

    public List<RESTRecord> getParticipants(List<RESTRecord> participants) {
        RecordListIterator iter = new RecordListIterator(preparedList);
        AbstractRecord recordBeingHandled;

        while (((recordBeingHandled = iter.iterate()) != null)) {
            if (recordBeingHandled.activate()) {
                if (recordBeingHandled.typeIs() == RecordType.RESTAT_RECORD) {
                    participants.add((RESTRecord) recordBeingHandled);
                } else {
                    log.warnf("Could not reactivate participant %s of transaction %s",
                            recordBeingHandled.get_uid(), get_uid());
                }

            }
        }

        return participants;
    }
}