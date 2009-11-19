/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.arjunatransaction;

import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.arjunatransaction.icons.*;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.arjunatransaction.nodes.*;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.TxInfoNode;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.TxInfoViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.ArjunaTransactionWrapper;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.AtomicActionViewer;
import com.arjuna.ats.tools.objectstorebrowser.ObjectStoreBrowserTreeManipulationInterface;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.SubTreeViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.UidNode;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNode;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordList;

import javax.swing.tree.DefaultMutableTreeNode;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ArjunaTransactionViewer.java 2342 2006-03-30 13:06:17Z  $
 */


public class ArjunaTransactionViewer extends AtomicActionViewer
{
    /**
     * A uid node of the type this viewer is registered against has been expanded.
     * @param os
     * @param type
     * @param manipulator
     * @param uidNode
     * @throws ObjectStoreException
     */
    public void uidNodeExpanded(ObjectStore os,
                                String type,
                                ObjectStoreBrowserTreeManipulationInterface manipulator,
                                UidNode uidNode,
                                StatePanel infoPanel) throws ObjectStoreException
    {
        Uid theUid = uidNode.getUid();
        ArjunaTransactionWrapper ba = new ArjunaTransactionWrapper(os, type, theUid);
        ListNode node;
        SubTreeViewEntry entry;

        manipulator.clearEntries();

        if ( !activate(ba) )
        {
            infoPanel.reportError("Failed to activate transaction");

            node = new TxInfoNode("Tx Info", ba, type);
            entry = new TxInfoViewEntry(type, "Info", node);
            addNode(manipulator, node, entry, "Basic Information");
        }
        else
        {
            node = new TxInfoNode("Tx Info", ba, type);
            entry = new TxInfoViewEntry(type, "Info", node);
            addNode(manipulator, node, entry, "Basic Information");
            
            manipulator.createEntry(node = new PreparedListNode("Prepared List", ba, type));
            node.setIconPanelEntry(entry = new PreparedViewEntry(type, "Prepared List", node));
            entry.setToolTipText(getListSize(ba.getPreparedList())+" entries");
            node.add(new DefaultMutableTreeNode(""));

            manipulator.createEntry(node = new PendingListNode("Pending List", ba, type));
            node.setIconPanelEntry(entry = new PendingViewEntry(type, "Pending List", node));
            entry.setToolTipText(getListSize(ba.getPendingList())+" entries");
            node.add(new DefaultMutableTreeNode(""));

            manipulator.createEntry(node = new HeuristicListNode("Heuristic List", ba, type));
            node.setIconPanelEntry(entry = new HeuristicViewEntry(type, "Heuristic List", node));
            entry.setToolTipText(getListSize(ba.getHeuristicList())+" entries");
            node.add(new DefaultMutableTreeNode(""));

            manipulator.createEntry(node = new FailedListNode("Failed List", ba, type));
            node.setIconPanelEntry(entry = new FailedViewEntry(type, "Failed List", node));
            entry.setToolTipText(getListSize(ba.getFailedList())+" entries");
            node.add(new DefaultMutableTreeNode(""));

            manipulator.createEntry(node = new ReadOnlyListNode("Read-only List", ba, type));
            node.setIconPanelEntry(entry = new ReadOnlyViewEntry(type, "Read-only List", node));
            entry.setToolTipText(getListSize(ba.getReadOnlyList())+" entries");
            node.add(new DefaultMutableTreeNode(""));
        }
    }

    /**
     * Get the type this state viewer is intended to be registered against.
     * @return
     */
    public String getType()
    {
        return "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/";
    }

    private int getListSize(RecordList list)
    {
        return list != null ? list.size() : 0;
    }
}
