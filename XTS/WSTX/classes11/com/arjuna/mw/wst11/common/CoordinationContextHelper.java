/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.mw.wst11.common;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.w3c.dom.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.soap.SOAPElement;

/**
 * Helper class for serialising Coordination Contexts into SOAP headers.
 * @author kevin
 */
public class CoordinationContextHelper
{
   /**
     * Deserialise a coordination context from a DOM SOAP Header Element.
     * @param headerElement The SOAP header element to deserialise.
     * @return The coordination context.
     * @throws javax.xml.stream.XMLStreamException for errors during parsing.
     */
    public static CoordinationContextType deserialise(final Element headerElement)
        throws JAXBException
    {
        Thread current =Thread.currentThread();
        ClassLoader loader = current.getContextClassLoader();
        try {
            // use the XTS module loader so that JAXB can find its factory
            current.setContextClassLoader(CoordinationContextHelper.class.getClassLoader());
            JAXBContext jaxbContext = JAXBContext.newInstance("org.oasis_open.docs.ws_tx.wscoor._2006._06");
            Unmarshaller unmarshaller;
            unmarshaller = jaxbContext.createUnmarshaller();
            // the header element is a valid CoordinationContextType node so we can unpack it directly
            // using JAXB. n.b. we will see a mustUnderstand=1 in the otherAttributes which we probably don't
            // want but it will do no harm.
            CoordinationContextType coordinationContextType = unmarshaller.unmarshal(headerElement, CoordinationContextType.class).getValue();

            return coordinationContextType;
        } catch (JAXBException jaxbe) {
            return null;
        } finally {
            // restore the original  loader
            current.setContextClassLoader(loader);
        }
    }

    /**
      * Deserialise a coordination context from a DOM SOAP Header Element.
      * @param headerElement The SOAP header element to deserialise.
      * @return The coordination context.
      * @throws javax.xml.stream.XMLStreamException for errors during parsing.
      */
    public static void serialise(final CoordinationContextType  coordinationContextType, Element headerElement)
        throws JAXBException
    {
        // we would really like to just serialise the coordinationContextType direct. But the JAXB context will
        // only generate a Node and we need to add a SOAPHeaderElement. So, we cheat by serialising the
        // coordinationContextType into a header created by the caller, moving all its children into the
        // header element and then deleting it.
        Thread current =Thread.currentThread();
        ClassLoader loader = current.getContextClassLoader();
        try {
            // use the XTS module loader so that JAXB can find its factory
            current.setContextClassLoader(CoordinationContextHelper.class.getClassLoader());
            JAXBContext jaxbContext = JAXBContext.newInstance("org.oasis_open.docs.ws_tx.wscoor._2006._06");
            Marshaller marshaller;
            marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(coordinationContextType, headerElement);
            Node element = headerElement.getFirstChild();
            NamedNodeMap map = element.getAttributes();
            // we also need to copy namespace declarations into the parent
            int l = map.getLength();
            for (int i = 0; i < l; i++) {
                Attr attr = (Attr)map.item(i);
                if (attr.getPrefix().equals("xmlns")) {
                    headerElement.setAttribute(attr.getName(),attr.getValue());
                }
            }
            // copy the children
            NodeList children = element.getChildNodes();
            l = children.getLength();
            Node[] copy = new Node[l];
            // sigh. native creates a copy list while CXF gives us the actual child list so there is no simple way
            // to index into it from 0 to l which will work with both stacks. instead we copy the entries to an array
            // and use it to locate the element we want to delete and the append to the parent.
            for (int i = 0; i < l; i++) {
                copy[i] = children.item(i);
            }
            for (int i = 0; i < l; i++) {
                Node child = copy[i];
                element.removeChild(child);
                headerElement.appendChild(child);
            }
            headerElement.removeChild(element);
        } catch (JAXBException jaxbe) {
        } finally {
            // restore the original  loader
            current.setContextClassLoader(loader);
        }
    }
}