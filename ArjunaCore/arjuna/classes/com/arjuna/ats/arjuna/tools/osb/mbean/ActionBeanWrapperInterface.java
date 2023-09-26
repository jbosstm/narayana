/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;

/**
 * Common interface for JTA and JTS transactions
 *
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 *
 * @author Mike Musgrove
 */
@Deprecated // in order to provide a better separation between public and internal classes.
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
