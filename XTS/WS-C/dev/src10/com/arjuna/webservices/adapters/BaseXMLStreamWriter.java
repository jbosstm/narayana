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
package com.arjuna.webservices.adapters;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;

/**
 * The base XMLStreamWriter.
 * @author kevin
 * 
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_1 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_1] - setNamespaceContext unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_2 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_2] - writeComment unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_3 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_3] - writeProcessingInstruction unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_4 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_4] - writeProcessingInstruction unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_5 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_5] - writeEntityRef unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_6 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_6] - writeDTD unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_7 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_7] - writeStartDocument unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_8 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_8] - writeStartDocument unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_9 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_9] - writeStartDocument unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_10 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_10] - writeEndDocument unsupported
 * @message com.arjuna.webservices.adapters.BaseXMLStreamWriter_11 [com.arjuna.webservices.adapters.BaseXMLStreamWriter_11] - close unsupported
 */
public abstract class BaseXMLStreamWriter implements XMLStreamWriter
{
    /**
     * The current namespace context.
     */
    private NamespaceContextImpl currentNamespaceContext = new NamespaceContextImpl() ;

    /**
     * Get the uri associated with the prefix.
     * @param prefix The prefix.
     * @return The prefix URI.
     */
    public String getPrefix(final String prefix)
        throws XMLStreamException
    {
        return currentNamespaceContext.getPrefix(prefix) ;
    }

    /**
     * Set the prefix.
     * @param prefix The prefix.
     * @param uri The uri.
     */
    public void setPrefix(final String prefix, final String uri)
        throws XMLStreamException
    {
        currentNamespaceContext.setPrefix(prefix, uri) ;
    }

    /**
     * Set the default namespace.
     * @param uri The default namespace uri.
     */
    public void setDefaultNamespace(final String uri)
        throws XMLStreamException
    {
        currentNamespaceContext.setDefaultNamespace(uri) ;
    }

    /**
     * Get the current namespace context.
     * @return The namespace context.
     */
    public NamespaceContext getNamespaceContext()
    {
        return currentNamespaceContext ;
    }
    
    /**
     * Set the namespace context.
     * @param namespaceContext The namespace context.
     */
    public void setNamespaceContext(final NamespaceContext namespaceContext)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_1()) ;
    }
    
    /**
     * Push the current namespace context.
     */
    protected void pushNamespaceContext()
    {
        currentNamespaceContext = new NamespaceContextImpl(currentNamespaceContext) ;
    }
    
    /**
     * Pop the current namespace context.
     */
    protected void popNamespaceContext()
    {
        currentNamespaceContext = currentNamespaceContext.getParent() ;
    }
    
    /**
     * Write an XML comment to the stream.
     * @param comment The XML comment to write.
     */
    public void writeComment(final String comment)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_2()) ;
    }

    /**
     * Write a Processing Instruction to the stream.
     * @param target The Processing Instruction target.
     */
    public void writeProcessingInstruction(final String target)
            throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_3()) ;
    }

    /**
     * Write a Processing Instruction to the stream.
     * @param target The Processing Instruction target.
     * @param data The Processing Instruction data.
     */
    public void writeProcessingInstruction(final String target, final String data)
            throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_4()) ;
    }

    /**
     * Write an Entity Reference to the stream.
     * @param name The entity reference name.
     */
    public void writeEntityRef(final String name)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_5()) ;
    }

    /**
     * Write a DTD.
     * @param dtd The DTD.
     */
    public void writeDTD(final String dtd)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_6()) ;
    }

    /**
     * Write the XML encoding.
     */
    public void writeStartDocument()
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_7()) ;
    }

    /**
     * Write the XML encoding.
     * @param version The XML version.
     */
    public void writeStartDocument(final String version)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_8()) ;
    }

    /**
     * Write the XML encoding.
     * @param encoding The XML encoding.
     * @param version The XML version.
     */
    public void writeStartDocument(final String encoding, final String version)
            throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_9()) ;
    }

    /**
     * Write an end document.
     */
    public void writeEndDocument()
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_10()) ;
    }

    /**
     * Get the value of the specified property.
     * @param name The property name.
     * @return The property value.
     */
    public Object getProperty(final String name)
        throws IllegalArgumentException
    {
        return null ;
    }

    /**
     * Close the writer.
     */
    public void close()
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_adapters_BaseXMLStreamWriter_11()) ;
    }

    /**
     * Flush the writer.
     */
    public void flush()
        throws XMLStreamException
    {
        // Do nothing
    }

    /**
     * Normalise the namespace.
     * @param namespace The namespace.
     * @return The normalised namespace.
     */
    protected String normaliseNamespace(final String namespace)
    {
        if ((namespace == null) || (namespace.length() > 0))
        {
            return namespace ;
        }
        return null ;
    }
}
