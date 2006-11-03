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
package com.arjuna.common.internal.util.propertyservice.plugins.io;

import com.arjuna.common.util.propertyservice.plugins.PropertyManagerIOPlugin;
import com.arjuna.common.util.propertyservice.propertycontainer.PropertyManagerPluginInterface;
import com.arjuna.common.util.exceptions.LoadPropertiesException;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.FileLocator;
import com.arjuna.common.util.exceptions.SavePropertiesException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XMLFilePlugin.java 2342 2006-03-30 13:06:17Z  $
 */

public class XMLFilePlugin implements PropertyManagerIOPlugin
{
	private final static String PROPERTIES_ELEMENT_NAME = "properties";
	private final static String DEPENDS_ATTRIBUTE_NAME = "depends";

	private final static String PROPERTY_ELEMENT_NAME = "property";
	private final static String NAME_ATTRIBUTE_NAME = "name";
	private final static String VALUE_ATTRIBUTE_NAME = "value";
	private final static String PROPERTY_TYPE_ATTRIBUTE_NAME = "type";

	private final static String SYSTEM_PROPERTY_TYPE_NAME = "system";

    private static DocumentBuilder _documentBuilder;

    /**
     * This hashtable is used to store the URI's of files already loaded.
     * The map is from PropertyManagerPluginInterface -> URI[]
     * A PropertyManagerPluginInterface can only load the properties from a
     * file once.
     */
    private static Hashtable    _loadedFiles = new Hashtable();

    private static void setURILoaded(PropertyManagerPluginInterface pcm, String uri)
    {
        ArrayList uris = (ArrayList)_loadedFiles.get(pcm);

        if ( uris == null )
        {
            uris = new ArrayList(1);
            _loadedFiles.put(pcm, uris);
        }

        if ( !uris.contains(uri) )
        {
            uris.add(uri);
        }
    }

    private static boolean isURILoaded(PropertyManagerPluginInterface pcm, String uri)
    {
        boolean loaded = false;

        ArrayList uris = (ArrayList)_loadedFiles.get(pcm);

        if ( uris != null )
        {
            loaded = uris.contains(uri);
        }

        return loaded;
    }

	/**
	 * This method loads the properties stored at the given <code>uri</code>.  The
	 * plugin uses the <code>PropertyManagerPluginInterface</code> to put the properties
	 * into the property manager.
	 *
	 * @param uri
	 * @param pcm
	 * @throws java.io.IOException
	 */
	public void load(String uri, PropertyManagerPluginInterface pcm, boolean verbose) throws LoadPropertiesException, IOException
	{
        /** Ensure we haven't loaded the URI before **/
        if ( !isURILoaded(pcm, uri) )
        {
            try
            {
                String filename = FileLocator.locateFile(uri);

				if ( filename != null && new File(filename).exists() )
				{
					Document doc = _documentBuilder.parse(filename);
					Element rootElement = doc.getDocumentElement();

					setURILoaded(pcm, uri);

					NodeList propertiesNodes = rootElement.getElementsByTagName(PROPERTIES_ELEMENT_NAME);

					Hashtable pmDependents = new Hashtable();
					Hashtable pms = new Hashtable();
					for (int count=0;count<propertiesNodes.getLength();count++)
					{
						Element propertiesNode = (Element)propertiesNodes.item(count);
                        Node nameNode = propertiesNode.getAttributeNode(NAME_ATTRIBUTE_NAME);
						String propertiesName = nameNode != null ? nameNode.getNodeValue() : null;
						Node dependsNode = propertiesNode.getAttributeNode(DEPENDS_ATTRIBUTE_NAME);
						String dependsName = dependsNode != null ? dependsNode.getNodeValue() : "";

						NodeList propertyNodes = propertiesNode.getElementsByTagName(PROPERTY_ELEMENT_NAME);

                        /** If the name is provided get the OM from the top level else just load into the current PM **/
						PropertyManagerPluginInterface pm = propertiesName != null ? pcm.getTopLevelPropertyManager().getChild(propertiesName) : pcm;

						/** If the child doesn't already exist create one **/
						if ( pm == null )
						{
							pm = pcm.createPropertyManager(propertiesName);
						}

						if ( verbose )
						{
							System.out.println("Properties loaded ("+uri+"):");
						}

						/** Associate the current URI with this property manager **/
						pm.setUri(uri);
						/** Associate this plugin with the property manager **/
						pm.setIOPluginClassname(this.getClass().getName());

						for (int nodeCount=0;nodeCount<propertyNodes.getLength();nodeCount++)
						{
							Element propertyNode = (Element)propertyNodes.item(nodeCount);
							String propertyName = propertyNode.getAttribute(NAME_ATTRIBUTE_NAME);
							String propertyValue = propertyNode.getAttribute(VALUE_ATTRIBUTE_NAME);
							String propertyType = propertyNode.getAttribute(PROPERTY_TYPE_ATTRIBUTE_NAME);

							if ( verbose )
							{
								System.out.println( propertyName +"="+ propertyValue );
							}

							/** Set the property but don't allow any system property to be overridden **/
							pm.setProperty(propertyName, propertyValue, false);

							/** If the property is specified as a System property and it's not already a system property **/
							if ( propertyType != null &&
							     propertyType.equalsIgnoreCase(SYSTEM_PROPERTY_TYPE_NAME) &&
								 System.getProperty(propertyName) == null )
							{
								/** Set this as a system property also **/
								System.setProperty(propertyName, propertyValue);
							}
						}

						/** If this has no dependents then it is a top-level module **/
						if ( dependsName.length() == 0 && pm != pcm)
						{
							pcm.addChild(pm);
						}

						pmDependents.put(pm, dependsName);

                        /** If these properties belond to a named set store them **/
                        if ( propertiesName != null )
                        {
						    pms.put(propertiesName, pm);
                        }
					}

					/** Ensure dependency tree is kept **/
					for (Enumeration e = pmDependents.keys();e.hasMoreElements();)
					{
						PropertyManagerPluginInterface pm = (PropertyManagerPluginInterface)e.nextElement();
						String depends = (String)pmDependents.get(pm);

						StringTokenizer strtok = new StringTokenizer(depends,",");

						while (strtok.hasMoreElements())
						{
							String dependantName = strtok.nextToken().trim();
							PropertyManager dependant = (PropertyManager)pms.get(dependantName);

                            if ( dependant == null )
                            {
                                throw new LoadPropertiesException("Dependency not found - property file invalid");
                            }
							pm.addParent(dependant);
						}
					}
				}
				else
				{
					throw new FileNotFoundException();
				}
            }
			catch (FileNotFoundException e)
			{
				if ( verbose )
				{
					System.err.println("Cannot load properties file: "+uri);
				}
			}
            catch (IOException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new LoadPropertiesException("Failed to open properties file: "+e);
            }
        }
	}

