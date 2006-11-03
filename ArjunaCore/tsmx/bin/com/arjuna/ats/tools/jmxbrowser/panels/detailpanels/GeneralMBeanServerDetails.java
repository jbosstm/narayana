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
 * $Id: GeneralMBeanServerDetails.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels.detailpanels;

import com.arjuna.ats.tools.jmxbrowser.panels.MBeanDetailsSupplier;

import javax.swing.*;
import java.awt.*;

public class GeneralMBeanServerDetails extends DetailPanel
{
	private JLabel					_numberOfMBeans;

	public GeneralMBeanServerDetails(MBeanDetailsSupplier supplier)
	{
		super(supplier);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);

		/** Add BOLD header label **/
		JLabel label = new JLabel("MBean Server Details");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets.top = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add mbean detail labels **/
		label = new JLabel("Number of MBeans: ");
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridy = 1;
		gbc.insets.top = 5;
		gbc.insets.bottom = 10;
		gbc.insets.left = 10;
		gbc.insets.right = 0;
		gbc.gridwidth = 1;
		gbl.setConstraints(label, gbc);
		this.add(label);

		_numberOfMBeans = new JLabel(""+supplier.getNumberOfMBeans());
		_numberOfMBeans.setFont(label.getFont().deriveFont(Font.BOLD));
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets.left = 0;
		gbc.insets.right = 10;
		gbc.gridwidth = 1;
		gbl.setConstraints(_numberOfMBeans, gbc);
		this.add(_numberOfMBeans);
	}

	public void updateDetails()
	{
		_numberOfMBeans.setText( "" + getDetailsSupplier().getNumberOfMBeans() );
	}
}
