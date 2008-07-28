/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.panels;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: StatePanel.java 2342 2006-03-30 13:06:17Z  $
 */

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;

public class StatePanel extends JPanel implements ActionListener
{
    private final static String TYPE_LABEL_TEXT = "Type:";
    private final static String INFO_LABEL_TEXT = "Information:";
    
    public final static String DETAILS_BUTTON_TEXT = "details";
    public final static String FORGET_BUTTON_TEXT = "forget";
    public final static String COMMIT_BUTTON_TEXT = "commit";
    public final static String ROLLBACK_BUTTON_TEXT = "rollback";

    private final static Map<String, String> BUTTONS;

    static {
        BUTTONS = new HashMap<String, String> ();

        BUTTONS.put(DETAILS_BUTTON_TEXT, "Show more information");
        BUTTONS.put(FORGET_BUTTON_TEXT, "Forget this item");
        BUTTONS.put(COMMIT_BUTTON_TEXT, "Commit the item");
        BUTTONS.put(ROLLBACK_BUTTON_TEXT, "Rollback the item");
    }

    private JLabel                  _type;
    private JLabel                  _info;
    private JLabel                  _statusBar;
    private DefaultTableModel       _tableModel;
    private StateTable              _table;
    private Map<String, JButton>    _buttons = new HashMap<String, JButton>();

    public StatePanel()
    {
        super();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        this.setLayout(gbl);

        /** Create and add type label **/
        JLabel label = new JLabel(TYPE_LABEL_TEXT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.addLayoutComponent(label, gbc);
        add(label);

        /** Create and add type text label **/
        _type = new JLabel("");
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.addLayoutComponent(_type, gbc);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        add(_type);

        /** Create and add info label **/
        label = new JLabel(INFO_LABEL_TEXT);
        gbc.gridx=0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbl.addLayoutComponent(label, gbc);
        add(label);

        /** Create and add info text label **/
        _info = new JLabel("");
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbl.addLayoutComponent(_info, gbc);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        add(_info);

        JPanel panel = new JPanel();
        panel.setBorder(javax.swing.border.LineBorder.createBlackLineBorder());
        _table = new StateTable(_tableModel = new DefaultTableModel(0,2));
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.CENTER, _table);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.top = 10;
        gbl.addLayoutComponent(panel, gbc);
        add(panel);

        JPanel buttonPanel = new JPanel();

        for (Map.Entry<String, String> e : BUTTONS.entrySet())
            buttonPanel.add(newButton(e.getKey(), e.getValue(), false, this));

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        gbl.addLayoutComponent(buttonPanel, gbc);
        add(buttonPanel);
    }

    /**
     * Create a new button. Note this does not add the button to the panel which
     * is the callers responsibility
     *
     * @param text button text
     * @param tooltip button tooltip
     * @param enable if true the button is enabled and made visible
     * @param listener
     * @return the newly added button
     */
    private JButton newButton(String text, String tooltip, boolean enable, ActionListener listener)
    {
        JButton button = new JButton(text);

        button.setToolTipText(tooltip);
        _buttons.put(text, button);
        enableButtons(enable, listener, text);

        return button;
    }

    /**
     * Initialise a collection of buttons. Any existing listeners will be removed.
     *
     * @param enable if true enable the button and make it visible
     * @param listener the action
     * @param buttons button (these must correspond to a previously registered button)
     */
    public void enableButtons(boolean enable, ActionListener listener, String ... buttons)
    {
        for (String name : buttons)
        {
            JButton button = _buttons.get(name);

            if (button != null)
            {
                for (ActionListener l : button.getActionListeners())
                    button.removeActionListener(l);

                button.addActionListener(listener);
                button.setEnabled(enable);
                button.setVisible(enable);
            }
        }
    }

    public void enableButton(String buttonName, ActionListener listener)
    {
         enableButtons(true, listener, buttonName);
    }
    
    public void setType(String type)
    {
        _type.setText(type);
    }

    public void setInfo(String info)
    {
        _info.setText(info);
    }

    public void setData(String name, String value)
    {
        _tableModel.addRow(new String[] { name, value });
        _table.invalidate();
    }

    /**
     * Add headers for the table in the state panel
     * @param name first column
     * @param value second column
     */
    public void setTableHeader(String name, String value)
    {
        _table.shadeHeaders(true);
        _tableModel.insertRow(0, new String[] { name, value });
    }

    /**
     * Size the columns to accomodate the data (ie shrink to largest data size or expand
     * to largest data size up to a maximum)
     */
    public void updateColumnSizes()
    {
        _table.updateColumnSizes();
    }

    public void clear()
    {
        _type.setText("");
        _info.setText("");
        _table.shadeHeaders(false);
        enableButtons(false, null, _buttons.keySet().toArray(new String[_buttons.size()]));
        clearStatus();
        
        while ( _table.getRowCount() > 0 )
        {
            _tableModel.removeRow(0);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
    }

    public void setStatusBar(JLabel statusBar)
    {
        this._statusBar = statusBar;
    }

    public void reportStatus(String message)
    {
        if (_statusBar != null)
            _statusBar.setText(message);
    }

    public void reportError(String message)
    {
        reportStatus(message);
    }

    public int reportStatus(String message, int severity)
    {
        switch (severity)
        {
            case JOptionPane.ERROR_MESSAGE:
                JOptionPane.showMessageDialog(this, message, "Error", severity);
                break;
            case JOptionPane.INFORMATION_MESSAGE:
                JOptionPane.showMessageDialog(this, message, "Information", severity);
                break;
            case JOptionPane.WARNING_MESSAGE:
                JOptionPane.showMessageDialog(this, message, "Warning", severity);
                break;
            case JOptionPane.QUESTION_MESSAGE:
                return JOptionPane.showConfirmDialog(this, message);
            case JOptionPane.PLAIN_MESSAGE:
                //FALLTHRU
            default:
                break;
        }

        return JOptionPane.OK_OPTION;
    }

    public void clearStatus()
    {
        reportStatus("");
    }
}
