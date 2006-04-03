/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * $Id: MBeanDetails.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels.detailpanels;

import com.arjuna.ats.tools.jmxbrowser.panels.MBeanDetailsSupplier;
import com.arjuna.ats.tools.toolsframework.components.TextButton;
import com.arjuna.ats.tools.toolsframework.iconpanel.IconImage;
import com.arjuna.ats.tools.jmxbrowser.dialogs.ViewAttributesAndMethodsFrame;
import com.arjuna.ats.tools.jmxbrowser.JMXBrowserPlugin;

import javax.swing.*;
import javax.management.MBeanInfo;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class MBeanDetails extends DetailPanel implements ActionListener
{
	private final static String VIEW_BUTTON_TEXT = "View";

	private IconImage	_iconImage;
	private JLabel		_numberOfAttributes;
	private JLabel		_numberOfOperations;
	private JLabel		_numberOfConstructors;
	private JLabel		_numberOfNotifications;
	private JTextArea	_description;
	private ArrayList	_hideableComponents = new ArrayList();

	public MBeanDetails(MBeanDetailsSupplier supplier)
	{
		super(supplier);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);

		/** Add BOLD header label **/
		JLabel label = new JLabel("MBean Details");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.bottom = 10;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add icon image**/
		_iconImage = new IconImage("mbean-icon.gif");
		_iconImage.setSelected(true);
		_hideableComponents.add(_iconImage);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.bottom = 10;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbl.setConstraints(_iconImage, gbc);
		this.add(_iconImage);

		/** Add number of constructors label **/
		label = new JLabel("Number of Constructors: ");
		_hideableComponents.add(label);
		label.setVisible(false);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.insets.top = 5;
		gbc.insets.right = 0;
		gbc.insets.bottom = 0;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add number of constructors**/
		_numberOfConstructors = new JLabel("xxx");
		_hideableComponents.add(_numberOfConstructors);
		_numberOfConstructors.setFont(label.getFont().deriveFont(Font.BOLD));
		_numberOfConstructors.setVisible(false);
		gbc.gridx = 1;
		gbc.insets.left = 0;
		gbc.insets.right = 10;
		gbl.setConstraints(_numberOfConstructors, gbc);
		this.add(_numberOfConstructors);

		/** Add number of attributes label **/
		label = new JLabel("Number of Attributes: ");
		_hideableComponents.add(label);
		label.setVisible(false);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.insets.top = 0;
		gbc.insets.left = 10;
		gbc.insets.right = 0;
		gbc.insets.bottom = 0;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add number of attributes **/
		_numberOfAttributes = new JLabel("xxx");
		_hideableComponents.add(_numberOfAttributes);
		_numberOfAttributes.setFont(label.getFont().deriveFont(Font.BOLD));
		_numberOfAttributes.setVisible(false);
		gbc.gridx = 1;
		gbc.insets.left = 0;
		gbc.insets.right = 10;
		gbl.setConstraints(_numberOfAttributes, gbc);
		this.add(_numberOfAttributes);

		/** Add number of operations label **/
		label = new JLabel("Number of Operations: ");
		_hideableComponents.add(label);
		label.setVisible(false);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.insets.top = 0;
		gbc.insets.left = 10;
		gbc.insets.right = 0;
		gbc.insets.bottom = 0;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add number of operations **/
		_numberOfOperations = new JLabel("xxx");
		_hideableComponents.add(_numberOfOperations);
		_numberOfOperations.setFont(label.getFont().deriveFont(Font.BOLD));
		_numberOfOperations.setVisible(false);
		gbc.gridx = 1;
		gbc.insets.left = 0;
		gbc.insets.right = 10;
		gbl.setConstraints(_numberOfOperations, gbc);
		this.add(_numberOfOperations);

		/** Add number of notifications label **/
		label = new JLabel("Number of Notifications: ");
		_hideableComponents.add(label);
		label.setVisible(false);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.insets.top = 0;
		gbc.insets.left = 10;
		gbc.insets.right = 0;
		gbc.insets.bottom = 10;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add number of notifications **/
		_numberOfNotifications = new JLabel("xxx");
		_hideableComponents.add(_numberOfNotifications);
		_numberOfNotifications.setFont(label.getFont().deriveFont(Font.BOLD));
		_numberOfNotifications.setVisible(false);
		gbc.gridx = 1;
		gbc.insets.left = 0;
		gbc.insets.right = 10;
		gbl.setConstraints(_numberOfNotifications, gbc);
		this.add(_numberOfNotifications);

		/** Add description label **/
		label = new JLabel("Description: ");
		_hideableComponents.add(label);
		label.setVisible(false);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.insets.top = 5;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.bottom = 0;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add description text area **/
		_description = new JTextArea();
		_description.setLineWrap(true);
		_description.setWrapStyleWord(true);
		_description.setMaximumSize(new Dimension(75,50));
		_description.setPreferredSize(new Dimension(75,50));
		_description.setVisible(false);
		_description.setEditable(false);
		_description.setBackground(this.getBackground());
		_description.setFont(_description.getFont().deriveFont(Font.BOLD));
		_hideableComponents.add(_description);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.insets.top = 0;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.bottom = 0;
		gbl.setConstraints(_description, gbc);
		this.add(_description);

		/** Add View attributes and methods text button **/
		TextButton viewButton = new TextButton(VIEW_BUTTON_TEXT, DetailPanel.NORMAL_COLOR, DetailPanel.OVER_COLOR, DetailPanel.DOWN_COLOR);
		_hideableComponents.add(viewButton);
		viewButton.setVisible(false);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 1;
		gbc.insets.top = 0;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.bottom = 10;
		gbl.setConstraints(viewButton, gbc);
		this.add(viewButton);

		viewButton.addActionListener(this);
	}

	public void updateDetails()
	{
		MBeanInfo mbeanInfo = getDetailsSupplier().getSelectedMBean();

		/** If an mbean has been selected **/
		if ( mbeanInfo != null )
		{
            /** Unhide all components **/
			for (int count=0;count<_hideableComponents.size();count++)
			{
				Component c = (Component)_hideableComponents.get(count);
				c.setVisible(true);
			}

			_numberOfConstructors.setText( ""+mbeanInfo.getConstructors().length );
			_numberOfAttributes.setText( ""+mbeanInfo.getAttributes().length );
			_numberOfOperations.setText( ""+mbeanInfo.getOperations().length );
			_numberOfNotifications.setText( ""+mbeanInfo.getNotifications().length );
			_iconImage.setImage(getDetailsSupplier().getSelectedMBeanIconImage().getImage());
			_description.setText(mbeanInfo.getDescription());
			_description.setColumns(10);
			_description.setRows(5);
		}
		else
		{
			/** Hide all components **/
			for (int count=0;count<_hideableComponents.size();count++)
			{
				Component c = (Component)_hideableComponents.get(count);
				c.setVisible(false);
			}
		}
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals( VIEW_BUTTON_TEXT ) )
		{
			ViewAttributesAndMethodsFrame frame = new ViewAttributesAndMethodsFrame(getDetailsSupplier().getSelectedMBeanName());

			JMXBrowserPlugin.getDesktopPane().add(frame);
			JMXBrowserPlugin.getDesktopPane().moveToFront(frame);
		}
	}
}
