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
 * $Id: AutoCompleteTextField.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.toolsframework.components;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.*;

public class AutoCompleteTextField extends JTextField implements KeyListener, Runnable
{
	private String[]		_knownEntries;
	private String			_previousText;
	private int				_cachedMatch = -1;
    private Thread			_timer = null;
	private boolean			_textMustBeKnown = false;
    private boolean			_entryKnown = false;

	/**
	 * Constructs a new <code>TextField</code>.  A default model is created,
	 * the initial string is <code>null</code>,
	 * and the number of columns is set to 0.
	 */
	public AutoCompleteTextField(String[] knownEntries, boolean textMustBeKnown, int size)
	{
		super(size);

		this.addKeyListener(this);

		setKnownEntries(knownEntries);
		setTextMustBeKnown(textMustBeKnown);
		setForeground(Color.black);
	}

	public boolean isEntryKnown()
	{
		return _entryKnown;
	}

	public void setTextMustBeKnown(boolean textMustBeKnown)
	{
		_textMustBeKnown = textMustBeKnown;
	}

	public boolean getTextMustBeKnown()
	{
		return _textMustBeKnown;
	}

	public void setKnownEntries(String[] knownEntries)
	{
		_knownEntries = knownEntries;
	}

	/**
	 * Invoked when a key has been typed.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key typed event.
	 */
	public void keyTyped(KeyEvent e)
	{
		String currentText = getText();

		if ( !currentText.equals(_previousText) )
		{
			if ( _timer != null )
			{
				_timer.interrupt();
			}

			_timer = new Thread(this);
			_timer.setName("AutocompleteThread");
			_timer.start();
		}

		_previousText = currentText;
	}

	/**
	 * Invoked when a key has been pressed.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key pressed event.
	 */
	public void keyPressed(KeyEvent e)
	{
	}

	/**
	 * Invoked when a key has been released.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key released event.
	 */
	public void keyReleased(KeyEvent e)
	{
	}

	private String findMatch(String start)
	{
		if ( _cachedMatch != -1 )
		{
            if ( _knownEntries[_cachedMatch].startsWith(start) )
			{
				return _knownEntries[_cachedMatch];
			}
		}

		for (int count=0;count<_knownEntries.length;count++)
		{
			if ( ( _knownEntries[count].startsWith(start) ) && ( start.length() > 0 ) )
			{
				_cachedMatch = count;
				return _knownEntries[count];
			}
		}

		_cachedMatch = -1;
		return null;
	}

	public void run()
	{
		try
		{
			Thread.sleep(500);

			String currentText = getText();
			String match = findMatch(currentText);

			if ( match != null )
			{
				if ( getTextMustBeKnown() )
				{
					this.setForeground(Color.black);
					_entryKnown = true;
				}

				this.setText(match);
				this.setSelectionStart(currentText.length());
				this.setSelectionEnd(match.length());
			}
			else
			{
				if ( getTextMustBeKnown() )
				{
					this.setForeground(Color.red);
					_entryKnown = false;
				}
			}
		}
		catch (Exception e)
		{
			// Ignore
		}
	}

	public void addToKnownEntries(String[] runtimeIds)
	{
		if ( runtimeIds.length > 0 )
		{
			String[] oldEntries = _knownEntries;

			_knownEntries = new String[oldEntries.length + runtimeIds.length];
			System.arraycopy(oldEntries, 0, _knownEntries, 0, oldEntries.length);
			System.arraycopy(runtimeIds, 0, _knownEntries, oldEntries.length, runtimeIds.length);
		}
	}
}
