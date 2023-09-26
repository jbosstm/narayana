/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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