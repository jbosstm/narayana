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
 * $Id: MBeanDetailsPanel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels;

import com.arjuna.ats.tools.jmxbrowser.panels.detailpanels.GeneralMBeanServerDetails;
import com.arjuna.ats.tools.jmxbrowser.panels.detailpanels.MBeanDetails;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MBeanDetailsPanel extends JPanel
{
	private final static int INTER_COMPONENT_GAP = 10;

	private MBeanDetailsSupplier		_supplier;

	private GeneralMBeanServerDetails  	_serverDetails;
	private MBeanDetails				_mbeanDetails;

	public MBeanDetailsPanel(MBeanDetailsSupplier supplier)
	{
		_supplier = supplier;

		/** Setup panel **/
		this.setBackground(Color.decode("#99C2FF"));

		/** Set layout manager to null, we'll manage the layout ourselves **/
		this.setLayout(null);

		/** Add panels **/
		_serverDetails = new GeneralMBeanServerDetails(_supplier);
		this.add(_serverDetails);

		_mbeanDetails = new MBeanDetails(_supplier);
		this.add(_mbeanDetails);

		/** Layout container **/
		layoutContainer();

		this.addComponentListener(new ComponentAdapter()
		{
			/**
			 * Invoked when the component's size changes.
			 */
			public void componentResized(ComponentEvent e)
			{
				super.componentResized(e);

				layoutContainer();
			}
		});
	}

	private void layoutContainer()
	{
		int maxX = Integer.MIN_VALUE;
		int y = INTER_COMPONENT_GAP;

		/** Setup Y positions **/
		for (int count=0;count<this.getComponentCount();count++)
		{
			Component c = this.getComponents()[count];

			c.setLocation(0, y);
			c.setSize(c.getPreferredSize());

			if ( c.getPreferredSize().getWidth() > maxX )
			{
				maxX = (int)c.getPreferredSize().getWidth();
			}
			y += (int)c.getPreferredSize().getHeight() + INTER_COMPONENT_GAP;
		}

		/** Setup X positions **/
		for (int count=0;count<this.getComponentCount();count++)
		{
			Component c = this.getComponents()[count];

			c.setSize(maxX, c.getHeight());
			c.setLocation((getWidth() / 2) - (maxX/2), (int)c.getLocation().getY());
		}

		Dimension size = new Dimension(maxX, y);
		setPreferredSize(size);
		setMinimumSize(size);
	}

	public void updateAll()
	{
		_serverDetails.updateDetails();
		_mbeanDetails.updateDetails();

		layoutContainer();
	}

	public void updateMBeanSpecific()
	{
        _mbeanDetails.updateDetails();

		layoutContainer();
	}
}
