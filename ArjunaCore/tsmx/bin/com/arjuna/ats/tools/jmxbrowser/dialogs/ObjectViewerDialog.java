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
 * $Id: ObjectViewerDialog.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.dialogs;

import com.arjuna.ats.tools.jmxbrowser.JMXObjectViewer;
import com.arjuna.ats.tools.jmxbrowser.JMXBrowserPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ObjectViewerDialog extends JDialog implements ActionListener
{
	private final static String SET_BUTTON_TEXT = "set";
	private final static String CANCEL_BUTTON_TEXT = "cancel";

	private JMXObjectViewer	_viewerPanel;
    private boolean			_changed = false;

	public ObjectViewerDialog(Frame owner, JMXObjectViewer viewer, Object obj) throws ObjectViewerCreationException
	{
		super(owner, "Object Viewer", true);

		_viewerPanel = viewer;

		/** Setup dialog **/
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.getContentPane().setLayout(gbl);
		this.getContentPane().setBackground(Color.white);

		/** Create viewer panel and add to the container **/
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.ipadx = 10;
		gbc.ipady = 10;
		gbl.setConstraints(_viewerPanel, gbc);
		this.getContentPane().add(_viewerPanel);

		/** Create button panel and add to the container **/
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(Color.white);

		/** Create set button **/
		JButton setButton = new JButton(SET_BUTTON_TEXT);
		setButton.setMnemonic(KeyEvent.VK_S);
		setButton.addActionListener(this);
		buttonPanel.add(setButton);

		/** Create cancel button **/
		JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		gbc.gridy = 1;
		gbc.ipadx = 10;
		gbc.ipady = 10;
		gbl.setConstraints(buttonPanel, gbc);
		this.getContentPane().add(buttonPanel);

		pack();

		/** Place dialog in the middle its owner **/
		this.setLocation(owner.getX() + (owner.getWidth()/2) - (getWidth()/2), owner.getY() + ( owner.getHeight()/2) - (getHeight()/2));

		show();
	}

	public boolean hasChanged()
	{
		return _changed;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals( SET_BUTTON_TEXT ) )
		{
			_viewerPanel.commitChanges();
			_changed = true;
		}

		if ( actionCommand.equals( CANCEL_BUTTON_TEXT ) )
		{
			_viewerPanel.ignoreChanges();
		}

		dispose();
	}
}
