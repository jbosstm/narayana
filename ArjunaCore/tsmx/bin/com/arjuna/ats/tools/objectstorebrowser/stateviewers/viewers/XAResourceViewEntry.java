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

import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ObjectStoreBrowserNode;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.icons.AtomicTransactionViewEntry;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;

import java.util.Collection;

public class XAResourceViewEntry extends AtomicTransactionViewEntry
{
    public XAResourceViewEntry(String tn, String label, ObjectStoreBrowserNode node)
    {
        super(tn, label, node);

        addSelectionListener(this);
    }

    public void iconSelected(IconPanelEntry icon, boolean selected)
    {
        StatePanel panel = BrowserFrame.getStatePanel();
        XAResourceListNode node = (XAResourceListNode) getNode();
        ArjunaTransactionWrapper aaw = (ArjunaTransactionWrapper) node.getAssObject();
        Collection<XAResourceInfo> c = aaw.getResources();

        panel.clear();
        panel.setType(getTypeName());
        panel.setInfo(c.size()+" entries");
        panel.setTableHeader("Product Name", "Instance Name");

        for (XAResourceInfo xares : c)
            panel.setData(xares.getEisProductName(), xares.getInstanceName());
        
        if (c.size() != 0)
        {
            enableDetailsButton(panel, node);
            panel.updateColumnSizes();
        }

        panel.repaint();
    }
}
