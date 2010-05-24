package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;

/**
 * MBean wrapper for exposing the lists maintained by a JTS transaction
 *
 * @see com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple
 */
public class ArjunaTransactionImpleWrapper extends ArjunaTransactionImple implements ActionBeanWrapperInterface {

    ActionBean action;
    boolean activated;

    public ArjunaTransactionImpleWrapper (ActionBean action, UidWrapper w) {
        super(w.getUid());
        this.action = action;
    }

    public boolean activate() {
        if (!activated)
            activated = super.activate();

        return activated;
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

    public void clearHeuristicDecision(int newDecision) {
        if (super.heuristicList.size() == 0)
            setHeuristicDecision(newDecision);
    }
}
