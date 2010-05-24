package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordList;

/**
 * common interface for JTA and JTS transactions
 */
public interface ActionBeanWrapperInterface {
	RecordList getRecords(ParticipantStatus type);
	boolean activate();
	void doUpdateState();
	Uid get_uid();
	Uid getUid(AbstractRecord rec);
	StringBuilder toString(String prefix, StringBuilder sb);

    void clearHeuristicDecision(int newDecision);
}