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
 * $Id: ObjectNameViewer.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.stateviewers;

import com.arjuna.ats.tools.jmxbrowser.JMXObjectViewer;
import com.arjuna.ats.tools.jmxbrowser.dialogs.ViewAttributesAndMethodsFrame;

import javax.management.ObjectName;
import java.beans.PropertyVetoException;

public class ObjectNameViewer extends JMXObjectViewer
{
	/**
	 * Initialise this object viewer.
	 * @param object The object whose state is to be viewed.
	 */
	public void initialise(Object object)
	{
		ViewAttributesAndMethodsFrame frame = new ViewAttributesAndMethodsFrame((ObjectName)object);

		getDesktop().add(frame);
		try
		{
			frame.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			// Ignore
		}
		getDesktop().getDesktopManager().activateFrame(frame);
	}

	/**
	 * Commit the changes made to the objects state.
	 */
	public void commitChanges()
	{
		// Do nothing
	}

	/**
	 * Ignore the changes made to the objects state.
	 */
	public void ignoreChanges()
	{
		// Do nothing
	}

	/**
	 * Returns true if this object viewer is a panel if it returns false
	 * it is assumed that this viewer invokes some other Dialog/Frame.
	 *
	 * @return True if this object viewer is a panel
	 */
	public boolean isPanel()
	{
		return false; // This viewer isn't a panel
	}
}
