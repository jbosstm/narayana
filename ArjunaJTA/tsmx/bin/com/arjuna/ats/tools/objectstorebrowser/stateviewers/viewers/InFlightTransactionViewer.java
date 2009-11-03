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

import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.AtomicActionViewer;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.LiveAtomicActionWrapper;
import com.arjuna.ats.tools.objectstorebrowser.ObjectStoreBrowserTreeManipulationInterface;
import com.arjuna.ats.tools.objectstorebrowser.ObjectStoreBrowserPlugin;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.TxInputObjectState;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.InFlightTransactionPseudoStore;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.panels.SubTreeViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.UidNode;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNode;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.jta.transaction.Transaction;

/**
 * Viewer for inspecting running transaction within the object browser
 */
public class InFlightTransactionViewer extends AtomicActionViewer
{
   // private static final String STORE_KEY = com.arjuna.ats.arjuna.common.Environment.STATIC_INVENTORY_IMPLE + '.' + InFlightTransactionPseudoStore.STORE_NAME;
    private static final String STORE_VAL = "com.arjuna.ats.tools.objectstorebrowser.rootprovider.InFlightTransactionPseudoStore";

    public InFlightTransactionViewer() throws ToolPluginException
    {
        if (ObjectStoreBrowserPlugin.getRootProvider() == null)
            throw new ToolPluginException("No object store provider has been configured");

        ObjectStoreBrowserPlugin.getRootProvider().addRoot(InFlightTransactionPseudoStore.STORE_NAME);
    }

    public void uidNodeExpanded(ObjectStore os,
                                String type,
                                ObjectStoreBrowserTreeManipulationInterface manipulator,
                                UidNode uidNode,
                                StatePanel infoPanel) throws ObjectStoreException
    {
        if (!InFlightTransactionPseudoStore.STORE_NAME.equals(os.getStoreName()))
            return; // called with the wrong store name
        
        Uid theUid = uidNode.getUid();
        Transaction delegate = null;
        Object ios = os.read_committed(theUid, type);

        if (ios instanceof TxInputObjectState)
            delegate = (Transaction)((TxInputObjectState)ios).getRealObject();

        LiveAtomicActionWrapper aaw = new LiveAtomicActionWrapper(delegate, theUid, type);
        manipulator.clearEntries();

        ListNode node;
        SubTreeViewEntry entry;

        node = new TxInfoNode("Tx Info", aaw, type);
        entry = new TxInfoViewEntry(type, "Info", node);
        addNode(manipulator, node, entry, "Basic Information");

        node = new SynchronizationListNode("Synchronisations", aaw, type);
        entry = new SynchronizationViewEntry(type + "Synchronisation", "Synchronisations", node);
        addNode(manipulator, node, entry, aaw.getSynchronizationInfo().size() + " synchronizations");

        node = new XAResourceListNode("XA Resources", aaw, type);
        entry = new XAResourceViewEntry(type + "XAResource", "XA Resources", node);
        addNode(manipulator, node, entry, aaw.getResources().size() + " resources");

    }

    // show current state of tx, synchronisations, XA resources
    public String getType()
    {
        return "/Transaction/";
    }
}
