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
 * $Id: ToolPlugin.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.plugin;

import com.arjuna.ats.tools.toolsframework.panels.ATFSettingsPanel;

import javax.swing.*;
import java.util.Properties;

public abstract class ToolPlugin
{
	private JMenuBar		_menubar = null;
	private JDesktopPane	_desktop = null;
    private Icon			_icon16 = null;
	private Icon			_icon32 = null;

	public final void initialisePlugin(JMenuBar menubar, JDesktopPane desktop, String icon16, String icon32)
	{
		_menubar = menubar;
		_desktop = desktop;

		if ( icon16 != null )
			_icon16 = new ImageIcon( this.getClass().getResource(icon16) );
		if ( icon32 != null )
			_icon32 = new ImageIcon( this.getClass().getResource(icon32) );
	}

	/**
	 * Retrieve the 16x16 icon for this tool plugin.
	 * @return
	 */
	public final Icon getIcon16()
	{
		return _icon16;
	}

	/**
	 * Retrieve the 32x32 icon for this tool plugin.
	 * @return
	 */
	public final Icon getIcon32()
	{
		return _icon32;
	}

	/**
	 * Initialise the plugin, many activities should be performed during this method:-
	 *
	 *     + Add menus to the menu bar,
	 */
    public abstract void initialise(Properties properties) throws ToolPluginException;

	/**
	 * Retrieve the name of this plugin.
	 *
	 * @return The name of this plugin.
	 */
	public abstract String getName();


	/**
	 * This method is called when the framework is closing down.
	 *
	 * @throws ToolPluginException
	 */
	public abstract void dispose() throws ToolPluginException;

	/**
	 * Called by the framework when the user selects Settings from the menu.
	 * If this plugin doesn't require a settings pane then it should return null.
	 */
	public abstract ATFSettingsPanel createSettingsPanel();

	public final JDesktopPane getDesktop()
	{
		return _desktop;
	}

	/**
	 * Retrieve a reference to a named menu within the given menu bar.
	 *
	 * @param menubar The menu bar to search.
	 * @param name The menu to find.
	 * @return A reference to the named menu.
	 */
	public final static JMenu getMenu(JMenuBar menubar, String name)
	{
		for (int count=0;count<menubar.getMenuCount();count++)
		{
			JMenu menu = menubar.getMenu(count);

			if ( ( menu != null ) && ( menu.getText().equals(name) ) )
			{
				return menu;
			}
		}

		return null;
	}

	public final JMenuBar getMenuBar()
	{
		return _menubar;
	}

	public final JMenu getMenu(String name)
	{
		return getMenu(_menubar, name);
	}

	public final JMenu getFileMenu()
	{
		return getMenu(_menubar, "File");
	}

}
