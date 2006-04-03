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
 * $Id: MBeanDomainGroup.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels;

import com.arjuna.ats.tools.toolsframework.iconpanel.IconPanel;

import javax.swing.plaf.ComponentUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MBeanDomainGroup extends JPanel
{
	private static final Color BORDER_COLOR = Color.decode("#01B1DD");

	private String 		_domainName;
	private JLabel		_domainLabel;
	private IconPanel	_iconPanel;

	public MBeanDomainGroup(String domainName)
	{
		this.setBorder( BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createLineBorder(BORDER_COLOR)),BorderFactory.createEmptyBorder(10,10,10,10)));
		this.setBackground(Color.white);

		_domainName = domainName;

		/** Setup panel **/
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);

		/** Setup and add domain name label **/
		_domainLabel = new JLabel( domainName );
		_domainLabel.setFont(_domainLabel.getFont().deriveFont(Font.BOLD, 18));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(_domainLabel, gbc);
		this.add(_domainLabel);

		/** Setup and add icon panel **/
		_iconPanel = new IconPanel(800);
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(_iconPanel, gbc);
		this.add(_iconPanel);

		this.addComponentListener(new ComponentAdapter()
		{
			/**
			 * Invoked when the component's size changes.
			 */
			public void componentResized(ComponentEvent e)
			{
				super.componentResized(e);

/*				_iconPanel.layoutContainer();
				invalidate();*/
			}
		});
	}

	/**
	 * If the <code>preferredSize</code> has been set to a
	 * non-<code>null</code> value just returns it.
	 * If the UI delegate's <code>getPreferredSize</code>
	 * method returns a non <code>null</code> value then return that;
	 * otherwise defer to the component's layout manager.
	 *
	 * @return the value of the <code>preferredSize</code> property
	 * @see #setPreferredSize
	 * @see ComponentUI
	 */
	public Dimension getMinimumSize()
	{
		Dimension d = _iconPanel.getPreferredSize();
		d = new Dimension((int)d.getWidth() + 25, (int)( d.getHeight() * 4 ) + _domainLabel.getHeight());
		return d;
	}

	public IconPanel getIconPanel()
	{
		return _iconPanel;
	}

	public String getDomainName()
	{
		return _domainName;
	}

	public void clearSelection()
	{
		_iconPanel.clearSelection();
	}
}
