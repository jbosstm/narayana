/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wst11.common;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.w3c.dom.*;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

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
     * @throws JAXBException for errors during parsing.
     */
    public static CoordinationContextType deserialise(final Element headerElement)
        throws JAXBException
    {
        ClassLoader loader = getContextClassLoader();
        try {
            // use the XTS module loader so that JAXB can find its factory
            setContextClassLoader(CoordinationContextHelper.class.getClassLoader());
            JAXBContext jaxbContext = getJaxbContext();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            // the header element is a valid CoordinationContextType node so we can unpack it directly
            // using JAXB. n.b. we will see a mustUnderstand=1 in the otherAttributes which we probably don't
            // want but it will do no harm.
            CoordinationContextType coordinationContextType = unmarshaller.unmarshal(headerElement, CoordinationContextType.class).getValue();

            return coordinationContextType;
        } catch (Exception e) {
            return null;
        } finally {
            // restore the original  loader
            setContextClassLoader(loader);
        }
    }

    /**
      * Deserialise a coordination context from a DOM SOAP Header Element.
      * @param headerElement The SOAP header element to deserialise.
      * @throws JAXBException for errors during parsing.
      */
    public static void serialise(final CoordinationContextType  coordinationContextType, Element headerElement)
        throws JAXBException
    {
        // we would really like to just serialise the coordinationContextType direct. But the JAXB context will
        // only generate a Node and we need to add a SOAPHeaderElement. So, we cheat by serialising the
        // coordinationContextType into a header created by the caller, moving all its children into the
        // header element and then deleting it.
        ClassLoader loader = getContextClassLoader();
        try {
            // use the XTS module loader so that JAXB can find its factory
            setContextClassLoader(CoordinationContextHelper.class.getClassLoader());
            JAXBContext jaxbContext = getJaxbContext();
            Marshaller marshaller = jaxbContext.createMarshaller();
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
        } catch (Exception e) {
        } finally {
            // restore the original  loader
            setContextClassLoader(loader);
        }
    }
    
    private static void setContextClassLoader(final ClassLoader classLoader) {
        if (System.getSecurityManager() == null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    Thread.currentThread().setContextClassLoader(classLoader);
                    return null;
                }
            });
        }
    }

    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    private static JAXBContext getJaxbContext() throws Exception {
        final CoordinationContextAction coordinationContextAction = CoordinationContextAction.getInstance();

        if (System.getSecurityManager() == null) {
            return coordinationContextAction.run();
        }

        try {
            return AccessController.doPrivileged(coordinationContextAction);
        } catch (final PrivilegedActionException e) {
            throw e.getException();
        }
    }
}