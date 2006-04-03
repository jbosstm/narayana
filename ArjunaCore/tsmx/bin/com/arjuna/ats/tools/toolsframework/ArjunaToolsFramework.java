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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ArjunaToolsFramework.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginException;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginInformation;
import com.arjuna.ats.tools.toolsframework.dialogs.AboutDialog;
import com.arjuna.ats.tools.toolsframework.dialogs.SettingsDialog;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Properties;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.*;
import java.io.InputStream;
import java.io.File;
import java.beans.PropertyVetoException;

import org.w3c.dom.*;

public class ArjunaToolsFramework extends JFrame implements ActionListener, MenuListener
{
	private final static String CONFIGURATION_FILENAME = "toolsframework.xml";

	private final static String LOOK_AND_FEEL_NODE = "look-and-feel";
	private final static String TITLE_NODE = "title";
	private final static String FRAME_PROPERTIES_NODE = "frame-properties";
	private final static String PLUGIN_CONFIGURATIONS_NODE = "plugins";
	private final static String PROPERTIES_CONFIGURATION_NODE = "properties";
	private final static String PROPERTY_CONFIGURATION_NODE = "property";

	private final static String PROPERTY_NAME_ATTRIBUTE = "name";
	private final static String PROPERTY_VALUE_ATTRIBUTE = "value";
	private final static String PLUGIN_CLASSNAME_ATTRIBUTE = "classname";
	private final static String WIDTH_ATTRIBUTE = "width";
	private final static String HEIGHT_ATTRIBUTE = "height";

	private final static String DEFAULT_LIB_DIRECTORY = "lib";

	private final static String FILE_MENU = "File";
	private final static String SETTINGS_MENU_ITEM = "Settings";
	private final static String EXIT_MENU_ITEM = "Exit";

	private final static String HELP_MENU = "Help";
	private final static String ABOUT_MENU_ITEM = "About";

	private final static String WINDOW_MENU = "Window";
	private final static String CASCADE_MENU_ITEM = "Cascade Windows";

	private final static String ACTION_COMMAND_PREFIX = "WINDOW_ACTION_COMMAND_";

	private final static float CASCADE_WINDOW_PERCENTAGE = 0.75f;
	private final static int CASCADE_WINDOW_INCREMENT = 25;

    private static File	_libDirectory = new File(System.getProperty("com.arjuna.mw.ArjunaToolsFramework.lib", DEFAULT_LIB_DIRECTORY));

    public static File getLibDirectory()
    {
        return _libDirectory;
    }

	private ToolsClassLoader _classLoader = null;
	private ArrayList _plugins = new ArrayList();
	private JDesktopPane _desktop = null;
	private JMenuBar _menuBar = null;
	private boolean _hasSettings = false;
	private boolean _disposed = false;

	public ArjunaToolsFramework()
	{
		/** Create tools classloader **/
		_classLoader = new ToolsClassLoader(_libDirectory);

		/** Create JMenuBar **/
		this.setJMenuBar(_menuBar = createBasicMenuBar());

		/** Create the desktop **/
		this.setContentPane(_desktop = new JDesktopPane());

		/** Load plugins and configuration **/
		loadConfiguration();

		/** Add the File menu items **/
		addFileMenuItems(_menuBar);

		/** Add the Window menu **/
		addWindowMenu(_menuBar);

		/** Add the help menu **/
		addHelpMenu(_menuBar);

		/** Add window event handler **/
		this.addWindowListener(new WindowAdapter()
		{
			/**
			 * Invoked when a window has been closed.
			 */
			public void windowClosed(WindowEvent e)
			{
				windowClosing(e);
			}

			/**
			 * Invoked when a window is in the process of being closed.
			 * The close operation can be overridden at this point.
			 */
			public void windowClosing(WindowEvent e)
			{
				disposePlugins();
				System.exit(0);
			}
		});

		/** Display the frame **/
		show();

		/** Show the about dialog and allow to automatically close **/
		new AboutDialog(this, true);
	}

	public synchronized void disposePlugins()
	{
		if ( !_disposed )
		{
			_disposed = true; 
			for (int count = 0; count < _plugins.size(); count++)
			{
				try
				{
					ToolPlugin plugin = (ToolPlugin) _plugins.get(count);

					plugin.dispose();
				}
				catch (ToolPluginException e)
				{
					System.err.println("An error occurred while trying to dispose of the plugs: " + e);
				}	
			}
		}
	}

	private JMenuBar createBasicMenuBar()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu menu = null;

		menubar.add(menu = new JMenu(FILE_MENU));
		menu.setMnemonic(KeyEvent.VK_F);

