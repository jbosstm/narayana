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
 * $Id: SettingsDialog.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.dialogs;

import com.arjuna.ats.tools.toolsframework.plugin.ToolPlugin;
import com.arjuna.ats.tools.toolsframework.panels.ATFSettingsPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SettingsDialog extends JDialog implements ActionListener
{
	private final static String DIALOG_TITLE = "Settings";

	private JTabbedPane 		_tabPane = null;
	private SettingsButtonPanel _buttonPanel = null;

	public SettingsDialog(Frame owner, ArrayList	plugins)
	{
		super(owner, DIALOG_TITLE, true);

		/** Set dialog layout **/
		this.getContentPane().setLayout(new BorderLayout());
		this.setSize(400,300);

		/** Add tabbed panel and button panel **/
    	this.getContentPane().add(_tabPane = new JTabbedPane(), BorderLayout.CENTER);
    	this.getContentPane().add(_buttonPanel = new SettingsButtonPanel(this), BorderLayout.SOUTH);

		for (int count=0;count<plugins.size();count++)
		{
			ToolPlugin plugin = (ToolPlugin)plugins.get(count);

			ATFSettingsPanel panel = plugin.createSettingsPanel();

			/** If a panel is defined then add it to the tabbed pane **/
			if ( panel != null )
			{
				_tabPane.addTab(panel.getTabTitle(), panel);
			}
		}

		show();
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals("Apply") )
		{
			boolean success = true;
			for (int count=0;count<_tabPane.getTabCount();count++)
			{
				ATFSettingsPanel panel = (ATFSettingsPanel)_tabPane.getComponentAt(count);

				success &= panel.validateSettings();
			}

			if ( success )
			{
				for (int count=0;count<_tabPane.getTabCount();count++)
				{
					ATFSettingsPanel panel = (ATFSettingsPanel)_tabPane.getComponentAt(count);

					panel.settingsConfirmed();
				}

				dispose();
			}
		}
		else
		if ( actionCommand.equals("Cancel") )
		{
			for (int count=0;count<_tabPane.getTabCount();count++)
			{
				ATFSettingsPanel panel = (ATFSettingsPanel)_tabPane.getComponentAt(count);

				panel.settingsAborted();
			}

			dispose();
		}
	}

	private class SettingsButtonPanel extends JPanel
	{
		private JButton	_apply = null;
		private JButton	_cancel = null;

		public SettingsButtonPanel(ActionListener listener)
		{
			this.setLayout(new GridLayout(2, 1));
			this.add(_apply = new JButton("Apply"));
			_apply.setMnemonic(KeyEvent.VK_A);
			_apply.addActionListener(listener);

			this.add(_cancel = new JButton("Cancel"));
			_cancel.setMnemonic(KeyEvent.VK_C);
			_cancel.addActionListener(listener);
		}
	}
}
