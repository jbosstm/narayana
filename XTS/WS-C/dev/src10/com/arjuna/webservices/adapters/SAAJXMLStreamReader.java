/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.adapters;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.Text;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.logging.WSCLogger;

/**
 * XMLStreamReader for reading from a SAAJ element.
 * @author kevin
 *
 */
public class SAAJXMLStreamReader implements XMLStreamReader
{
    /**
     * The empty location.
     */
    private static final Location EMPTY_LOCATION = new EmptyLocation() ;
    
    /**
     * The header element.
     */
    private final SOAPElement headerElement ;
    /**
     * The current node.
     */
    private Node currentNode ;
    /**
     * The current text of an element.
     */
    private String text ;
    /**
     * The current type ;
     */
    private int type ;
    /**
     * The namespace context.
     */
    private NamespaceContextImpl namespaceContext ;
    /**
     * A stack of child node iterators.
     */
    private Stack childNodeIterators = new Stack() ;
    /**
     * The attribute names for the current element.
     */
    private Name[] attributeNames ;
    
    /**
     * Construct the SAAJ XMLStreamReader.
     * @param headerElement The header element.
     */
    public SAAJXMLStreamReader(final SOAPElement headerElement)
    {
        this.headerElement = headerElement ;
        initialiseNamespaces(headerElement.getParentElement()) ;
        startElement(headerElement) ;
    }
    