		return menubar;
	}

	/**
	 * Invoked when a menu is selected.
	 *
	 * @param e  a MenuEvent object
	 */
	public void menuSelected(MenuEvent e)
	{
		JMenuItem item;
		JMenu menu = (JMenu) e.getSource();
		JInternalFrame[] frames = _desktop.getAllFrames();

		menu.removeAll();

		menu.add(item = new JMenuItem(CASCADE_MENU_ITEM));
		item.setMnemonic(KeyEvent.VK_C);
		item.addActionListener(this);

		menu.addSeparator();

		for (int count = 0; count < frames.length; count++)
		{
			JMenuItem menuItem;
			menu.add(menuItem = new JMenuItem((count + 1) + ". " + frames[count].getTitle()));
			menuItem.setActionCommand(ACTION_COMMAND_PREFIX + frames[count].getTitle());
			menuItem.addActionListener(this);
		}
	}

	/**
	 * Invoked when the menu is deselected.
	 *
	 * @param e  a MenuEvent object
	 */
	public void menuDeselected(MenuEvent e)
	{
	}

	/**
	 * Invoked when the menu is canceled.
	 *
	 * @param e  a MenuEvent object
	 */
	public void menuCanceled(MenuEvent e)
	{
	}

	private void addFileMenuItems(JMenuBar menuBar)
	{
		/** Find File menu **/
		JMenu menu = ToolPlugin.getMenu(menuBar, FILE_MENU);
		JMenuItem item;

		/** Add menu separator **/
		menu.addSeparator();

		/** Add settings menu item **/
		menu.add(item = new JMenuItem(SETTINGS_MENU_ITEM));
		item.setMnemonic(KeyEvent.VK_S);
		item.addActionListener(this);
		item.setEnabled( _hasSettings );

		/** Add exit menu item **/
		menu.add(item = new JMenuItem(EXIT_MENU_ITEM));
		item.setMnemonic(KeyEvent.VK_X);
		item.addActionListener(this);
	}

	private void addWindowMenu(JMenuBar menubar)
	{
		JMenu menu = null;

		menubar.add(Box.createHorizontalGlue());

		menubar.add(menu = new JMenu(WINDOW_MENU));
		menu.addMenuListener(this);
		menu.setMnemonic(KeyEvent.VK_W);
	}

	private void addHelpMenu(JMenuBar menubar)
	{
		JMenu menu = null;
		JMenuItem item = null;

		menubar.add(menu = new JMenu(HELP_MENU));
		menu.setMnemonic(KeyEvent.VK_H);

		menu.add(item = new JMenuItem(ABOUT_MENU_ITEM));
		item.setMnemonic(KeyEvent.VK_A);
		item.addActionListener(this);
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if (actionCommand.startsWith(ACTION_COMMAND_PREFIX))
		{
			JInternalFrame[] frames = _desktop.getAllFrames();

			for (int count = 0; count < frames.length; count++)
			{
				if (frames[count].getTitle().equals(actionCommand.substring(ACTION_COMMAND_PREFIX.length())))
				{
					try
					{
						frames[count].setSelected(true);
					}
					catch (PropertyVetoException ex)
					{
						// Ignore
					}
					_desktop.getDesktopManager().activateFrame(frames[count]);
				}
			}
		}
		else if (actionCommand.equals(EXIT_MENU_ITEM))
		{
			dispose();
		}
		else if (actionCommand.equals(ABOUT_MENU_ITEM))
		{
			showAboutDialog();
		}
		else if (actionCommand.equals(SETTINGS_MENU_ITEM))
		{
			showSettingsDialog();
		}
		else if (actionCommand.equals(CASCADE_MENU_ITEM))
		{
			cascadeWindows();
		}
	}

	private void cascadeWindows()
	{
		JInternalFrame[] frames = _desktop.getAllFrames();
		int offset = 0;

		for (int count = 0; count < frames.length; count++)
		{
			frames[count].setLocation(offset, offset);
			frames[count].setSize((int) (this.getWidth() * CASCADE_WINDOW_PERCENTAGE),
					(int) (this.getHeight() * CASCADE_WINDOW_PERCENTAGE));

			offset += CASCADE_WINDOW_INCREMENT;
		}
	}

	private void showSettingsDialog()
	{
		SettingsDialog dlg = new SettingsDialog(this, _plugins);

		int x = (int) this.getLocation().getX() + (this.getWidth() / 2) - (dlg.getWidth() / 2);
		int y = (int) this.getLocation().getY() + (this.getHeight() / 2) - (dlg.getHeight() / 2);

		dlg.setLocation(x, y);
	}

	private void showAboutDialog()
	{
		new AboutDialog(this, false);
	}

	/**
	 * Retrieve a specific named child node of the given node.
	 * @param configRoot The node whose children need seraching.
	 * @param nodeName The name of the node to find.
	 * @return The child node matching the node name.
	 */
	private Node getChildNode(Node configRoot, String nodeName)
	{
		NodeList nodeList = configRoot.getChildNodes();

		for (int count=0;count<nodeList.getLength();count++)
		{
			if ( nodeList.item(count).getNodeName().equals( nodeName ) )
			{
				return nodeList.item(count);
			}
		}

		return null;
	}

	private boolean loadConfiguration()
	{
		boolean success = true;

		try
		{
			ToolPlugin plugin;

			InputStream configStream = _classLoader.getResourceAsStream(CONFIGURATION_FILENAME);

			if (configStream == null)
			{
				System.err.println("Cannot find the configuration file '" + CONFIGURATION_FILENAME + "' in the tests/config directory");
				System.exit(1);
			}

			/** Parse the configuration document **/
			Document configDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configStream);
			Element configRoot = configDoc.getDocumentElement();

			/** Retrieve and parse the look-and-feel node **/
            Node lookAndFeelNode = getChildNode(configRoot, LOOK_AND_FEEL_NODE);

			if ( lookAndFeelNode != null )
			{
				/** Set the tool title **/
				String title = getChildNode(lookAndFeelNode, TITLE_NODE).getFirstChild().getNodeValue();

				if (title != null)
				{
					this.setTitle(title);
				}

				/** Set the frame properties **/
				Node frameProperties = getChildNode(lookAndFeelNode, FRAME_PROPERTIES_NODE);

				if ( frameProperties != null )
				{
					int width = Integer.parseInt( frameProperties.getAttributes().getNamedItem(WIDTH_ATTRIBUTE).getNodeValue() );
					int height = Integer.parseInt( frameProperties.getAttributes().getNamedItem(HEIGHT_ATTRIBUTE).getNodeValue() );

					this.setSize(width, height);
				}
			}

			/** Retrieve the plugin configuration nodes **/
            Node pluginConfigs = getChildNode(configRoot, PLUGIN_CONFIGURATIONS_NODE);

			/** Retrieve the tool plugin information classes for the JARs in the tool lib directory **/
			ToolPluginInformation[] plugins = _classLoader.getToolsInformation();

			for (int pluginCount=0;pluginCount<plugins.length;pluginCount++)
			{
				String[] pluginClassname = plugins[pluginCount].getClassnames();

				for (int classnameCount=0;classnameCount<pluginClassname.length;classnameCount++)
				{
					/** Instantiate plugin and add to list **/
					_plugins.add(plugin = (ToolPlugin) _classLoader.loadClass(pluginClassname[classnameCount]).newInstance());

					/** Call initialisers **/
					plugin.initialisePlugin(_menuBar, _desktop, plugins[pluginCount].getIcon16(), plugins[pluginCount].getIcon32());

					/** Retrieve the tool properties and then override any locally defined properties **/
					Properties toolProps = plugins[pluginCount].getProperties();
					Properties localProps = getLocalPluginProperties(pluginConfigs, pluginClassname[classnameCount]);

					if ( localProps != null )
					{
						toolProps.putAll( localProps );
					}

					plugin.initialise( toolProps );

					/** See if this plugin has a settings panel, if it does ensure we enable all setting functionality **/
					if ( plugin.createSettingsPanel() != null )
					{
						_hasSettings = true;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("An error occurred while initialising plugins: " + e);
			success = false;
		}

		return success;
	}

	/**
	 * Retrieve from the given XML node the local plugin properties for the given plugin.
	 *
	 * @param pluginConfigsNode
	 * @param pluginClassname
	 * @return
	 */
	private Properties getLocalPluginProperties(Node pluginConfigsNode, String pluginClassname)
	{
		if ( pluginConfigsNode != null )
		{
			Properties props = new Properties();
			NodeList pluginNodes = pluginConfigsNode.getChildNodes();

			for (int count=0;count<pluginNodes.getLength();count++)
			{
				Node pluginNode = pluginNodes.item(count);

				if ( pluginNode != null )
				{
					NamedNodeMap attrs = pluginNode.getAttributes();

					if ( attrs != null )
					{
						Node classNameNode = attrs.getNamedItem(PLUGIN_CLASSNAME_ATTRIBUTE);

						if ( ( classNameNode != null ) && ( classNameNode.getNodeValue().equals( pluginClassname ) ) )
						{
							Node propertiesNode = getChildNode(pluginNode, PROPERTIES_CONFIGURATION_NODE);

							NodeList propertyNodes = propertiesNode.getChildNodes();

							for (int propCount=0;propCount<propertyNodes.getLength();propCount++)
							{
								Node propertyNode = propertyNodes.item(propCount);

								if ( propertyNode.getNodeName().equals(PROPERTY_CONFIGURATION_NODE) )
								{
									String propName = propertyNode.getAttributes().getNamedItem(PROPERTY_NAME_ATTRIBUTE).getNodeValue();
									String propValue = propertyNode.getAttributes().getNamedItem(PROPERTY_VALUE_ATTRIBUTE).getNodeValue();

									props.put(propName, propValue);
								}
							}

							return props;
						}
					}
				}
			}
		}

		return null;
	}

	public static void main(String[] args)
	{
		new ArjunaToolsFramework();
	}
}
