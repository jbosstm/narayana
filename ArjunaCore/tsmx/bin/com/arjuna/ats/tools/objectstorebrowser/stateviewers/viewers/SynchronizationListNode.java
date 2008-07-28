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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers;

import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNode;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListEntryNode;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNodeListener;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;

/**
 * A node representing a synchronisation that has been registered with a transaction
 */
public class SynchronizationListNode extends ListNode implements ListNodeListener, IconSelectionListener
{
    public SynchronizationListNode(Object userObject, Object assObject, String type)
    {
        super(userObject, assObject, type);
    }

    /**
     ** Called when the list node is expanded
     ** @param node the selected node
     **/
    public void listExpanded(ListNode node)
    {
        super.listExpanded(node);

        ArjunaTransactionWrapper aaw = (ArjunaTransactionWrapper) this.getAssObject();
        int i = 0;

        for (SynchronizationInfo si : aaw.getSynchronizationInfo())
        {
            ListEntryNode entryNode = new ListEntryNode("[" + i + "] " + si.getInstanceName(), si, "synchronisation");
            ObjectStoreViewEntry icon = new ObjectStoreViewEntry(aaw.type(), entryNode.getUserObject().toString(), entryNode);

            node.createEntry(entryNode);
            entryNode.setIconPanelEntry(icon);
            icon.addSelectionListener(this);

            i += 1;            
        }
    }

    public void iconSelected(IconPanelEntry icon, boolean selected)
    {
        ListEntryNode node = (ListEntryNode)(((ObjectStoreViewEntry)icon).getNode());
        SynchronizationInfo si = (SynchronizationInfo) node.getAssociatedObject();
        StatePanel panel = BrowserFrame.getStatePanel();

        panel.clear();
        panel.setType("Synchronization");
        panel.setInfo(si.getInstanceName());

        panel.setData("Creation Time", UidInfo.formatTime(si.getCreationTime()));
        panel.setData("Age (seconds)", String.valueOf(si.getAge()));

        panel.updateColumnSizes();
        panel.repaint();
    }
}
