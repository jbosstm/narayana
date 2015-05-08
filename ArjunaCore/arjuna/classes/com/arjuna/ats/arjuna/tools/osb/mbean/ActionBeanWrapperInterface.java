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
