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
 * $Id: AttributeEditorComponent.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser.components;

import com.arjuna.ats.tools.jmxbrowser.JMXBrowserPlugin;
import com.arjuna.ats.tools.jmxbrowser.JMXObjectViewer;
import com.arjuna.ats.tools.jmxbrowser.dialogs.ObjectViewerDialog;
import com.arjuna.ats.tools.jmxbrowser.dialogs.ObjectViewerCreationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class AttributeEditorComponent extends JPanel implements ActionListener
{
	private final static String[] PRIMITIVE_TYPES = { "int", "float", "double", "short", "long", "byte", "char", "boolean" };
	private final static String[] PRIMITIVE_TYPE_CLASSES = { "java.lang.Integer", "java.lang.Float", "java.lang.Double",
															 "java.lang.Short", "java.lang.Long", "java.lang.Byte",
															 "java.lang.Character", "java.lang.Boolean" };
	private	JTextField	_editorField;
	private JButton		_extendedEditorButton;
	private String		_objectType;
	private Object		_value = null;
	private Object		_initialValue = null;
	private	boolean 	_extendedEditorSet = false;

	public AttributeEditorComponent()
	{
        GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		this.setLayout(gbl);
        this.setBackground(Color.white);

		/** Create editor field **/
		_editorField = new JTextField();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(_editorField, gbc);
		this.add(_editorField);

		/** Create extended editor button **/
        _extendedEditorButton = new JButton("...");
		_extendedEditorButton.addActionListener(this);
		gbc.gridx = 2;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(_extendedEditorButton, gbc);
		this.add(_extendedEditorButton);
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String objectViewerClassname = JMXBrowserPlugin.getObjectViewer(_objectType);

		if ( objectViewerClassname != null )
		{
			try
			{
				JMXObjectViewer viewerPanel = (JMXObjectViewer)Thread.currentThread().getContextClassLoader().loadClass( objectViewerClassname ).newInstance();
		        Object obj = getObject();

				/** Call framework level initialisation routine **/
				viewerPanel.initialiseViewer(JMXBrowserPlugin.getDesktopPane());

				/** Now call the JMXObjectViewer's initialisation routine **/
				viewerPanel.initialise(obj);

				if ( viewerPanel.isPanel() )
				{
					ObjectViewerDialog ovd = new ObjectViewerDialog( (Frame)this.getTopLevelAncestor(), viewerPanel, obj );

					_extendedEditorSet = ovd.hasChanged();
				}

				invalidate();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "Failed to create object viewer", "Object Viewer Error", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}

	public Object getInitialValue()
	{
		return _initialValue;
	}

	public void setObject(Object text)
	{
		_initialValue = text;
		_value = text;
		_editorField.setText(text.toString());
	}

	public boolean valueHasChanged()
	{
		Object current = getObject();

		if ( _extendedEditorButton.isEnabled() )
		{
			return _extendedEditorSet;
		}

		return current.equals( _initialValue );
	}

	public Object getObject() throws ClassCastException
	{
		Object returnValue = null;

		int primIndex;

		/** If this editor represents a primitive type return its equivalent class **/
		if ( ( primIndex = getPrimitiveIndex(_objectType) ) != -1 )
		{
			try
			{
				Class primitiveClass = Thread.currentThread().getContextClassLoader().loadClass( PRIMITIVE_TYPE_CLASSES[primIndex] );
                Method valueOfMethod = primitiveClass.getMethod("valueOf", new Class[] { java.lang.String.class });
				return valueOfMethod.invoke(null, new Object[] { _editorField.getText() });
			}
			catch (Exception e)
			{
				e.printStackTrace();
				/** Indicate invalid input **/
				getToolkit().beep();

				throw new ClassCastException("Cannot cast to correct type");
			}
		}

		if ( _editorField.isEnabled() )
		{
			return _editorField.getText();
		}

		return _value;
	}

	private int getPrimitiveIndex(String type)
	{
		for (int count=0;count<PRIMITIVE_TYPES.length;count++)
		{
			if ( PRIMITIVE_TYPES[count].equals( type ) )
			{
				return count;
			}
		}

		return -1;
	}

	private boolean isPrimitive(String type)
	{
		return getPrimitiveIndex(type) != -1;
	}

	public void setType(String attributeType)
	{
		boolean enabled = false;

        _objectType = attributeType;

		try
		{
			String objectViewerClassname = JMXBrowserPlugin.getObjectViewer(_objectType);

			enabled = ( objectViewerClassname != null );
		}
		catch (Exception e)
		{
			System.err.println("Warning unknown attribute type: "+attributeType);
		}

		_extendedEditorButton.setEnabled(enabled);
		_editorField.setEnabled(!enabled);

		if ( ( !isPrimitive(_objectType) ) && ( !_objectType.equals("java.lang.String") ) )
		{
			_editorField.setEnabled(false);
		}
	}

	public void clear()
	{
		_editorField.setText("");
	}
}
