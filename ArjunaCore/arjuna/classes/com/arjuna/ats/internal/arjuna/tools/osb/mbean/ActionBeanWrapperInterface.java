package com.arjuna.ats.internal.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;

/**
 * Common interface for JTA and JTS transactions
 *
 * @author Mike Musgrove
 */
public interface ActionBeanWrapperInterface {
    RecordList getRecords(ParticipantStatus type);
    boolean activate();
    void doUpdateState();
    Uid get_uid();
    Uid getUid(AbstractRecord rec);
    StringBuilder toString(String prefix, StringBuilder sb);
    BasicAction getAction();

    void clearHeuristicDecision(int newDecision);

    void remove(LogRecordWrapper logRecordWrapper);
}
