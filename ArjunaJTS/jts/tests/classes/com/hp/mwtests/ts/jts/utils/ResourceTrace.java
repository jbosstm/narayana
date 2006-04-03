/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceTrace.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.utils;

public class ResourceTrace
{
    public static final int	ResourceTraceUnknown = 0,
        			ResourceTraceNone = 1,
        			ResourceTracePrepare = 2,
        			ResourceTracePrepareHeuristicHazard = 11,
        			ResourceTracePrepareCommitHeurisiticRollback = 12,
        			ResourceTracePrepareCommitHeurisiticRollbackForget = 13,
        			ResourceTracePrepareHeuristicHazardForget = 14,
        			ResourceTracePrepareCommit = 3,
			        ResourceTracePrepareRollback = 4,
			        ResourceTraceCommitOnePhase = 5,
			        ResourceTraceRollback = 6,
			        ResourceTracePrepareForget = 7,
			        ResourceTracePrepareCommitForget = 8,
			        ResourceTracePrepareRollbackForget = 9,
			        ResourceTraceCommitOnePhaseForget = 10;

    private int			_value = ResourceTraceNone;

    public int getTrace()
    {
    	return(_value);
    }

    public void setTrace(int value)
    {
   	_value = value;
    }
}
