/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.resources;

import com.arjuna.ats.arjuna.coordinator.*;

public class DummyResource extends AbstractRecord
{
    public DummyResource()
    {
        String debug = System.getProperty("DEBUG", null);

        if (debug != null)
        {
            printDebug = true;
        }

        if (printDebug)
        {
            System.out.println("Creating new resource");
        }
    }

    public static AbstractRecord create()
    {
        return new DummyResource();
    }

    public int nestedAbort()
    {
        if (printDebug)
        {
            System.out.println("nestedAbort");
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit()
    {
        if (printDebug)
        {
            System.out.println("nestedCommit");
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedPrepare()
    {
        if (printDebug)
        {
            System.out.println("nestedPrepare");
        }

        return TwoPhaseOutcome.PREPARE_OK;
    }

    public int topLevelAbort()
    {
        if (printDebug)
        {
            System.out.println("topLevelAbort");
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit()
    {
        if (printDebug)
        {
            System.out.println("topLevelCommit");
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare()
    {
        if (printDebug)
        {
            System.out.println("topLevelPrepare");
        }

        return TwoPhaseOutcome.PREPARE_OK;
    }

    public void alter(AbstractRecord abstractRecord)
    {
    }

    public void merge(AbstractRecord abstractRecord)
    {
    }

    public boolean shouldAdd(AbstractRecord abstractRecord)
    {
        return false;
    }

    public boolean shouldAlter(AbstractRecord abstractRecord)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord abstractRecord)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord abstractRecord)
    {
        return false;
    }

    public void setValue(Object object)
    {
    }

    public Object value()
    {
        return null;
    }

    public int typeIs()
    {
        return RecordType.USER_DEF_FIRST0;
    }

    private boolean printDebug = false;
}