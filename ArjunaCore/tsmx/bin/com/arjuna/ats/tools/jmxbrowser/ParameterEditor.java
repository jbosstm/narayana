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
 * $Id: ParameterEditor.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser;

import com.arjuna.ats.tools.jmxbrowser.components.AttributeEditorComponent;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.management.MBeanParameterInfo;
import java.awt.*;
import java.util.EventObject;
import java.util.ArrayList;

public class ParameterEditor implements TableCellEditor
{
	private AttributeEditorComponent	_editor;
	private ArrayList					_listeners = new ArrayList();
    private MBeanParameterInfo[]		_parameters;

	public ParameterEditor(MBeanParameterInfo[] parameters)
	{
		_editor = new AttributeEditorComponent();
		_parameters = parameters;
	}

	/**
	 *  Sets an initial <code>value</code> for the editor.  This will cause
	 *  the editor to <code>stopEditing</code> and lose any partially
	 *  edited value if the editor is editing when this method is called. <p>
	 *
	 *  Returns the component that should be added to the client's
	 *  <code>Component</code> hierarchy.  Once installed in the client's
	 *  hierarchy this component will then be able to draw and receive
	 *  user input.
	 *
	 * @param	table		the <code>JTable</code> that is asking the
	 *				editor to edit; can be <code>null</code>
	 * @param	value		the value of the cell to be edited; it is
	 *				up to the specific editor to interpret
	 *				and draw the value.  For example, if value is
	 *				the string "true", it could be rendered as a
	 *				string or it could be rendered as a check
	 *				box that is checked.  <code>null</code>
	 *				is a valid value
	 * @param	isSelected	true if the cell is to be rendered with
	 *				highlighting
	 * @param	row     	the row of the cell being edited
	 * @param	column  	the column of the cell being edited
	 * @return	the component for editing
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
												 boolean isSelected,
												 int row, int column)
	{
		if ( value != null)
		{
			_editor.setObject(value);
		}
		else
		{
			_editor.clear();
		}

		_editor.setType(_parameters[row].getType());
		return _editor;
	}

	/**
	 * Returns the value contained in the editor.
	 * @return the value contained in the editor
	 */
	public Object getCellEditorValue()
	{
		try
		{
			return _editor.getObject();
		}
		catch (ClassCastException e)
		{

		}

		return null;
	}

	/**
	 * Asks the editor if it can start editing using <code>anEvent</code>.
	 * <code>anEvent</code> is in the invoking component coordinate system.
	 * The editor can not assume the Component returned by
	 * <code>getCellEditorComponent</code> is installed.  This method
	 * is intended for the use of client to avoid the cost of setting up
	 * and installing the editor component if editing is not possible.
	 * If editing can be started this method returns true.
	 *
	 * @param	anEvent		the event the editor should use to consider
	 *				whether to begin editing or not
	 * @return	true if editing can be started
	 * @see #shouldSelectCell
	 */
	public boolean isCellEditable(EventObject anEvent)
	{
		return true;
	}

	/**
	 * Returns true if the editing cell should be selected, false otherwise.
	 * Typically, the return value is true, because is most cases the editing
	 * cell should be selected.  However, it is useful to return false to
	 * keep the selection from changing for some types of edits.
	 * eg. A table that contains a column of check boxes, the user might
	 * want to be able to change those checkboxes without altering the
	 * selection.  (See Netscape Communicator for just such an example)
	 * Of course, it is up to the client of the editor to use the return
	 * value, but it doesn't need to if it doesn't want to.
	 *
	 * @param	anEvent		the event the editor should use to start
	 *				editing
	 * @return	true if the editor would like the editing cell to be selected;
	 *    otherwise returns false
	 * @see #isCellEditable
	 */
	public boolean shouldSelectCell(EventObject anEvent)
	{
		return true;
	}

	/**
	 * Tells the editor to stop editing and accept any partially edited
	 * value as the value of the editor.  The editor returns false if
	 * editing was not stopped; this is useful for editors that validate
	 * and can not accept invalid entries.
	 *
	 * @return	true if editing was stopped; false otherwise
	 */
	public boolean stopCellEditing()
	{
		for (int count=0;count<_listeners.size();count++)
		{
			CellEditorListener l = (CellEditorListener)_listeners.get(count);

			l.editingStopped( new ChangeEvent(this) );
		}

		try
		{
			_editor.getObject();
			return true;
		}
		catch (ClassCastException e)
		{
		}

		return false;
	}

	/**
	 * Tells the editor to cancel editing and not accept any partially
	 * edited value.
	 */
	public void cancelCellEditing()
	{
		for (int count=0;count<_listeners.size();count++)
		{
			CellEditorListener l = (CellEditorListener)_listeners.get(count);

			l.editingCanceled( new ChangeEvent(this) );
		}
	}

	/**
	 * Adds a listener to the list that's notified when the editor
	 * stops, or cancels editing.
	 *
	 * @param	l		the CellEditorListener
	 */
	public void addCellEditorListener(CellEditorListener l)
	{
		_listeners.add(l);
	}

	/**
	 * Removes a listener from the list that's notified
	 *
	 * @param	l		the CellEditorListener
	 */
	public void removeCellEditorListener(CellEditorListener l)
	{
		_listeners.remove(l);
	}
}