	/**
	 * This method saves the properties to the given <code>uri</code>.  The plugin
	 * uses the <code>PropertyManagerPluginInterface</code> to read the properties
	 * from the property manager.
	 *
	 * @param uri
	 * @param pcm
	 * @throws SavePropertiesException
	 * @throws IOException
	 */
	public void save(String uri, PropertyManagerPluginInterface pcm) throws SavePropertiesException, IOException
	{
        uri = uri != null ? null : pcm.getUri();

        if ( uri == null )
        {
            throw new SavePropertiesException("No uri is associated with this property manager");
        }

        Properties props = pcm.getLocalProperties();

        try
		{
			String filename = FileLocator.locateFile(uri);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename);
			Element root = doc.getDocumentElement();
			String depends = null;

			/** Remove the properties node for this property manager from the tree **/
            NodeList propertiesNodes = root.getElementsByTagName(PROPERTIES_ELEMENT_NAME);

			for (int count=0;count<propertiesNodes.getLength();count++)
			{
				Element propertiesElement = (Element)propertiesNodes.item(count);
				String propertiesName = propertiesElement.getAttributeNode(NAME_ATTRIBUTE_NAME).getNodeValue();

				/** If this is the properties node we are looking for remove it **/
				if ( propertiesName.equals(pcm.getName()) )
				{
					root.removeChild(propertiesElement);
					depends = propertiesElement.getAttributeNode(DEPENDS_ATTRIBUTE_NAME).getNodeValue();
					break;
				}
			}

			/** Now add the new properties node **/
			Element newPropertiesElement = doc.createElement(PROPERTIES_ELEMENT_NAME);

			newPropertiesElement.setAttribute(NAME_ATTRIBUTE_NAME, pcm.getName());

			if ( depends != null)
			{
				newPropertiesElement.setAttribute(DEPENDS_ATTRIBUTE_NAME, depends);
			}

			for (Enumeration e = props.keys();e.hasMoreElements();)
			{
				String key = (String)e.nextElement();
				String value = props.getProperty(key);

				Element newPropertyElement = doc.createElement(PROPERTY_ELEMENT_NAME);
				newPropertyElement.setAttribute(NAME_ATTRIBUTE_NAME, key);
				newPropertyElement.setAttribute(VALUE_ATTRIBUTE_NAME, value);

				newPropertiesElement.appendChild(newPropertyElement);
			}

			root.appendChild(newPropertiesElement);

			/** Output new properties file **/
			FileOutputStream out;
			OutputFormat of = new OutputFormat(doc);
			of.setIndenting(true);
        	XMLSerializer srl = new XMLSerializer(out = new FileOutputStream(filename), of);

			srl.serialize(doc);

			out.close();
		}
		catch (java.io.IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new SavePropertiesException("Unexpected exception: "+e);
		}
	}

    static
    {
        try
        {
            _documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError("Failed to create document builder:"+e);
        }
    }
}
