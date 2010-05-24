package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

@MXBeanDescription("Representation of a transaction participant")
public interface LogRecordWrapperMBean extends OSEntryBeanMBean {
	@MXBeanPropertyDescription("Indication of the status of this transaction participant (prepared, heuristic, etc)")
	String getStatus();

	//@MXBeanPropertyDescription("Change the status of this participant back to prepared or to a heuristic")
	void setStatus(String newState);

    @MXBeanPropertyDescription("Clear any heuristics so that the recovery system will replay the commit")
    String clearHeuristic();
    
	@MXBeanPropertyDescription("The internal type of this transaction participant")
	String getType();

	@MXBeanPropertyDescription("This entry corresponds to a transaction participant")
	boolean isParticipant();
}
