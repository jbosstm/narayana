/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2008,
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
 * $Id: ObjectStoreBrowserPlugin.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.objectstorebrowser;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginException;
import com.arjuna.ats.tools.toolsframework.panels.ATFSettingsPanel;
import com.arjuna.ats.tools.toolsframework.ArjunaToolsFramework;
import com.arjuna.ats.tools.objectstorebrowser.frames.BrowserFrame;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.StateViewersRepository;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.*;
import com.arjuna.ats.tools.objectstorebrowser.rootprovider.providers.DefaultRootProvider;
import com.arjuna.ats.tools.objectstorebrowser.entityviewer.EntityViewerRepository;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Properties;
import java.io.File;
import java.beans.PropertyVetoException;

public class ObjectStoreBrowserPlugin extends ToolPlugin implements ActionListener
{
	private final static String BROWSE_OBJECT_STORE_MENU_ITEM = "Open Object Store Browser";
	private final static String PLUGIN_NAME = "ObjectStoreBrowser";

    private final static String ROOT_PROVIDER_PROPERTY = "com.arjuna.mwtools.objectstorebrowser.rootprovider";  // see tools.properties
    private final static String INITIALIZER_PROPERTY = "com.arjuna.mwtools.objectstorebrowser.initializer";   // see tools.properties
    private final static String ALT_INITIALIZER_PROPERTY = "com.arjuna.mwtools.objectstorebrowser.altinitializer";   // see tools.properties
    private final static String PLUGINS_DIRECTORY = "plugins";

	private static boolean	                _browserOpen = false;
    private static ObjectStoreRootProvider  _rootProvider = null;

	private BrowserFrame	        _browser = null;
	private JMenuItem		        _menuItem = null;
    private boolean                 _isJTS;

    /**
	 * Initialise the plugin, many activities should be performed during this method:-
	 *
	 *     + Add menus to the menu bar,
	 */
	public void initialise(Properties props) throws ToolPluginException
	{
        if (getToolsFramework().getToolsDir() == null)
            throw new ToolPluginException("ToolsFramework installation does not have a tools direcory");

        /** Create menu bar **/
		_menuItem = new JMenuItem( BROWSE_OBJECT_STORE_MENU_ITEM );
		_menuItem.setMnemonic(KeyEvent.VK_B);
		_menuItem.addActionListener(this);
		_menuItem.setIcon(getIcon16());

		this.getFileMenu().add(_menuItem);

        /** Setup the roots provider **/
        String rootProviderClass = props.getProperty(ROOT_PROVIDER_PROPERTY);

        /*
         * initialise the root provider before the plugins in case a plugin needs to update the root provider
         */
        if ( rootProviderClass != null )
        {
            try
            {
                _rootProvider = (ObjectStoreRootProvider)Class.forName(rootProviderClass).newInstance();
            }
            catch (Exception e)
            {
                throw new ToolPluginException("Failed to create object store root provider plugin:"+e);
            }
        }
        else
        {
            _rootProvider = new DefaultRootProvider();
        }

        if (!initialize(props, INITIALIZER_PROPERTY) && !initialize(props, ALT_INITIALIZER_PROPERTY))
            throw new ToolPluginException("Cannot locate plugin initializer for plugin " + getClass().getName());

        /** Initialise the state viewers repository **/
		StateViewersRepository.initialiseRepository(_isJTS, new File(getToolsFramework().getToolsDir().getFile(), PLUGINS_DIRECTORY));
        EntityViewerRepository.initialiseRepository(props);

        StateViewersRepository.setDefaultStateViewer(new DefaultStateViewer());
    }

    private boolean initialize(Properties props, String propName)
    {
        String cName = props.getProperty(propName);

        if (cName != null)
        {
            try
            {
                IToolInitializer toolInitializer = (IToolInitializer)Class.forName(cName).newInstance();
                toolInitializer.initialize(this);
                _isJTS = toolInitializer.isJTS();
                return true;
            }
            catch (Exception e)
            {
                System.out.println("Information: unable to locate initializer class " + e.getMessage());
            }
        }

        return false;
    }

    /**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		/** If the browse object store menu item was chosen **/
		if ( actionCommand.equals(BROWSE_OBJECT_STORE_MENU_ITEM) )
		{
			/** Disable the menu item **/
			_menuItem.setEnabled(false);

			/** Create browser frame and add close listener to reenable menu item **/
			_browser = new BrowserFrame();

            /** If a problem occurred while opening the frame **/
            if ( !_browser.isVisible() )
            {
                _menuItem.setEnabled(true);
                _browserOpen = false;
            }

			_browser.setFrameIcon(getIcon16());
			_browserOpen = true;

            _browser.addInternalFrameListener(new InternalFrameAdapter()
			{
				/**
				 * Invoked when an internal frame has been closed.
				 */
				public void internalFrameClosed(InternalFrameEvent e)
				{
					super.internalFrameClosed(e);

					_menuItem.setEnabled(true);
					_browserOpen = false;
				}
			});

			/** Add new frame to desktop **/
			this.getDesktop().add( _browser );
            this.getDesktop().getDesktopManager().maximizeFrame(_browser);
        }
	}

	public static boolean isBrowserOpen()
	{
		return _browserOpen;
	}

    /**
     * Get the currently configured root provider
     * @return
     */
    public static ObjectStoreRootProvider getRootProvider()
    {
        return _rootProvider;
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
        /*
        StateViewersRepository.disposeRepository();
        EntityViewerRepository.disposeRepository();
        */
    }

	/**
	 * Called by the framework when the user selects Settings from the menu.
	 * If this plugin doesn't require a settings pane then it should return null.
	 */
	public ATFSettingsPanel createSettingsPanel()
	{
		return null;
	}
}
