/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransaction;

public class RecoveredTransactionWrapper extends RecoveredTransaction implements ActionBeanWrapperInterface {
    private UidWrapper wrapper;
    private ActionBean action;
    private boolean activated;

    public RecoveredTransactionWrapper () {
        this(Uid.nullUid());
    }

    public RecoveredTransactionWrapper (Uid uid) {
        super(uid);
    }
    public RecoveredTransactionWrapper (ActionBean action, UidWrapper w) {
        super(w.getUid());
        this.wrapper = w;
        this.action = action;
    }

    public boolean activate() {
        if (!activated)
            activated = super.activate();

        return activated;
    }

    public String type () {
        String name = UidWrapper.getRecordWrapperTypeName();

        if (name != null)
            return name;

        return super.type();
    }

    public void doUpdateState() {
        updateState();
    }

    public Uid getUid(AbstractRecord rec) {
        return rec.order();
    }

    public void register() {

    }

    public void unregister() {

    }

    public RecordList getRecords(ParticipantStatus type) {
        switch (type) {
            default:
            case PREPARED: return preparedList;
            case FAILED: return failedList;
            case HEURISTIC: return heuristicList;
            case PENDING: return pendingList;
            case READONLY: return readonlyList;
        }
    }

    public StringBuilder toString(String prefix, StringBuilder sb) {
        prefix += '\t';
        return sb.append('\n').append(prefix).append(get_uid());
    }

    public BasicAction getAction() {
        return null;
    }

    public void clearHeuristicDecision(int newDecision) {
        if (super.heuristicList.size() == 0)
            setHeuristicDecision(newDecision);
    }

    @Override
    public void remove(LogRecordWrapper logRecordWrapper) {
        if (logRecordWrapper.removeFromList(getRecords(logRecordWrapper.getListType()))) {
            doUpdateState(); // rewrite the list
        }
    }
}
