/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
