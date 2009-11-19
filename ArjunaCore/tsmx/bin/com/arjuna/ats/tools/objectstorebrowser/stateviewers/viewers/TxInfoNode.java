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
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListNodeListener;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.ListEntryNode;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconSelectionListener;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanelEntry;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TxInfoNode extends ListNode implements ListNodeListener, IconSelectionListener
{
    public TxInfoNode(Object userObject, Object assObject, String type)
    {
        super(userObject, assObject, type);
    }

    public void listExpanded(ListNode node)
    {
        super.listExpanded(node);

        BasicActionInfo aaw = getAction();
        ListEntryNode entryNode = new ListEntryNode(aaw.get_uid(), aaw, getType());
        ObjectStoreViewEntry icon = new ObjectStoreViewEntry(aaw.type(), entryNode.getUserObject().toString(), entryNode);

        node.createEntry(entryNode);
        entryNode.setIconPanelEntry(icon);
        icon.addSelectionListener(this);
    }


    public void iconSelected(IconPanelEntry icon, boolean selected)
    {

        StatePanel panel = BrowserFrame.getStatePanel();

        updatePanel(panel);
    }

    public void updatePanel(StatePanel panel)
    {
        BasicActionInfo aaw = getAction();
        UidInfo uidInfo = aaw.getUidInfo();

        panel.clear();
        panel.setType(aaw.type());
        panel.setInfo(aaw.get_uid().stringForm());

        panel.setData("Creation Time", UidInfo.formatTime(uidInfo.getCreationTime()));
        panel.setData("Age (seconds)", String.valueOf(uidInfo.getAge()));
        panel.setData("Status", jtaStatusToString(aaw.getStatus()));
        panel.setData("Timeout", String.valueOf(aaw.getTxTimeout()));

        panel.updateColumnSizes();

        if (!isLive())
            enableDetails(panel);

        panel.repaint();
    }

    public BasicActionInfo getAction()
    {
        return (BasicActionInfo) getAssObject();
    }

    private void enableDetails(final StatePanel panel)
    {
        panel.enableButton(StatePanel.FORGET_BUTTON_TEXT, new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                String cmd = ae.getActionCommand();

                if (cmd != null)
                {
                    int res = JOptionPane.showConfirmDialog(panel, "This operation will permanently remove the transaction. Do you wish to continue?");

                    if (res == JOptionPane.YES_OPTION)
                    {
                        try
                        {
                            TreeNode parent = getParent().getParent();
                            getAction().remove();

                            // the node should no longer exist so collapse the tree up to its parent.
                            if (parent != null)
                            {
                                TreePath path = new TreePath(BrowserFrame.getTreeModel().getPathToRoot(parent));
                                BrowserFrame.getTree().collapsePath(path);
                                BrowserFrame.getTree().setSelectionPath(path);
                                BrowserFrame.getTree().scrollPathToVisible(path);
                            }
                        }
                        catch (ObjectStoreException e)
                        {
                            panel.reportStatus("Forget failed: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    // copied form JTAHelper (which is not in ArjunaCore
    public static String jtaStatusToString (int status)
    {
        switch (status)
        {
            case javax.transaction.Status.STATUS_ACTIVE:
                return "javax.transaction.Status.STATUS_ACTIVE";
            case javax.transaction.Status.STATUS_COMMITTED:
                return "javax.transaction.Status.STATUS_COMMITTED";
            case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
                return "javax.transaction.Status.STATUS_MARKED_ROLLBACK";
            case javax.transaction.Status.STATUS_NO_TRANSACTION:
                return "javax.transaction.Status.STATUS_NO_TRANSACTION";
            case javax.transaction.Status.STATUS_PREPARED:
                return "javax.transaction.Status.STATUS_PREPARED";
            case javax.transaction.Status.STATUS_ROLLEDBACK:
                return "javax.transaction.Status.STATUS_ROLLEDBACK";
            case javax.transaction.Status.STATUS_UNKNOWN:
            default:
                return "javax.transaction.Status.STATUS_UNKNOWN";
        }
    }

    public boolean isLive()
    {
        return ((BasicActionInfo) getAssObject()).isLive();
    }
}
