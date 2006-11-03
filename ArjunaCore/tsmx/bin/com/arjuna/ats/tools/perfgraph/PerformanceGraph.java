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
 * $Id: PerformanceGraph.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.perfgraph;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.toolsframework.plugin.ToolPluginException;
import com.arjuna.ats.tools.toolsframework.panels.ATFSettingsPanel;
import com.arjuna.ats.tsmx.TransactionServiceMX;
import com.arjuna.ats.arjuna.common.Environment;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Properties;

public class PerformanceGraph extends ToolPlugin implements ActionListener
{
	private final static String PLUGIN_TITLE = "Performance Grapher";

	private final static String NEW_GRAPH_MENU_ITEM = "Open";
    private final static String CLOSE_ALL_MENU_ITEM = "Close All";

	private ArrayList	_localFrames = new ArrayList();

	/**
	 * Initialise the plugin, many activities should be performed during this method:-
	 *
	 *     + Add menus to the menu bar,
	 */
	public void initialise(Properties props) throws ToolPluginException
	{
		createMenu(getMenuBar());

		com.arjuna.ats.arjuna.common.arjPropertyManager.propertyManager.setProperty( Environment.ENABLE_STATISTICS, "YES" );
		TransactionServiceMX.getTransactionServiceMX();
	}

	/**
	 * Retrieve the name of this plugin.
	 *
	 * @return The name of this plugin.
	 */
	public String getName()
	{
		return PLUGIN_TITLE;
	}

	/**
	 * This method is called when the framework is closing down.
	 *
	 * @throws ToolPluginException
	 */
	public void dispose() throws ToolPluginException
	{
		closeAllGraphs();
	}

	/**
	 * Called by the framework when the user selects Settings from the menu.
	 * If this plugin doesn't require a settings pane then it should return null.
	 */
	public ATFSettingsPanel createSettingsPanel()
	{
		return new SettingsPanel();
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals( NEW_GRAPH_MENU_ITEM ) )
		{
			createGraphFrame();
		}
		else
		if ( actionCommand.equals( CLOSE_ALL_MENU_ITEM ) )
		{
			closeAllGraphs();
		}
	}

	private void closeAllGraphs()
	{
		for (int count=0;count<_localFrames.size();count++)
		{
			InternalGraphFrame igf = (InternalGraphFrame)_localFrames.get(count);

			igf.dispose();
		}
	}

	private void createGraphFrame()
	{
		InternalGraphFrame igf = new InternalGraphFrame();
		igf.setFrameIcon(getIcon16());
		igf.setVisible(true);
		_localFrames.add(igf);

		getDesktop().add( igf );
	}

	private void createMenu(JMenuBar menubar)
	{
		JMenu menu = null;
        JMenuItem item = null;

		menubar.add(menu = new JMenu("Performance"));
		menu.setMnemonic(KeyEvent.VK_P);

		menu.add(item = new JMenuItem(NEW_GRAPH_MENU_ITEM));
		item.setMnemonic(KeyEvent.VK_N);
		item.addActionListener(this);
		item.setIcon(getIcon16());
		menu.add(item = new JMenuItem(CLOSE_ALL_MENU_ITEM));
		item.setMnemonic(KeyEvent.VK_C);
		item.addActionListener(this);
	}
}
