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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction.icons;

import com.arjuna.ats.tools.objectstorebrowser.panels.ListViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ObjectStoreBrowserNode;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNode;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.BasicActionInfo;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;
import com.arjuna.ats.arjuna.coordinator.RecordList;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AtomicTransactionViewEntry extends ListViewEntry implements IconSelectionListener
{
    public AtomicTransactionViewEntry(String tn, String label, ObjectStoreBrowserNode node)
    {
        super(tn, label, node);

        addSelectionListener(this);
    }

    public void iconSelected(IconPanelEntry icon, boolean selected)
    {
        StatePanel panel = BrowserFrame.getStatePanel();
        final ListNode node = (ListNode) getNode();
        BasicActionInfo ba = (BasicActionInfo) node.getAssObject();
        RecordList list = getList(ba);

        panel.clear();
        panel.setType(getTypeName());
        panel.setInfo(list == null ? "0 entries" : list.size() + " entries");

        updatePanelData(panel, ba);

        if (list != null && list.size() != 0)
            enableDetailsButton(panel, node);

        panel.repaint();
    }

    protected void enableDetailsButton(StatePanel panel, final TreeNode node)
    {
        panel.enableButton(StatePanel.DETAILS_BUTTON_TEXT, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                TreePath path = new TreePath(BrowserFrame.getTreeModel().getPathToRoot(node));
                BrowserFrame.getTree().expandPath(path);
                BrowserFrame.getTree().setSelectionPath(path);
                BrowserFrame.getTree().scrollPathToVisible(path);
            }
        });
    }

    protected void updatePanelData(StatePanel panel, BasicActionInfo wrapper)
    {
    }

    protected RecordList getList(BasicActionInfo wrapper)
    {
        return emptyList;
    }

    RecordList  emptyList = new RecordList();
}
