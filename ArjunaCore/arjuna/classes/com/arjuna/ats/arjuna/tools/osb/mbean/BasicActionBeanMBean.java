/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 *
 * (C) 2009
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.common.StateBeanMBean;

/**
 * MBean representation of an ObjectStore BasicAction providing a summary of the number
 * of records in each of the various lists
 *
 * @see com.arjuna.ats.arjuna.coordinator.BasicAction
 */

@MXBeanDescription("A representation of the state of each participant involved in an action")
public interface BasicActionBeanMBean extends StateBeanMBean
{
//	@MXBeanPropertyDescription("Permanently remove this mxbean")
//    public void remove();
/*  TODO array types don't seem to work with the JMX agent inside the JBoss AS
    @MXBeanPropertyDescription("Participants that failed during phase 2 (of a two phase commit protocol)")
    String[] getFailedList();
    @MXBeanPropertyDescription("Particpants that returned a heuristic out come during phase 2 (of a two phase commit protocol)")
    String[] getHeuristicList();
    @MXBeanPropertyDescription("Participants that have not had phase 1 (of a two phase commit protocol) called on them")
    String[] getPendingList();
    @MXBeanPropertyDescription("Particpants that have successfully completed phase 1 (of a two phase commit protocol)")
    String[] getPreparedList();
    @MXBeanPropertyDescription("Particpants that indicated no chages were made when asked to complete phase 1 (of a two phase commit protocol)")
    String[] getReadOnlyList();
*/

    @MXBeanPropertyDescription("Number of participants that failed during phase 2 (of a two phase commit protocol)")    
    int getFailedCount();
    @MXBeanPropertyDescription("Number of particpants that returned a heuristic out come during phase 2 (of a two phase commit protocol)")
    int getHeuristicCount();
    @MXBeanPropertyDescription("Number of particpants that have successfully completed phase 1 (of a two phase commit protocol)")
    int getPendingCount();
    @MXBeanPropertyDescription("Number of particpants that indicated no chages were made when asked to complete phase 1 (of a two phase commit protocol)")
    int getPreparedCount();
    @MXBeanPropertyDescription("Number of particpants that indicated no chages were made when asked to complete phase 1 (of a two phase commit protocol)")
    int getReadOnlyCount();
}
