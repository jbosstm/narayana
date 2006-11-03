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
 * $Id: MergePropertyFiles.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mw.buildsystem.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;

/**
 * This ANT task allows an XML property file to be produced by merging property files into a
 * single property file.
 *
 * @author Richard A. Begg
 * @version $Id: MergePropertyFiles.java 2342 2006-03-30 13:06:17Z  $
 */
public class MergePropertyFiles extends Task
{
	public final static String PROPERTIES_ELEMENT_NAME = "properties";
	public final static String NAME_ATTRIBUTE_NAME = "name";
	public final static String DEPENDS_ATTRIBUTE_NAME = "depends";

	public final static String PROPERTY_ELEMENT_NAME = "property";
	public final static String VALUE_ATTRIBUTE_NAME = "value";

	private File	_master = null;
	private File	_merge = null;
	private String	_topLevelElementName = null;
	private boolean _override = false;

	public void setMaster(String filename)
	{
		_master = new File(filename);
	}

	public void setMerge(String filename)
	{
		_merge = new File(filename);
	}

	public void setTopLevelElement(String name)
	{
		_topLevelElementName = name;
	}

	public void setOverride(String text)
	{
		_override = new Boolean(text).booleanValue();
	}

	public void execute() throws BuildException
	{
    	if ( _master == null )
		{
			throw new BuildException("Please specify the master property file (master)");
		}

		if ( _merge == null || !_merge.exists() )
		{
			throw new BuildException("Please specify a valid property file to merge (merge)");
		}

		merge(_master, _merge);
	}

	private void merge(File master, File merge) throws BuildException
	{
		log("Merging '"+master.getName()+"' with '"+merge.getName()+"'");
		Document mergedDoc = mergeProperties(master, merge);

		try
		{
			log("Creating merged file '"+master.getName()+"'");

			FileOutputStream out;
			OutputFormat of = new OutputFormat(mergedDoc);
			of.setIndenting(true);
        	XMLSerializer srl = new XMLSerializer(out = new FileOutputStream(master), of);

			srl.serialize(mergedDoc);

			out.close();
		}
		catch (Exception e)
		{
			throw new BuildException("Unexpected exception while outputting merged file: "+e);
		}
	}

	public Document mergeProperties(File master, File merge) throws BuildException
	{
		Document masterDoc = null;
        Element masterDocRoot;

		try
		{
			Document mergeDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(merge);
			Element mergeDocRoot = mergeDoc.getDocumentElement();

			if ( master.exists() )
			{
				masterDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(master);
				masterDocRoot = masterDoc.getDocumentElement();
			}
			else
			{
				masterDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				masterDocRoot = masterDoc.createElement(_topLevelElementName != null ? _topLevelElementName : mergeDocRoot.getNodeName());
				masterDoc.appendChild(masterDocRoot);
			}

			NodeList mergePropertiesElements = mergeDocRoot.getElementsByTagName(PROPERTIES_ELEMENT_NAME);
			for (int count=0;count<mergePropertiesElements.getLength();count++)
			{
				Element mergePropertiesElement = (Element)mergePropertiesElements.item(count);
				String name = mergePropertiesElement.getAttributeNode(NAME_ATTRIBUTE_NAME).getNodeValue();

				/** Find the equivelant element in the master document **/
				Element masterPropertiesElement = getPropertiesElement(masterDocRoot, name);

				/** If we have been told to override then remove the previous settings **/
				if ( _override )
				{
					masterDocRoot.removeChild(masterPropertiesElement);
					masterPropertiesElement = null;
				}


				/** If this element doesn't appear in the master document add it **/
				if ( masterPropertiesElement == null )
				{
					Node newNode = masterDoc.importNode(mergePropertiesElement, true);
					masterDocRoot.appendChild(newNode);
				}
				else
				{
					NodeList mergePropertyElements = mergePropertiesElement.getChildNodes();

					for (int childCount=0;childCount<mergePropertyElements.getLength();childCount++)
					{
						Node childNode = mergePropertyElements.item(childCount);
						Node newNode = masterDoc.importNode(childNode, true);

						masterPropertiesElement.appendChild( newNode );
					}
				}
			}
		}
		catch (BuildException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new BuildException("Exception while merging properties file:"+e);
		}

		return masterDoc;
	}

	private Element getPropertiesElement(Element masterDocRoot, String findName)
	{
		NodeList masterPropertiesElements = masterDocRoot.getElementsByTagName(PROPERTIES_ELEMENT_NAME);
		for (int count=0;count<masterPropertiesElements.getLength();count++)
		{
			Element mergePropertiesElement = (Element)masterPropertiesElements.item(count);
			String name = mergePropertiesElement.getAttributeNode(NAME_ATTRIBUTE_NAME).getNodeValue();

			if ( name.equals(findName) )
			{
				return mergePropertiesElement;
			}
		}

		return null;
	}
}
