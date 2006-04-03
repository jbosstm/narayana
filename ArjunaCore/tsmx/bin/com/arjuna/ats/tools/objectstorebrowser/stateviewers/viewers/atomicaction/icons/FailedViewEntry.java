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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.icons;

import com.arjuna.ats.tools.objectstorebrowser.panels.ListViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.DetailsButtonAdapter;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ObjectStoreBrowserNode;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.nodes.FailedListNode;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.AtomicActionWrapper;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;

import javax.swing.tree.TreePath;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FailedViewEntry.java 2342 2006-03-30 13:06:17Z  $
 */

public class FailedViewEntry extends ListViewEntry implements IconSelectionListener
{
    public FailedViewEntry(String tn, String label, ObjectStoreBrowserNode node)
    {
        super(tn, label, node);

        addSelectionListener(this);
    }

    /**
     * Called when this entry is selected
     * @param icon
     * @param selected
     */
    public void iconSelected(IconPanelEntry icon, boolean selected)
    {
        /** Populate panel with details of this list **/
        StatePanel panel = BrowserFrame.getStatePanel();
        final FailedListNode node = (FailedListNode)getNode();
        AtomicActionWrapper aaw = (AtomicActionWrapper)node.getAssObject();
        panel.clear();
        panel.setType(getTypeName());
        panel.setInfo(aaw.getFailedList().size()+" entries");
        panel.enableDetailsButton(new DetailsButtonAdapter() {
            public void detailsButtonPressed()
            {
                TreePath path = new TreePath(BrowserFrame.getTreeModel().getPathToRoot(node));
                BrowserFrame.getTree().expandPath(path);
                BrowserFrame.getTree().setSelectionPath(path);
            }
        });
        panel.repaint();
    }
}
