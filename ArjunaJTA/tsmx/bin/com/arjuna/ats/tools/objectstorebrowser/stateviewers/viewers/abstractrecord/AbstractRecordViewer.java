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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.abstractrecord;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecoveryAbstractRecord;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.tools.objectstorebrowser.panels.ObjectStoreViewEntry;
import com.arjuna.ats.tools.objectstorebrowser.panels.StatePanel;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.AbstractRecordStateViewerInterface;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.UidInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.BasicActionInfo;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.treenodes.IUidCollection;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Default viewer for abstract records
 */
public class AbstractRecordViewer implements AbstractRecordStateViewerInterface
{
    protected enum RecoveryOp {
        NO_OP, COMMIT_OP, ABORT_OP, FORGET_OP
    }

    private XAResource xares;
    private XAResourceRecord xaresrec;
    private BasicActionInfo actionInfo;
    private AbstractRecord record;
    private StringBuilder msgBuf = new StringBuilder();

    protected void updateTableData(AbstractRecord record, StatePanel statePanel)
    {
        UidInfo uidInfo = new UidInfo(record.order(), record.getClass().getName() + "@" + Integer.toHexString(record.hashCode()));

        statePanel.setData("Creation Time", UidInfo.formatTime(uidInfo.getCreationTime()));
        statePanel.setData("Age (seconds)", String.valueOf(uidInfo.getAge()));

        statePanel.setData("Record Type", record.className().stringForm());
        statePanel.setData("Type", record.type());
        statePanel.setData("Uid", record.order().toString());

        statePanel.updateColumnSizes();
        statePanel.clearStatus();
    }

    public void entrySelected(final AbstractRecord record,
                              final BasicAction action,
                              final ObjectStoreViewEntry entry,
                              final StatePanel statePanel) throws ObjectStoreException
    {
        updateTableData(record, statePanel);

        initRecord(action, record, entry);

        statePanel.enableButton(StatePanel.FORGET_BUTTON_TEXT, new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (startAction(statePanel, entry, "This operation will permanently remove this record."))
                    endAction(statePanel, entry, doForget(), true);
            }});
        statePanel.enableButton(StatePanel.COMMIT_BUTTON_TEXT, new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (startAction(statePanel, entry, "This operation will attempt to commit this record."))
                {
                    doCommit();
                    endAction(statePanel, entry, false, false);
                }
            }});
        statePanel.enableButton(StatePanel.ROLLBACK_BUTTON_TEXT, new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                if (startAction(statePanel, entry, "This operation will attempt to rollback this record."))
                {
                    doRollback();
                    endAction(statePanel, entry, false, false);
                }
            }});
    }

    public AbstractRecord getRecord()
    {
        return record;
    }

    protected void initRecord(BasicAction action, AbstractRecord record, ObjectStoreViewEntry entry)
    {
        this.record = record;

        if (action instanceof BasicActionInfo)
            actionInfo = (BasicActionInfo) action;

        if ((record instanceof RecoveryAbstractRecord))
        {
            RecoveryAbstractRecord rrec = (RecoveryAbstractRecord)record;
            if (rrec.record() instanceof XAResourceRecord)
            {
                xaresrec = (XAResourceRecord) rrec.record();
                xares = (XAResource) xaresrec.value();
            }
        }
        else if ((record instanceof XAResourceRecord))
        {
            xaresrec = (XAResourceRecord) record;
            xares = (XAResource) xaresrec.value();
        }
    }

    protected boolean startAction(StatePanel statePanel, ObjectStoreViewEntry entry, String msg)
    {
        StringBuilder sb = new StringBuilder("<html><body>")
                .append(msg).append("<br>")
                .append("Do you wish to continue?")
                .append("</body></html>");

        return JOptionPane.showConfirmDialog(statePanel, sb.toString()) == JOptionPane.YES_OPTION;
    }

    protected void endAction(StatePanel statePanel, ObjectStoreViewEntry entry, boolean success, boolean removeRecord)
    {
        if (success)
        {
            if (removeRecord && entry.getNode().getParent() instanceof IUidCollection)
            {
                ((IUidCollection) entry.getNode().getParent()).remove(record.order());
                // save the changes
                if (!actionInfo.deactivate())
                    appendError("Failed to deactivate record");
            }

            JTree tree = BrowserFrame.getTree();
            TreePath path = tree.getSelectionPath().getParentPath();

            if (path == null)
            {
                TreeNode node = entry.getBrowserNode().getParent().getParent();
                
                if (node != null)
                    path = new TreePath(BrowserFrame.getTreeModel().getPathToRoot(node));
            }

            if (path != null)
            {
                tree.collapsePath(path);
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
            }
        }

        reportErrors(statePanel);        
    }

    protected boolean doForget()
    {
        try
        {
            if (xares != null)
                xares.forget(xaresrec.getXid());

//            if (!actionInfo.deactivate())
//                appendError("Failed to deactivate record");
//            else
                return true;
        }
        catch (XAException e)
        {
            appendError("Failed to deactivate record: " + e.getMessage());
        }

        return false;
    }

    protected boolean doCommit()
    {
        // the user has asked to take control so commit 1PC
        if (xares != null)
        {
            try
            {
                xares.commit(xaresrec.getXid(), true);
                return true;
            }
            catch (XAException e)
            {
                appendError("Commit error: " + e.getMessage());
            }
        }

        return false;
    }

    protected boolean doRollback()
    {
        if (xares != null)
        {
            try
            {
                xares.rollback(xaresrec.getXid());
                return true;
            }
            catch (XAException e)
            {
                appendError("Rollback error: " + e.getMessage());
            }
        }

        return false;
    }

    protected void appendError(String err)
    {
        if (msgBuf.length() != 0)
            msgBuf.append("<br>");

        msgBuf.append(err);
    }

    protected void clearErrors()
    {
        msgBuf.delete(0, msgBuf.length());
    }

    protected void reportErrors(StatePanel parent)
    {
        if (msgBuf.length() != 0)
        {
            parent.reportStatus(msgBuf.toString());
            msgBuf.insert(0, "<html><body>");
            msgBuf.append("</body></html>");
            JOptionPane.showMessageDialog(parent, msgBuf.toString());
            msgBuf.delete(0, msgBuf.length());
        }
    }

    public String getType()
    {
        return "/StateManager/AbstractRecord/";
    }
}
