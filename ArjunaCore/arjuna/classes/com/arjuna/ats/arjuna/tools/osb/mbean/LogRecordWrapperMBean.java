/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

/**
 * JMX MBean interface for transaction participants.
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
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

	// TODO create an MBean to represent the different types of heuristics
	@MXBeanPropertyDescription("If this record represents a heuristic then report the type of the heuristic")
	String getHeuristicStatus();
}
