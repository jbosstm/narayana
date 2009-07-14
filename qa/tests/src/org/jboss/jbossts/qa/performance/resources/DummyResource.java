/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
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
