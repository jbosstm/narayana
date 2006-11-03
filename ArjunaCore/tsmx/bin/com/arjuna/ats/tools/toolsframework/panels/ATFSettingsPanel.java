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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: ATFSettingsPanel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.panels;

import javax.swing.*;

public abstract class ATFSettingsPanel extends JPanel
{
	/**
	 * Called before the settings are confirmed so that
	 * this panel can ensure the data is valid before
	 * allowing it to be confirmed.  This method
	 * must display any error messages itself and block
	 * to allow the user to respond.
	 *
	 * @return True - if the settings are valid.
	 */
	public boolean validateSettings()
	{
		return true;
	}

	/**
	 * Called by the framework to confirm any changes
	 * made in this panel.  This method shouldn't fail
	 * due to any data integrity issues, there problems
	 * should be found during the validateSettings method.
	 */
	public abstract void settingsConfirmed();

	/**
	 * Called by the framework to abort any changes
	 * made in this panel.  Usually panels won't change
	 * any data until a confirmation is received but this
	 * method is called just in case.
	 */
	public void settingsAborted()
	{
		// Do nothing
	}

	public abstract String getTabTitle();
}
