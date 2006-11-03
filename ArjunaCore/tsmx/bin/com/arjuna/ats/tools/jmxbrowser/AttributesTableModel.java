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
 * $Id: AttributesTableModel.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.jmxbrowser;

import com.arjuna.ats.tsmx.TransactionServiceMX;

import javax.swing.table.DefaultTableModel;
import javax.management.*;

public class AttributesTableModel extends DefaultTableModel
{
	private final static String[] COLUMN_NAMES = { "Attribute Name", "Current Value" };
	public final static int NAME_COLUMN = 0, VALUE_COLUMN = 1;

	private final static String NOT_READABLE_ENTRY = "";

	private boolean[]				_attributeIsEditable = null;
	private MBeanAttributeInfo[]  	_attributesInfo;
	private boolean[]				_isNull = null;

	public String getAttributeType(int row)
	{
		return _attributesInfo[row].getType();
	}

	public boolean isNull(int row)
	{
		return _isNull[row];
	}

	/**
	 * This method is called when the attributes for a specific object
	 * are required.  This clears all current entries and replaces them
	 * with the attributes of this object.
	 *
	 * @param obj The object whos attributes are to be shown.
	 * @return True if the attributes were successfully queried and added
	 */
	public boolean setObjectName(ObjectName obj)
	{
		boolean returnValue = true;

		try
		{
        	MBeanInfo mbeanInfo = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getMBeanInfo( obj );

			_attributesInfo = mbeanInfo.getAttributes();

			this.setNumRows(_attributesInfo.length);

			/** Create isEditable array **/
			_attributeIsEditable = new boolean[_attributesInfo.length];
			_isNull = new boolean[_attributesInfo.length];

			for (int count=0;count<_attributesInfo.length;count++)
			{
				String attributeName = _attributesInfo[count].getName();
				Object attributeValue = NOT_READABLE_ENTRY;
				boolean noError = true;

				this.setValueAt(attributeName, count, NAME_COLUMN);

				if ( _attributesInfo[count].isReadable() )
				{
                    try
					{
						attributeValue = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(obj, attributeName);

						if ( attributeValue == null )
						{
							attributeValue = "null";
							_isNull[count] = true;
						}
						else
						{
							_isNull[count] = false;
						}
					}
					catch (AttributeNotFoundException e)
					{
						attributeValue = "'"+attributeName+"' AttributeNotFoundException thrown";
						noError = false;
					}
					catch (MBeanException e)
					{
						attributeValue = "'"+attributeName+"' MBeanException thrown";
						noError = false;
					}
					catch (ReflectionException e)
					{
						attributeValue = "'"+attributeName+"' ReflectionException thrown";
						noError = false;
					}
					catch (InstanceNotFoundException e)
					{
						attributeValue = "'"+attributeName+"' InstanceNotFoundException thrown";
						noError = false;
					}
					catch (RuntimeOperationsException e)
					{
						attributeValue = "'"+attributeName+"' RuntimeOperationsException thrown";
						noError = false;
					}

					this.setValueAt(attributeValue, count, VALUE_COLUMN );
				}

				_attributeIsEditable[count] = _attributesInfo[count].isWritable() && noError;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			returnValue = false;
		}

		return returnValue;
	}


	/**
	 * Returns the column name.
	 *
	 * @return a name for this column using the string value of the
	 * appropriate member in <code>columnIdentifiers</code>.
	 * If <code>columnIdentifiers</code> does not have an entry
	 * for this index, returns the default
	 * name provided by the superclass
	 */
	public String getColumnName(int column)
	{
        return COLUMN_NAMES[column];
	}

	/**
	 * Returns the number of columns in this data table.
	 * @return the number of columns in the model
	 */
	public int getColumnCount()
	{
		return COLUMN_NAMES.length;
	}

	/**
	 * Returns true regardless of parameter values.
	 *
	 * @param   row             the row whose value is to be queried
	 * @param   column          the column whose value is to be queried
	 * @return                  true
	 * @see #setValueAt
	 */
	public boolean isCellEditable(int row, int column)
	{
		return (column == VALUE_COLUMN) && (_attributeIsEditable[row]);
	}
}
