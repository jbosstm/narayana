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
 * $Id: InvokeMethodDialog.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.dialogs;

import com.arjuna.ats.tools.jmxbrowser.AttributeEditor;
import com.arjuna.ats.tools.jmxbrowser.ParameterEditor;
import com.arjuna.ats.tools.jmxbrowser.JMXBrowserPlugin;
import com.arjuna.ats.tools.jmxbrowser.JMXObjectViewer;
import com.arjuna.ats.tsmx.TransactionServiceMX;
import com.arjuna.ats.tsmx.agent.exceptions.AgentNotFoundException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.management.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class InvokeMethodDialog extends JDialog implements ActionListener
{
	private final static String INVOKE_METHOD_DIALOG_TITLE = "Invoke Operation";

	private final static String PARAMETER_NAME_COLUMN_NAME = "Parameter Name";
	private final static String PARAMETER_VALUE_COLUMN_NAME = "Value";

	private final static String INVOKE_BUTTON_TEXT = "Invoke";
	private final static String CANCEL_BUTTON_TEXT = "Cancel";

	private final static int NAME_COLUMN = 0;
	private final static int VALUE_COLUMN = 1;

	private JTable					_parametersTable;
	private ParametersTableModel   	_parameters;
	private ObjectName				_objectName;
	private MBeanOperationInfo		_operationInfo;

	public InvokeMethodDialog(Frame owner, ObjectName objectName, MBeanOperationInfo operationInfo)
	{
		super(owner, INVOKE_METHOD_DIALOG_TITLE + " " + objectName.getDomain()+"."+operationInfo.getName(), true);

		_objectName = objectName;
		_operationInfo = operationInfo;

		/** Setup frame **/
        GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.getContentPane().setLayout(gbl);
		this.getContentPane().setBackground(Color.white);

		/** Setup parameters label **/
		JLabel label = new JLabel("Parameters:");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(label, gbc);
		this.getContentPane().add(label);

		/** Setup parameters table **/
		_parametersTable = new JTable( _parameters = createParametersTableModel() );
		_parametersTable.setDefaultEditor(Object.class, new ParameterEditor(operationInfo.getSignature()));
		_parametersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scroller = new JScrollPane(_parametersTable);
		scroller.setPreferredSize(new Dimension(400,120));
        gbc.gridy = 1;
		gbc.insets.top = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(scroller, gbc);
		this.getContentPane().add(scroller);

		/** Setup buttons panel **/
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(Color.white);
		gbc.gridy = 2;
		gbc.insets.bottom = 10;

		/** Create and add Invoke button **/
        JButton invokeButton = new JButton( INVOKE_BUTTON_TEXT );
		invokeButton.addActionListener(this);
		invokeButton.setMnemonic(KeyEvent.VK_I);
		buttonPanel.add(invokeButton);

		/** Create and add Cancel button **/
        JButton cancelButton = new JButton( CANCEL_BUTTON_TEXT );
		cancelButton.addActionListener(this);
		cancelButton.setMnemonic(KeyEvent.VK_C);
		buttonPanel.add(cancelButton);

		gbl.setConstraints(buttonPanel, gbc);
		this.getContentPane().add(buttonPanel);

		pack();

		/** Place dialog in the middle its owner **/
		this.setLocation(owner.getX() + (owner.getWidth()/2) - (getWidth()/2), owner.getY() + ( owner.getHeight()/2) - (getHeight()/2));

		show();
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		/** Has the invoke button been pressed? **/
		if ( actionCommand.equals( INVOKE_BUTTON_TEXT ) )
		{
			int invalidRow = -1;

			/** Ensure all parameter values are filled **/
			for (int count=0;count<_parameters.getRowCount();count++)
			{
				if ( _parameters.getValueAt(count, VALUE_COLUMN) == null )
				{
					invalidRow = count;
				}
			}

			/** If not valid **/
			if ( invalidRow != -1 )
			{
				JOptionPane.showMessageDialog(this, "You have not entered a value for all the parameters", "Error", JOptionPane.ERROR_MESSAGE);

				_parametersTable.setEditingRow(invalidRow);
			}
			else
			{
				invokeMethod();
			}
		}

		if ( actionCommand.equals(CANCEL_BUTTON_TEXT) )
		{
			dispose();
		}
	}

	private void invokeMethod()
	{
		Object[] parameters = new Object[_operationInfo.getSignature().length];

		/** Retrieve parameters from table **/
		for (int count=0;count<parameters.length;count++)
		{
			parameters[count] = _parameters.getValueAt(count, VALUE_COLUMN);
		}

		/** Generate signature array **/
		MBeanParameterInfo[] mbpi =  _operationInfo.getSignature();
		String[] signature = new String[mbpi.length];

		for (int count=0;count<signature.length;count++)
		{
			signature[count] = mbpi[count].getType();
		}

		/** Invoke method **/
		try
		{
			Object result = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().invoke( _objectName, _operationInfo.getName(), parameters, signature );

			if ( result != null )
			{
				/** Try to show exception information in object viewer dialog **/
				String objectViewerClassname = JMXBrowserPlugin.getObjectViewer( result.getClass().getName() );

				if ( objectViewerClassname != null )
				{
					try
					{
						JMXObjectViewer viewerPanel = (JMXObjectViewer)Thread.currentThread().getContextClassLoader().loadClass( objectViewerClassname ).newInstance();

						/** Call framework level initialisation routine **/
						viewerPanel.initialiseViewer(JMXBrowserPlugin.getDesktopPane());

						/** Now call the JMXObjectViewer's initialisation routine **/
						viewerPanel.initialise(result);

						new ObjectViewerDialog( (Frame)this.getParent(), viewerPanel, result );
					}
					catch (Exception ex)
					{
						JOptionPane.showMessageDialog(this, "Failed to create object viewer to display exception information", "Object Viewer Error", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(this, "No viewer registered for the return type: "+result+" ("+result.getClass().getName()+")", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "The method returned: null");
			}
		}
		catch (javax.management.RuntimeMBeanException e)
		{
			/** Try to show exception information in object viewer dialog **/
			String objectViewerClassname = JMXBrowserPlugin.getObjectViewer("java.lang.Exception");

			if ( objectViewerClassname != null )
			{
				try
				{
					JMXObjectViewer viewerPanel = (JMXObjectViewer)Thread.currentThread().getContextClassLoader().loadClass( objectViewerClassname ).newInstance();

					/** Call framework level initialisation routine **/
					viewerPanel.initialiseViewer(JMXBrowserPlugin.getDesktopPane());

					/** Now call the JMXObjectViewer's initialisation routine **/
					viewerPanel.initialise(e.getTargetException());

					new ObjectViewerDialog( (Frame)this.getParent(), viewerPanel, e.getTargetException() );
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(this, "Failed to create object viewer to display exception information", "Object Viewer Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
			else
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Failed to open object viewer for mbean exception (no viewer registered)", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to interface to MBean server", "Error", JOptionPane.ERROR_MESSAGE);
		}


		dispose();
	}

	private ParametersTableModel createParametersTableModel()
	{
        MBeanParameterInfo[] parameters = _operationInfo.getSignature();
		ParametersTableModel tableModel = new ParametersTableModel(parameters.length,0);

		tableModel.addColumn( PARAMETER_NAME_COLUMN_NAME );
		tableModel.addColumn( PARAMETER_VALUE_COLUMN_NAME );

		for (int count=0;count<parameters.length;count++)
		{
			tableModel.setValueAt( "("+parameters[count].getType()+")"+parameters[count].getName(), count, NAME_COLUMN );
		}

		return tableModel;
	}

	private class ParametersTableModel extends DefaultTableModel
	{
		/**
		 *  Constructs a <code>DefaultTableModel</code> with
		 *  <code>rowCount</code> and <code>columnCount</code> of
		 *  <code>null</code> object values.
		 *
		 * @param rowCount           the number of rows the table holds
		 * @param columnCount        the number of columns the table holds
		 *
		 * @see #setValueAt
		 */
		public ParametersTableModel(int rowCount, int columnCount)
		{
			super(rowCount, columnCount);
		}

		/**
		 * Returns true regardless of parameter values.
		 *
		 * @param   row             the row whose value is to be queried
		 * @param   column          the column whose value is to be queried
		 * @return                  true
		 * @see #setValueAt
		 */
		public boolean isCellEditable(int row, int column)
		{
			return column == VALUE_COLUMN;
		}
	}
}
