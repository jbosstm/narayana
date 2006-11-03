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

public class StatePanel extends JPanel implements ActionListener
{
    private final static String TYPE_LABEL_TEXT = "Type:";
    private final static String INFO_LABEL_TEXT = "Information:";
    private final static String DETAILS_BUTTON_TEXT = "details";

    private JLabel                  _type;
    private JLabel                  _info;
    private DefaultTableModel       _tableModel;
    private JTable                  _table;
    private JButton                 _detailsButton;
    private DetailsButtonListener   _detailsListener;

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
        _table = new JTable(_tableModel = new DefaultTableModel(0,2));
        panel.setLayout(new BorderLayout());
        panel.add(BorderLayout.CENTER, _table);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.top = 10;
        gbl.addLayoutComponent(panel, gbc);
        add(panel);

        _detailsButton = new JButton(DETAILS_BUTTON_TEXT);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbl.addLayoutComponent(_detailsButton, gbc);
        _detailsButton.addActionListener(this);
        _detailsButton.setEnabled(false);
        add(_detailsButton);
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

    public void enableDetailsButton(DetailsButtonListener listener)
    {
        _detailsButton.setEnabled(true);
        _detailsListener = listener;
    }

    public void clear()
    {
        _type.setText("");
        _info.setText("");
        _detailsListener = null;
        _detailsButton.setEnabled(false);
        while ( _table.getRowCount() > 0 )
        {
            _tableModel.removeRow(0);
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        if ( actionCommand != null && actionCommand.equals(DETAILS_BUTTON_TEXT) )
        {
            _detailsListener.detailsButtonPressed();
        }
    }
}
