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
 * $Id: ViewMBeanOperations.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels;

import com.arjuna.ats.tsmx.TransactionServiceMX;
import com.arjuna.ats.tools.jmxbrowser.dialogs.InvokeMethodDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ViewMBeanOperations extends JPanel implements ActionListener, ListSelectionListener
{
	private final static String INVOKE_METHOD_BUTTON_TEXT = "Invoke";
	private final static String INVOKE_BUTTON_ICON_FILENAME = "invoke.gif";

	private ObjectName			_mbeanName;
	private JList				_methodList;
	private DefaultListModel    _methodListModel;
	private JButton				_invokeButton;

	public ViewMBeanOperations(ObjectName obj)
	{
		_mbeanName = obj;

		/** Setup layout **/
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);
		this.setBackground(Color.white);

		/** Create and add method label **/
		JLabel label = new JLabel("Methods:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Create and add method list **/
		_methodList = new JList(_methodListModel = createMethodListModel());
		_methodList.addListSelectionListener(this);
		gbc.gridy=1;
		gbc.insets.top = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		JScrollPane methodListScroller = new JScrollPane(_methodList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gbl.setConstraints(methodListScroller, gbc);
		this.add(methodListScroller);

		/** Create and add invoke method button **/
		_invokeButton = new JButton( INVOKE_METHOD_BUTTON_TEXT );
        _invokeButton.setIcon(new ImageIcon(ClassLoader.getSystemResource( INVOKE_BUTTON_ICON_FILENAME )));
		_invokeButton.addActionListener(this);
		_invokeButton.setEnabled(false);
		gbc.gridy=2;
		gbc.insets.bottom = 10;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(_invokeButton, gbc);
		this.add(_invokeButton);
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
        String actionCommand = e.getActionCommand();

		if ( actionCommand.equals( INVOKE_METHOD_BUTTON_TEXT ) )
		{
			/** Get the selected operation information **/
			OperationWrapper operation = (OperationWrapper)_methodList.getSelectedValue();
			MBeanOperationInfo opInfo = operation.getOperationInfo();

			/** Open invoke method dialog **/
			InvokeMethodDialog dlg = new InvokeMethodDialog((Frame)this.getTopLevelAncestor(), _mbeanName, opInfo);

		}
	}

	/**
	 * Called whenever the value of the selection changes.
	 * @param e the event that characterizes the change.
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		_invokeButton.setEnabled(true);
	}

	private DefaultListModel createMethodListModel()
	{
		DefaultListModel list = new DefaultListModel();

		try
		{
			MBeanInfo mbeanInfo = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getMBeanInfo(_mbeanName);

			MBeanOperationInfo[] operations = mbeanInfo.getOperations();
			for (int count=0;count<operations.length;count++)
			{
				list.addElement(new OperationWrapper(operations[count]));
			}
		}
		catch (Exception e)
		{
			throw new Error("Failed to create method list: "+e);
		}

		return list;
	}

	private class OperationWrapper
	{
		private MBeanOperationInfo	_operationInfo;

		public OperationWrapper(MBeanOperationInfo opInfo)
		{
			_operationInfo = opInfo;
		}

		public MBeanOperationInfo getOperationInfo()
		{
			return _operationInfo;
		}

		public String toString()
		{
			return generateOperationSignature(_operationInfo);
		}

		private String generateOperationSignature(MBeanOperationInfo opInfo)
		{
			String returnType = opInfo.getReturnType();
			String operationName = opInfo.getName();

			String elementToAdd = ( returnType != null ? returnType + " " : "" ) + operationName + "(";

			MBeanParameterInfo[] parameters = opInfo.getSignature();

			for (int count=0;count<parameters.length;count++)
			{
				elementToAdd += parameters[count].getType() +" "+ parameters[count].getName();

				if ( (count+1) < parameters.length )
				{
					elementToAdd += ", ";
				}
			}

			elementToAdd += ")";

			return elementToAdd;
		}

	}
}
