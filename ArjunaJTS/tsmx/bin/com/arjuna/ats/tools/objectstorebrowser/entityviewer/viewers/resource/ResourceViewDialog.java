/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.ats.tools.objectstorebrowser.entityviewer.viewers.resource;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceViewDialog.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.internal.jts.resources.ResourceRecord;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.BasicActionInfo;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

import javax.swing.*;
import javax.transaction.xa.XAResource;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ResourceViewDialog extends JDialog implements ActionListener
{
    private final static String DIALOG_TITLE = "org.omg.CosTransactions.Resource Viewer";

    private final static String CLOSE_BUTTON_LABEL = "Close";
    private final static String FORGET_BUTTON_LABEL = "Forget";

    private AbstractResourceActionHandle _resourceActionHandle;

    /**
     * Creates a non-modal dialog without a title with the
     * specifed <code>Frame</code> as its owner.
     *
     * @param owner the <code>Frame</code> from which the dialog is displayed
     */
    public ResourceViewDialog(Frame owner, String type)
    {
        super(owner, DIALOG_TITLE);

        /** Create layout manager **/
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        getContentPane().setLayout(gbl);

        /** Create type: label **/
        JLabel label = new JLabel("Type:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.addLayoutComponent(label, gbc);
        getContentPane().add(label);

        /** Create type label **/
        label = new JLabel(type);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.addLayoutComponent(label,gbc);
        getContentPane().add(label);

        /** Create button panel with close and forget buttons **/
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton(CLOSE_BUTTON_LABEL);
        JButton forgetButton = new JButton(FORGET_BUTTON_LABEL);
        forgetButton.addActionListener(this);
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);
        buttonPanel.add(forgetButton);

        gbc.gridy++;
        gbl.addLayoutComponent(buttonPanel, gbc);
        getContentPane().add(buttonPanel);

        pack();
        setVisible(true);
    }

    public ResourceViewDialog(Frame owner, String type, AbstractResourceActionHandle rah)
    {
        this(owner, type);
        this._resourceActionHandle = rah;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        if ( actionCommand != null )
        {
            if ( actionCommand.equals(CLOSE_BUTTON_LABEL) )
            {
                dispose();
            }
            else
            if ( actionCommand.equals(FORGET_BUTTON_LABEL))
            {
                if ( JOptionPane.showConfirmDialog(this, "Are you sure you wish to do this?") == JOptionPane.YES_OPTION )
                {
                    try
                    {
                        /** Call forget on the resource handle **/
                        AbstractRecord ar = _resourceActionHandle.getAbstractRecord();

                        if (ar instanceof ResourceRecord)
                        {
                            ((ResourceRecord)ar).resourceHandle().forget();
                        }
                        else if (ar instanceof XAResourceRecord)
                        {
                            XAResourceRecord rc = (XAResourceRecord) ar;

                            ((XAResource)rc.value()).forget(rc.getXid());
                        }
                        else if (ar instanceof ExtendedResourceRecord)
                        {
                            // call forget on the OTS Resource
                            try
                            {
                                ((ExtendedResourceRecord) ar).resourceHandle().forget();
                            }
                            catch (Exception e1)
                            {
                                int res = JOptionPane.showConfirmDialog(this, "An error occurred during forget: "+e1.getMessage() + ". Do you wish to continue");

                                if (res != JOptionPane.YES_OPTION)
                                {
                                    e1.printStackTrace();
                                    return;
                                }
                            }
                        }

                        /** Remove the abstract record from the heuristic list **/
                        BasicAction action = _resourceActionHandle.getAction();

                        if (action instanceof BasicActionInfo)
                            ((BasicActionInfo) action).getHeuristicList().remove(ar);

                        /** Persist state **/
                        if ( !action.deactivate() )
                        {
                            JOptionPane.showMessageDialog(this, "Failed to persist the action");
                        }
                    }
                    catch (Throwable t)
                    {
                        JOptionPane.showMessageDialog(this, "An error occurred during forget: "+t.getMessage());
                        t.printStackTrace(System.err);
                    }

                    dispose();
                }
            }
        }
    }
}