    /**
     * Get the next parsing event.
     * @return The integer code corresponding to the parse event.
     */
    public int next()
        throws XMLStreamException
    {
        // just returns start, end, characters, CData
        if ((currentNode == headerElement) && (type == END_ELEMENT))
        {
            throw new XMLStreamException(WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_1")) ;
        }
        else if (type == START_ELEMENT)
        {
            final Iterator childNodeIter = ((SOAPElement)currentNode).getChildElements() ;
            final Node child = getNextChild(childNodeIter) ;
            if (child != null)
            {
                childNodeIterators.push(childNodeIter) ;
                processChild(child) ;
            }
            else
            {
                final String textValue = currentNode.getValue() ;
                if (textValue != null)
                {
                    text = textValue ;
                    type = CHARACTERS ;
                }
                else
                {
                    type = END_ELEMENT ;
                }
            }
            return type ;
        }
        else if ((type == CHARACTERS) || (type == CDATA) || (type == END_ELEMENT))
        {
            if ((type == CHARACTERS) && (text != null))
            {
                text = null ;
                type = END_ELEMENT ;
            }
            else
            {
                final Iterator childNodeIter = (Iterator)childNodeIterators.pop();
                final Node child = getNextChild(childNodeIter) ;
                if (child != null)
                {
                    childNodeIterators.push(childNodeIter) ;
                    processChild(child) ;
                }
                else
                {
                    type = END_ELEMENT ;
                    currentNode = currentNode.getParentElement() ;
                }
            }
        }
        else
        {
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_3") ;
            final String message = MessageFormat.format(pattern, new Object[] {new Integer(type)}) ;
            throw new XMLStreamException(message) ;
        }
        return type ;
    }

    /**
     * Get the next tag event.
     * @return The integer code corresponding to the parse event.
     */
    public int nextTag()
        throws XMLStreamException
    {
        final int nextType = next() ;
        if ((nextType != START_ELEMENT) && (nextType != END_ELEMENT))
        {
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_4") ;
            final String message = MessageFormat.format(pattern, new Object[] {new Integer(nextType)}) ;
            throw new XMLStreamException(message) ;
        }
        return nextType ;
    }

    /**
     * Get the current event type.
     * @param The current event type.
     */
    public int getEventType()
    {
        return type ;
    }

    /**
     * Returns true if there is another parsing event.
     * @return true if there is another parsing event, false otherwise.
     */
    public boolean hasNext()
        throws XMLStreamException
    {
        return ((currentNode != headerElement) || (type != END_ELEMENT)) ;
    }

    /**
     * Does the current event have a qualified name?
     * @return true if a qualified name, false otherwise.
     */
    public boolean hasName()
    {
        return ((type == START_ELEMENT) || (type == END_ELEMENT)) ;
    }

    /**
     * Returns the qualified name of the start/end element event.
     * @return the tag qualified name.
     */
    public QName getName()
    {
        if (hasName())
        {
            return qualifiedName(((SOAPElement)currentNode).getElementName()) ;
        }
        return null ;
    }

    /**
     * Returns the local name of the start/end element or entity reference event.
     * @return the local name.
     */
    public String getLocalName()
    {
        return (hasName() ? ((SOAPElement)currentNode).getElementName().getLocalName() : null) ;
    }

    /**
     * Get the prefix of the current event or null.
     * @return the prefix.
     */
    public String getPrefix()
    {
        return (hasName() ? ((SOAPElement)currentNode).getElementName().getPrefix() : null) ;
    }

    /**
     * Get the namespace URI of the current event.
     * @return the namespace URI.
     */
    public String getNamespaceURI()
    {
        return (hasName() ? ((SOAPElement)currentNode).getElementName().getURI() : null) ;
    }

    /**
     * Test the current event type.
     * @param type The event type.
     * @param namespaceURI The namespace URI.
     * @param localName The local name.
     */
    public void require(final int type, final String namespaceURI, final String localName)
        throws XMLStreamException
    {
        if ((type != this.type) || (hasName() && !(testEquals(namespaceURI, getNamespaceURI()) && testEquals(localName, getLocalName()))))
        {
            throw new XMLStreamException(WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_5")) ;
        }
    }

    /**
     * Is the current event a start tag?
     * @return true if a start tag, false otherwise.
     */
    public boolean isStartElement()
    {
        return (type == START_ELEMENT) ;
    }

    /**
     * Is the current event an end tag?
     * @return true if an end tag, false otherwise.
     */
    public boolean isEndElement()
    {
        return (type == END_ELEMENT) ;
    }

    /**
     * Is the current event a character event?
     * @return true if a character event, false otherwise.
     */
    public boolean isCharacters()
    {
        return ((type == CHARACTERS) || (type == CDATA)) ;
    }

    /**
     * Is the current event a whitespace event?
     * @return true if a whitespace event, false otherwise.
     */
    public boolean isWhiteSpace()
    {
        return false ;
    }

    /**
     * Get the number of attributes on the element.
     * @return the number of attributes.
     */
    public int getAttributeCount()
    {
        if (hasName())
        {
            return (attributeNames == null ? 0 : attributeNames.length) ;
        }
        return 0 ;
    }

    /**
     * Get the qualified name of the specified attribute.
     * @param index The attribute index.
     * @return The attribute qualified name.
     */
    public QName getAttributeName(final int index)
    {
        final Name name = getAttribute(index) ;
        return (name == null ? null : qualifiedName(name)) ;
    }

    /**
     * Get the namespace of the specified attribute.
     * @param index The attribute index.
     * @return The attribute namespace.
     */
    public String getAttributeNamespace(final int index)
    {
        final Name name = getAttribute(index) ;
        return (name == null ? null : name.getURI()) ;
    }

    /**
     * Get the local name of the specified attribute.
     * @param index The attribute index.
     * @return The attribute local name.
     */
    public String getAttributeLocalName(final int index)
    {
        final Name name = getAttribute(index) ;
        if (name != null)
        {
            final String localName = name.getLocalName() ;
            return (localName == null ? name.getQualifiedName() : localName) ;
        }
        return null ;
    }

    /**
     * Get the prefix of the specified attribute.
     * @param index The attribute index.
     * @return The attribute prefix.
     */
    public String getAttributePrefix(final int index)
    {
        final Name name = getAttribute(index) ;
        return (name == null ? null : name.getPrefix()) ;
    }

    /**
     * Get the type of the specified attribute.
     * @param index The attribute index.
     * @return The attribute type.
     */
    public String getAttributeType(final int index)
    {
        return null ;
    }

    /**
     * Get the value of the specified attribute.
     * @param index The attribute index.
     * @return The attribute value.
     */
    public String getAttributeValue(final int index)
    {
        final Name name = getAttribute(index) ;
        return (name == null ? null : ((SOAPElement)currentNode).getAttributeValue(name)) ;
    }

    /**
     * Get the value of the specified attribute.
     * @param namespaceURI The namespace URI of the attribute.
     * @param localName The local name of the attribute.
     * @return The attribute value.
     */
    public String getAttributeValue(final String namespaceURI, final String localName)
    {
        if (hasName())
        {
            final SOAPElement currentElement = (SOAPElement)currentNode ;
            final int numAttrs = (attributeNames == null ? 0 : attributeNames.length) ;
            for(int count = 0 ; count < numAttrs ; count++)
            {
                final Name name = attributeNames[count] ;
                if (testEquals(namespaceURI, name.getURI()) && testEquals(localName, name.getLocalName()))
                {
                    return ((SOAPElement)currentElement).getAttributeValue(name) ;
                }
            }
        }
        return null ;
    }
    
    /**
     * Was this attribute created by default?
     * @return true if created by default, false otherwise.
     */
    public boolean isAttributeSpecified(final int index)
    {
        return false ;
    }

    /**
     * Get the number of namespaces declared on this starT/end element.
     */
    public int getNamespaceCount()
    {
        return namespaceContext.getNamespaceCount() ;
    }

    /**
     * Get the prefix for the specified namespace declaration.
     * @param index The namespace declaration index.
     */
    public String getNamespacePrefix(final int index)
    {
        return namespaceContext.getPrefix(index) ;
    }

    /**
     * Get the URI for the specified namespace declaration.
     * @param index The namespace declaration index.
     */
    public String getNamespaceURI(final int index)
    {
        return namespaceContext.getNamespaceURI(index) ;
    }

    /**
     * Get the URI for the namespace prefix.
     * @param prefix The namespace prefix.
     */
    public String getNamespaceURI(final String prefix)
    {
        return namespaceContext.getNamespaceURI(prefix) ;
    }

    /**
     * Does the current event have text?
     * @return true if the current event has text, false otherwise.
     */
    public boolean hasText()
    {
        return ((type == CHARACTERS) || (type == CDATA)) ;
    }

    /**
     * Get the current event as a string.
     * @return The current event as a string.
     */
    public String getText()
    {
        if (hasText())
        {
            if ((type == CHARACTERS) && (text != null))
            {
                return text ;
            }
            else
            {
                return ((Text)currentNode).getValue() ;
            }
        }
        return null ;
    }

    /**
     * Get the characters from the current event.
     * @return The characters from the current event.
     */
    public char[] getTextCharacters()
    {
        if (hasText())
        {
            return getText().toCharArray() ;
        }
        return null ;
    }

    /**
     * Get the characters from the current event.
     * @param sourceStart The start index of the source.
     * @param target The target array.
     * @param targetStart The start index of the target.
     * @param length The maximum length of the target.
     * @return The number of characters copied.
     */
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length)
            throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_6")) ;
    }

