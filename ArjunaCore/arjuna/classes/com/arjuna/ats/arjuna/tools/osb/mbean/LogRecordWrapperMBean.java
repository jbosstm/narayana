/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
