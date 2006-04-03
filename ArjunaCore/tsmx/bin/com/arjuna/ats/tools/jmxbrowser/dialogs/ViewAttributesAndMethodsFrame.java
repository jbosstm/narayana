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
 * $Id: ViewAttributesAndMethodsFrame.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.dialogs;

import com.arjuna.ats.tools.jmxbrowser.JMXBrowserPlugin;
import com.arjuna.ats.tools.jmxbrowser.panels.ViewMBeanAttributes;
import com.arjuna.ats.tools.jmxbrowser.panels.ViewMBeanOperations;

import javax.management.ObjectName;
import javax.swing.*;
import java.awt.*;

public class ViewAttributesAndMethodsFrame extends JInternalFrame
{
	private final static String FRAME_TITLE = "View JMX Attributes and Operations";

	private ViewMBeanAttributes 	_attributesPanel;
	private ViewMBeanOperations		_methodsPanel;

	public ViewAttributesAndMethodsFrame(ObjectName obj)
	{
		super( FRAME_TITLE + " '"+obj.toString()+"'", true, true, true, true );

		/** Setup frame **/
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.getContentPane().setLayout(gbl);

        /** Create attributes panel **/
		_attributesPanel = new ViewMBeanAttributes(obj);
		_attributesPanel.setPreferredSize(new Dimension(500,200));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(_attributesPanel, gbc);
		this.getContentPane().add(_attributesPanel);

		/** Create operations panel **/
		_methodsPanel = new ViewMBeanOperations(obj);
		_methodsPanel.setPreferredSize(new Dimension(500,200));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(_methodsPanel, gbc);
		this.getContentPane().add(_methodsPanel);

		pack();
		setVisible(true);

		JMXBrowserPlugin.getDesktopPane().moveToFront(this);
	}
}
