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
 * $Id: JMXBrowserPlugin.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginException;
import com.arjuna.ats.tools.toolsframework.panels.ATFSettingsPanel;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;

/**
 * Simple JMX browser allowing the user to invoke methods
 * and see the results.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: JMXBrowserPlugin.java 2342 2006-03-30 13:06:17Z  $
 */
public class JMXBrowserPlugin extends ToolPlugin implements ActionListener
{
	private final static String PLUGIN_NAME = "JMXBrowserPlugin";
	private final static String JMX_BROWSER_MENU_NAME = "JMX Browser";
	private final static String OPEN_BROWSER_MENU_ITEM = "Open JMX Browser";

	private static JDesktopPane _desktop = null;
	private static Hashtable	_objectViewers = new Hashtable();

	private JMXBrowserFrame		_browserFrame = null;
	private JMenuItem			_openBrowserMenuItem = null;

	/**
	 * Register an object viewer.  This maps the given object type to the object viewer classname.
	 *
	 * @param objectType The type of object this object viewer displays the state of.
	 * @param objectViewerClassname The object viewer classname.
	 */
	private static void registerObjectViewer(String objectType, String objectViewerClassname)
	{
		_objectViewers.put(objectType, objectViewerClassname);
	}

	public static String getObjectViewer(String objectType)
	{
		return (String)_objectViewers.get(objectType);
	}

	/**
	 * Initialise the plugin, many activities should be performed during this method:-
	 *
	 *     + Add menus to the menu bar,
	 */
	public void initialise(Properties props) throws ToolPluginException
	{
		_desktop = this.getDesktop();

		/** Create the JMX browser menu **/
		createMenu();

		/** Register exception viewer **/
		registerObjectViewer("java.lang.Exception", "com.arjuna.mwtools.jmxbrowser.stateviewers.ExceptionViewer");

		if ( props != null )
		{
			Enumeration propNames = props.propertyNames();

			/** Register all object viewers **/
			while ( propNames.hasMoreElements() )
			{
				String propName = (String)propNames.nextElement();

				registerObjectViewer(propName, (String)props.get(propName));
			}
		}
	}

	/**
	 * Create menu for JMX Browser
	 */
	private void createMenu()
	{
		JMenu menu = this.getFileMenu();

		_openBrowserMenuItem = new JMenuItem(OPEN_BROWSER_MENU_ITEM);
		_openBrowserMenuItem.setMnemonic(KeyEvent.VK_X);
		_openBrowserMenuItem.addActionListener(this);
		_openBrowserMenuItem.setIcon(getIcon16());
		menu.add(_openBrowserMenuItem);
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals( OPEN_BROWSER_MENU_ITEM ) )
		{
			if ( _browserFrame == null )
			{
				_browserFrame = new JMXBrowserFrame();
				_browserFrame.setFrameIcon(getIcon16());

				_browserFrame.addInternalFrameListener(new InternalFrameAdapter()
				{
					/**
					 * Invoked when an internal frame has been closed.
					 */
					public void internalFrameClosed(InternalFrameEvent e)
					{
						super.internalFrameClosed(e);

						_browserFrame = null;
						/** Enable menu item now the frame is closed **/
						_openBrowserMenuItem.setEnabled(true);
					}
				});

				/** Disable menu item till the frame is closed **/

				_openBrowserMenuItem.setEnabled(false);
				this.getDesktop().add(_browserFrame);
				this.getDesktop().moveToFront(_browserFrame);
			}
			else
			{
				this.getDesktop().moveToFront(_browserFrame);
			}
		}
	}

	/**
	 * Retrieve the name of this plugin.
	 *
	 * @return The name of this plugin.
	 */
	public String getName()
	{
		return PLUGIN_NAME;
	}

	/**
	 * This method is called when the framework is closing down.
	 *
	 * @throws ToolPluginException
	 */
	public void dispose() throws ToolPluginException
	{
	}

	/**
	 * Called by the framework when the user selects Settings from the menu.
	 * If this plugin doesn't require a settings pane then it should return null.
	 */
	public ATFSettingsPanel createSettingsPanel()
	{
		return null;
	}

	public static JDesktopPane getDesktopPane()
	{
		return _desktop;
	}
}
