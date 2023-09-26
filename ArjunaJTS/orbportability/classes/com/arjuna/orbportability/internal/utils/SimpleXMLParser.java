/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.orbportability.internal.utils;

/**
 * An <i>extremely</i> simple XML parser used to parse the XML returned by ORBInfo.
 * NOT TO BE USED FOR PARSING XML OUTSIDE OF THIS SCOPE.
 *
 * @author Richard A. Begg
 */
public class SimpleXMLParser
{
    private String  _xml;

    /**
     * Create a simple xml parser which parses the given XML fragment.
     *
     * @param xml The XML fragment to parse
     */
    public SimpleXMLParser(String xml)
    {
        _xml = xml;
    }

    /**
     * Generate a start element tag from the given name.
     *
     *  e.g.  <TAG>
     *
     * @param name The name of the tag.
     */
    private String getStartElement(String name)
    {
        return "<"+ name +">";
    }

    /**
     * Generate an end element tag from the given name.
     *
     *  e.g.  </TAG>
     *
     * @param name The name of the tag.
     */
    private String getEndElement(String name)
    {
        return "</"+ name +">";
    }

    /**
     * Generate a simple XML parser to parse the XML in the given element.
     *
     * @param element The element whose children will be parsed.
     * @return A simple XML parser for parsing the children.
     */
    public SimpleXMLParser getElementParser(String element) throws Exception
    {
        return new SimpleXMLParser(getElementString(element));
    }

    /**
     * Retrieve the children of a given XML element.
     *
     * @param element The element whose children will be returned.
     * @return The XML for that given element.
     */
    public String getElementString(String element) throws Exception
    {
        String text = null;
        String startElement = getStartElement( element );

        int startIndex = _xml.indexOf( startElement );

        if ( startIndex != -1 )
        {
            text = _xml.substring( startIndex + startElement.length() );
            int endIndex = text.indexOf( getEndElement(element) );

            if ( endIndex != -1 )
            {
                text = text.substring( 0, text.indexOf( getEndElement(element) ) );
            }
            else
            {
                throw new Exception();
            }
        }
        else
        {
            throw new Exception();
        }

        return text;
    }
}