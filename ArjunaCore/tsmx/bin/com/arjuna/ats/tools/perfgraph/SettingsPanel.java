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
 * $Id: SettingsPanel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.perfgraph;

import com.arjuna.ats.tools.toolsframework.panels.ATFSettingsPanel;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends ATFSettingsPanel
{
	private final static String TAB_TITLE = "Performance Graph";

	private final static String SAMPLE_INTERVAL_LABEL_TEXT = "Sample Interval (secs):";
	private final static String	NUMBER_OF_SAMPLES_LABEL_TEXT = "Number of Samples:";

	private final static int    DEFAULT_SAMPLE_INTERVAL = 5;
	private final static int	DEFAULT_NUMBER_OF_SAMPLES = 100;

	private static int _currentInterval = DEFAULT_SAMPLE_INTERVAL;
	private static int _currentNumberOfSamples = DEFAULT_NUMBER_OF_SAMPLES;

	private JTextField 	_interval;
	private JTextField	_numberOfSamples;

	public SettingsPanel()
	{
		/** Setup panel layout manager **/
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);

		/** Add and setup interval label **/
		JLabel label = new JLabel( SAMPLE_INTERVAL_LABEL_TEXT );
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add and setup interval textfield **/
    	_interval = new JTextField(8);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(_interval, gbc);
		this.add(_interval);

		/** Add and setup number of samples label **/
		label = new JLabel( NUMBER_OF_SAMPLES_LABEL_TEXT );
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(label, gbc);
		this.add(label);

		/** Add and setup number of samples textfield **/
		_numberOfSamples = new JTextField(8);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(_numberOfSamples, gbc);
		this.add(_numberOfSamples);

		_interval.setText(String.valueOf(_currentInterval));
		_numberOfSamples.setText(String.valueOf(_currentNumberOfSamples));
	}

	public String getTabTitle()
	{
		return TAB_TITLE;
	}

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
		try
		{
			Integer.parseInt( _interval.getText() );
			Integer.parseInt( _numberOfSamples.getText() );
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "The performance graph details are not valid");
			return false;
		}

		return true;
	}

	public void settingsConfirmed()
	{
		try
		{
			_currentInterval = Integer.parseInt( _interval.getText() );
			_currentNumberOfSamples = Integer.parseInt( _numberOfSamples.getText() );
		}
		catch (Exception e)
		{
			// Ignore
		}
	}

	public static int getInterval()
	{
		return _currentInterval;
	}

	public static int getNumberOfSamples()
	{
		return _currentNumberOfSamples;
	}
}
