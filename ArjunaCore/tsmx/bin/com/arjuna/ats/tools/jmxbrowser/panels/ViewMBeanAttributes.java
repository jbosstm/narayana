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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: ViewMBeanAttributes.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels;

import com.arjuna.ats.tools.jmxbrowser.AttributesTableModel;
import com.arjuna.ats.tools.jmxbrowser.AttributeEditor;
import com.arjuna.ats.tsmx.TransactionServiceMX;
import com.arjuna.ats.tsmx.agent.exceptions.AgentNotFoundException;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.management.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ViewMBeanAttributes extends JPanel implements ActionListener, TableModelListener
{
    private final static String REFRESH_BUTTON_TEXT = "Refresh";
	private final static String REFRESH_ICON_FILENAME = "refresh.gif";

	private JTable					_attributesTable;
	private AttributesTableModel   	_attributes;
    private ObjectName				_mbeanName;
	private boolean					_refreshing = true;

    public ViewMBeanAttributes(ObjectName mbeanName)
	{
		_mbeanName = mbeanName;

		/** Setup dialog **/
        GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);
		this.setBackground(Color.white);

    	/** Create and add attributes label **/
		JLabel label = new JLabel("Attributes:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Create and add attributes table **/
		_attributesTable = new JTable(_attributes = createAttributesTable());
		_attributesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_attributesTable.setDefaultEditor(Object.class, new AttributeEditor(_attributes));
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets.top = 0;
		JScrollPane tableScrollPane = new JScrollPane(_attributesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gbl.setConstraints(tableScrollPane, gbc);
		this.add(tableScrollPane);

		/** Create and add refresh button **/
		JButton refreshButton = new JButton(REFRESH_BUTTON_TEXT);
		refreshButton.setMnemonic(KeyEvent.VK_R);
		refreshButton.addActionListener(this);
		refreshButton.setIcon( new ImageIcon( ClassLoader.getSystemResource( REFRESH_ICON_FILENAME ) ) );
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridy = 2;
		gbc.insets.bottom = 10;
		gbc.fill = GridBagConstraints.NONE;
		gbl.setConstraints(refreshButton, gbc);
		this.add(refreshButton);

		_refreshing = false;
	}

	/**
	 * Create attributes table model
	 * @return The table model for the attributes table.
	 */
	private AttributesTableModel createAttributesTable()
	{
		AttributesTableModel table = new AttributesTableModel();
		table.addTableModelListener(this);
		/** Attempt to set the objectname **/
        if ( !table.setObjectName(_mbeanName) )
		{
			JOptionPane.showMessageDialog(this,"Unable to view the attributes to this MBean");
			table = null;
		}

		return table;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals ( REFRESH_BUTTON_TEXT) )
		{
			_refreshing = true;
			if ( !_attributes.setObjectName(_mbeanName) )
			{
				JOptionPane.showMessageDialog(this, "Failed to refresh the mbean's attributes");
			}
			_refreshing = false;
		}
	}

	/**
	 * This fine grain notification tells listeners the exact range
	 * of cells, rows, or columns that changed.
	 */
	public void tableChanged(TableModelEvent e)
	{
		if ( ( !_refreshing) && ( e.getType() == TableModelEvent.UPDATE ) )
		{
			String propertyName = (String)_attributes.getValueAt(e.getLastRow(), AttributesTableModel.NAME_COLUMN);
			Object propertyValue = _attributes.getValueAt(e.getLastRow(), AttributesTableModel.VALUE_COLUMN);

			Object currentValue = getAttribute( _mbeanName, propertyName );

			if ( ( !currentValue.equals(propertyValue) ) && ( _attributes.isCellEditable(e.getLastRow(), AttributesTableModel.VALUE_COLUMN)) )
			{
				setAttribute(_mbeanName, new Attribute(propertyName, propertyValue));
			}
		}
	}

	private void setAttribute(ObjectName mbeanName, Attribute attr)
	{
		try
		{
			TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().setAttribute(mbeanName, attr);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to set property '"+attr.getName()+"' to specified value");
		}
	}

	private Object getAttribute(ObjectName mbeanName, String attributeName)
	{
		Object attributeValue = "";
		try
		{
			attributeValue = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(mbeanName, attributeName);

			if ( attributeValue == null )
			{
                attributeValue = "null";
			}
		}
		catch (AgentNotFoundException e)
		{
			JOptionPane.showMessageDialog(this,"Failed to find JMX agent");
		}
		catch (AttributeNotFoundException e)
		{
			attributeValue = "'"+attributeName+"' AttributeNotFoundException thrown";
		}
		catch (MBeanException e)
		{
			attributeValue = "'"+attributeName+"' MBeanException thrown";
		}
		catch (ReflectionException e)
		{
			attributeValue = "'"+attributeName+"' ReflectionException thrown";
		}
		catch (InstanceNotFoundException e)
		{
			attributeValue = "'"+attributeName+"' InstanceNotFoundException thrown";
		}
		catch (RuntimeOperationsException e)
		{
			attributeValue = "'"+attributeName+"' RuntimeOperationsException thrown";
		}

		return attributeValue;
	}
}

