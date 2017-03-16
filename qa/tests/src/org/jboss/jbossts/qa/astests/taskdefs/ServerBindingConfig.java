/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.astests.taskdefs;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Helper class to read a JBoss AS bindings file
 */
public class ServerBindingConfig
{
    private static final String BINDING_MANAGER_NAME = "jboss.system:service=ServiceBindingManager";
    private static final String BINDING_MANAGER_CLASS = "org.jboss.services.binding.ServiceBindingManager";
    private static final String STORE_FACTORY_CLASS = "org.jboss.services.binding.XMLServicesStoreFactory";
    public static final String DEFAULT_BINDING = "ports-01";

    /**
     * Configure an AS to start up with a non-default set of bindings.
     *
     * An mbean entry will be inserted into xmlFile with a name specified by bindingName.
     * The file bindingXml will contain an entry with the name bindingName.
     * 
     * @param xmlFile   The file that is to recieve the binding service specification
     * @param bindingName The name of the binding definition to use
     * @param bindingXml file name of the file that contains a group of binding definitions
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws TransformerException
     */
    public static void setBinding(String xmlFile, String bindingName, String bindingXml) throws IOException, ParserConfigurationException,
            org.xml.sax.SAXException, TransformerException
    {
        String xmlFileName = Utils.toFile(xmlFile).getAbsolutePath();

        System.out.println("update bindings:");
        System.out.println("\tConf File: " + xmlFileName);
        System.out.println("\tBinding File: " + bindingXml);
        System.out.println("\tBinding Name: " + bindingName);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.getDeclaredConstructor().newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc1 = documentBuilder.parse(xmlFileName);
        Document doc2 = documentBuilder.newDocument();

        doc2.appendChild(doc2.createElement("server"));

        NodeList children = doc1.getDocumentElement().getChildNodes();
        Node server2 = doc2.getDocumentElement();

        // add an mbean entry for the server binding
        server2.appendChild(createBinding(doc2, bindingName, bindingXml));

        for (int i=0; i< children.getLength(); i++)
        {
            Node child = children.item(i);

            if ("mbean".equals(child.getNodeName()))
            {
                String name = ((Element) child).getAttribute("name");

                // ignore the original binding since we are creating a new one
                if (BINDING_MANAGER_NAME.equals(name))
                    continue;
            }

            // take a detached copy of the node and add it to the new document
            Node copy = doc2.importNode(child, true).cloneNode(true);

            server2.appendChild(copy);
        }

        printDocument(doc2, xmlFileName);
    }

    private static Element createBinding(Document document, String binding, String bindingXml)
    {
        Element mbean = document.createElement("mbean");

        mbean.setAttribute("code", BINDING_MANAGER_CLASS);
        mbean.setAttribute("name", BINDING_MANAGER_NAME);

        mbean.appendChild(createMbeanAttribute(document, "ServerName", binding));
        mbean.appendChild(createMbeanAttribute(document, "StoreURL", bindingXml));
        mbean.appendChild(createMbeanAttribute(document, "StoreFactoryClassName", STORE_FACTORY_CLASS));

        return mbean;
    }

    private static Element createMbeanAttribute(Document document, String name, String cdata)
    {
        Element element = document.createElement("attribute");

        element.setAttribute("name", name);
        element.setTextContent(cdata);

        return element;
    }

    public static void printDocument(Document document, String fname){
        try{

            TransformerFactory tf = TransformerFactory.getDeclaredConstructor().newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result;

            if (fname == null)
                result = new StreamResult(System.out);
            else
                result = new StreamResult(new FileWriter(fname));

            transformer.transform(source, result);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Document getDocument(String xmlFile)
    {
        Document doc = null;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.getDeclaredConstructor().newInstance();

        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);

        try
        {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(xmlFile);
        }
        catch (ParserConfigurationException e)
        {
            System.out.println("Unable to locate an XML doc builder: " + e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println("Error parsing " + xmlFile + ' ' + e.getMessage());
        }

        return doc;
    }

    public static Node findServiceConfig(String bindingsFile, String server, String service)
    {
        Document doc = getDocument(bindingsFile);

        if (doc == null)
            return null;

        Node node = findNode(doc.getDocumentElement(), "server", "name", server);

        return (node != null ? findNode(node, "service-config", "name", service) : null);
    }

    public static String findBindingPort(Node node)
    {
        node = findNode(node, "binding", "port", null);

        return (node != null ? ((Element) node).getAttribute("port") : null);
    }

    private static int parseInt(String intValue, int defValue, String errMsg)
    {
        try
        {
            return (intValue != null ? Integer.parseInt(intValue) : defValue);
        }
        catch (NumberFormatException e)
        {
            if (errMsg != null)
                System.out.println(errMsg);

            return defValue;
        }
    }

    public static int lookupHttpPort(String bindingsFile, String server, int defValue)
    {
        Node node = findServiceConfig(bindingsFile, server, "jboss:service=invoker,type=http");
        String port = findBindingPort(node);

        return parseInt(port, defValue, "Invalid port found in service-config for binding " + server);
    }

    public static int lookupRmiPort(String bindingsFile, String server, int defValue)
    {
        Node node = findServiceConfig(bindingsFile, server, "jboss:service=Naming");
        String port = findBindingPort(node);

        return parseInt(port, defValue, "Invalid port found in service-config for binding " + server);
    }

    /**
     * Find a node with a given name and having a specified attribute
     * @param node the node to start the search from
     * @param nodeName the name of the xml node to look for the attribute
     * @param attrName the name of the attribute to match against
     * @param attrValue the value of the attribute to match against. If null
     *  the first matching node with an attribute called attrName is returned
     * @return the matching node
     */
    private static Node findNode(Node node, String nodeName, String attrName, String attrValue)
    {
        if (node != null)
        {
            NodeList children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++)
            {
                Node child = children.item(i);

                if (nodeName.equals(child.getNodeName()))
                {
                    // found a node of the correct type - now see if it has the given attribute
                    String value = ((Element) child).getAttribute(attrName);

                    if (attrName == null || attrValue == null || value.equals(attrValue))
                        return child;
                }
            }

            System.out.println("Node of type " + nodeName + " with " + attrName + '=' + attrValue + " not found");
        }

        return null;
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException,
            org.xml.sax.SAXException, TransformerException
    {
        if (args.length >= 3) {
            setBinding(args[0], args[1], args[2]);
        } else if (args.length == 2) {
            System.out.println("Rmi port: " + lookupRmiPort(args[0], args[1], -1));
            System.out.println("Http port: " + lookupHttpPort(args[0], args[1], -1));
        } else {
            System.out.println("syntax: ServerBindingConfig <conf file path> <server binding name> <path of bindings file>");
        }
    }
}
