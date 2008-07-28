/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.nodes;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: HeuristicListNode.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.tools.objectstorebrowser.panels.*;

import com.arjuna.ats.arjuna.coordinator.*;

public class HeuristicListNode extends AtomicActionListNode
{
    public HeuristicListNode(Object userObject, Object assObject, String type)
    {
        super(userObject, assObject, type);
    }

    public RecordList getList()
    {
        return getAction().getHeuristicList();
    }

    protected void updatePanelData(StatePanel panel, AbstractRecord record)
    {
        if ( record.value() instanceof HeuristicInformation )
        {
            HeuristicInformation heuristicInfo = (HeuristicInformation)record.value();

            panel.setData("Heuristic Type", TwoPhaseOutcome.stringForm(heuristicInfo.getHeuristicType()));
        }
    }
}
