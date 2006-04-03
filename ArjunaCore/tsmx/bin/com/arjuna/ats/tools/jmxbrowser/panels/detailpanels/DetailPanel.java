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
 * $Id: DetailPanel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.panels.detailpanels;

import com.arjuna.ats.tools.jmxbrowser.panels.MBeanDetailsSupplier;

import javax.swing.*;
import java.awt.*;

public abstract class DetailPanel extends JPanel
{
	protected final static Color  NORMAL_COLOR = Color.decode("#215DC6");
    protected final static Color  OVER_COLOR = Color.decode("#428EFF");
	protected final static Color  DOWN_COLOR = Color.red;

	private MBeanDetailsSupplier	_supplier;

	public DetailPanel(MBeanDetailsSupplier supplier)
	{
		_supplier = supplier;

		/** Setup panel **/
		this.setBackground(Color.decode("#C9DFFF"));
        this.setBorder(BorderFactory.createLineBorder(Color.white));
	}

	public MBeanDetailsSupplier getDetailsSupplier()
	{
		return _supplier;
	}

	public abstract void updateDetails();
}
