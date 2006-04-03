/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * $Id: AtomicActionListNode.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNode;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.StateViewersRepository;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.AbstractRecordStateViewerInterface;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.AtomicActionWrapper;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

public abstract class AtomicActionListNode extends ListNode
{
    public AtomicActionListNode(Object userObject, Object assObject, String type)
    {
        super(userObject, assObject, type);
    }

    protected void invokeStateViewer(AbstractRecord record, AtomicActionWrapper aaw, IconPanelEntry entry)
    {
        AbstractRecordStateViewerInterface svi = StateViewersRepository.lookupAbstractRecordStateViewer(record.type());

        if ( svi != null )
        {
            try
            {
                svi.entrySelected(record, aaw, (ObjectStoreViewEntry)entry, BrowserFrame.getStatePanel());
            }
            catch (ObjectStoreException e)
            {
                e.printStackTrace(System.err);
            }
        }
        else
        {
            System.out.println("Viewer not registered for "+record.type());
        }
    }
}