    /**
     * Get the index of the first character.
     * @return the index of the first character.
     */
    public int getTextStart()
    {
        return 0 ;
    }

    /**
     * Get the length of the text.
     * @return the length of the text.
     */
    public int getTextLength()
    {
        if (hasText())
        {
            return ((Text)currentNode).getValue().length() ;
        }
        return 0 ;
    }

    /**
     * Get the content of a text only element.
     * @return The text content.
     */
    public String getElementText()
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_7")) ;
    }

    /**
     * Get the current namespace context.
     * @return the namespace context.
     */
    public NamespaceContext getNamespaceContext()
    {
        return namespaceContext ;
    }

    /**
     * Return the input encoding.
     * @return the input encoding or null.
     */
    public String getEncoding()
    {
        return null ;
    }

    /**
     * Get the location.
     * @return the location.
     */
    public Location getLocation()
    {
        return EMPTY_LOCATION ;
    }

    /**
     * Get the XML version.
     * @return the XML version or null if not declared.
     */
    public String getVersion()
    {
        return null ;
    }

    /**
     * Get the standalone from the XML declaration.
     * @return true if standalone, false otherwise.
     */
    public boolean isStandalone()
    {
        return true ;
    }

    /**
     * Was standalone set in the document?
     * @return true if set, false otherwise.
     */
    public boolean standaloneSet()
    {
        return false ;
    }

    /**
     * Return the character encoding.
     * @return the character encoding or null.
     */
    public String getCharacterEncodingScheme()
    {
        return null ;
    }

    /**
     * Get the target of a processing instruction.
     * @return the processing instruction target.
     */
    public String getPITarget()
    {
        return null ;
    }

    /**
     * Get the data of a processing instruction.
     * @return the processing instruction data.
     */
    public String getPIData()
    {
        return null ;
    }

    /**
     * Get the value of the specified property.
     * @param name The name of the property.
     * @return the property value.
     */
    public Object getProperty(final String name)
        throws IllegalArgumentException
    {
        return null ;
    }

    /**
     * Close the stream.
     */
    public void close()
        throws XMLStreamException
    {
        // Do nothing
    }
    
    
    /**
     * Get the next child node.
     * @param childNodeIter The current child node iterator.
     * @return The next child node or null.
     */
    private Node getNextChild(final Iterator childNodeIter)
    {
        while(childNodeIter.hasNext())
        {
            final Node node = (Node)childNodeIter.next();
            if (node instanceof Text)
            {
                final Text text = (Text)node ;
                if (text.isComment())
                {
                    continue ;
                }
            }
            return node ;
        }
        return null ;
    }
    
    /**
     * Process the child node.
     * @param child The child node.
     * @throws XMLStreamException for unsupported node types.
     */
    private void processChild(final Node child)
        throws XMLStreamException
    {
        if (child instanceof Text)
        {
            currentNode = child ;
            type = CHARACTERS ;
        }
        else if (child instanceof SOAPElement)
        {
            startElement((SOAPElement)child) ;
        }
        else
        {
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.DOMXMLStreamReader_2") ;
            final String message = MessageFormat.format(pattern, new Object[] {child.getClass().getName()}) ;
            throw new XMLStreamException(message) ;
        }
    }
    
    /**
     * Start an element.
     * @param element The new element.
     */
    private void startElement(final Node element)
    {
        type = START_ELEMENT ;
        currentNode = element ;
        processNamespaces((SOAPElement)element) ;
        
        attributeNames = null ;
        final Iterator attrIter = ((SOAPElement)element).getAllAttributes() ;
        if (attrIter.hasNext())
        {
            final ArrayList attributeList = new ArrayList() ;
            do
            {
                final Name name = (Name)attrIter.next() ;
                attributeList.add(name) ;
            }
            while(attrIter.hasNext()) ;
            
            if (attributeList.size() > 0)
            {
                attributeNames = (Name[]) attributeList.toArray(new Name[attributeList.size()]) ;
            }
        }
    }
    
    /**
     * Test object references for equality.
     * @param lhs The first object.
     * @param rhs The second object.
     * @return true if equals or both null, false otherwise,
     */
    private boolean testEquals(final Object lhs, final Object rhs)
    {
        if (lhs == null)
        {
            return (rhs == null) ;
        }
        return (lhs.equals(rhs)) ;
    }
    
    /**
     * Get the attribute with the specified index.
     * @param index The attribute index.
     * @return The attribute name or null if the index is invalid.
     */
    private Name getAttribute(final int index)
    {
        return ((type == START_ELEMENT) && (attributeNames != null) ? attributeNames[index] : null) ;
    }
    
    /**
     * Get the qualified name.
     * @param name The current name.
     * @return The qualified name.
     */
    private QName qualifiedName(final Name name)
    {
        final String localName = name.getLocalName() ;
        if (localName == null)
        {
            return new QName(name.getQualifiedName()) ;
        }
        final String prefix = name.getPrefix() ;
        final String namespaceURI = name.getURI() ;
        if (prefix == null)
        {
            return new QName(namespaceURI, localName) ;
        }
        return new QName(namespaceURI, localName, prefix) ;
    }
    
    /**
     * Initialise the namespaces for the parent of the initial element.
     * @param element The parent of the initial element.
     */
    private void initialiseNamespaces(final SOAPElement element)
    {
        if (element != null)
        {
            initialiseNamespaces(element.getParentElement()) ;
            processNamespaces(element) ;
        }
    }
    
    /**
     * Process the namespaces for the current element.
     * @param element The current element.
     */
    private void processNamespaces(final SOAPElement element)
    {
        namespaceContext = new NamespaceContextImpl(namespaceContext) ;
        final Iterator prefixIter = element.getNamespacePrefixes() ;
        while(prefixIter.hasNext())
        {
            final String prefix = (String)prefixIter.next() ;
            final String uri = element.getNamespaceURI(prefix) ;
            if (prefix == null)
            {
                namespaceContext.setDefaultNamespace(uri) ;
            }
            else
            {
                namespaceContext.setPrefix(prefix, uri) ;
            }
        }
    }
}
