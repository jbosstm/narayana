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
package com.arjuna.webservices.transport.http;

import java.io.IOException;
import java.io.Reader;

import com.arjuna.webservices.soap.SoapDetails;

/**
 * Constants used by the HTTP transport layer.
 * @author kevin
 */
class HttpUtils
{
    /**
     * The content type for SOAP 1.1.
     */
    public static final String SOAP_11_CONTENT_TYPE = "text/xml" ;
    /**
     * The content type for SOAP 1.2.
     */
    public static final String SOAP_12_CONTENT_TYPE = "application/soap+xml" ;
    
    /**
     * The name of the SOAP Action header.
     */
    public static final String SOAP_ACTION_HEADER = "SOAPAction" ;
    /**
     * The name of the target host header.
     */
    public static final String HTTP_HOST_HEADER = "Host" ;
    /**
     * The name of the content type header.
     */
    public static final String HTTP_CONTENT_TYPE_HEADER = "Content-Type" ;
    /**
     * The name of the content length header.
     */
    public static final String HTTP_CONTENT_LENGTH_HEADER = "Content-Length" ;
    /**
     * The name of the HTTP Accept header.
     */
    public static final String HTTP_ACCEPT_HEADER = "Accept" ;
    /**
     * The name of the HTTP connection header.
     */
    public static final String HTTP_CONNECTION_HEADER = "Connection" ;
    
    /**
     * The HTTP charset parameter.
     */
    public static final String HTTP_CHARSET_PARAMETER = "charset" ;
    /**
     * The HTTP charset parameter length.
     */
    private static final int HTTP_CHARSET_PARAMETER_LEN = HTTP_CHARSET_PARAMETER.length() ;
    /**
     * The default charset parameter.
     */
    public static final String HTTP_DEFAULT_CHARSET_PARAMETER = "; " + HTTP_CHARSET_PARAMETER + "=utf-8" ;

    /**
     * The name of the HTTP scheme.
     */
    public static final String HTTP_SCHEME = "http" ;
    /**
     * The name of the HTTPS scheme.
     */
    public static final String HTTPS_SCHEME = "https" ;
    
    /**
     * Get the base content type.
     * @param contentType The content type value.
     * @return The base content type.
     */
    public static String getContentType(final String contentType)
    {
        if (contentType == null)
        {
            return contentType ;
        }
        final int separatorIndex = contentType.indexOf(';') ;
        if (separatorIndex >= 0)
        {
            return contentType.substring(0, separatorIndex) ;
        }
        return contentType ;
    }

    /**
     * Get the content type encoding portion of the 
     * @param contentType The content type value.
     * @return The content type encoding or null if not present.
     */
    public static String getContentTypeEncoding(final String contentType)
    {
        if (contentType != null)
        {
            final int length = contentType.length() ;
            
            int separatorIndex = contentType.indexOf(';') ;
            while((separatorIndex >= 0) && (separatorIndex < length))
            {
                separatorIndex = skipWhitespace(contentType, ++separatorIndex) ;
                
                if (separatorIndex >= 0)
                {
                    if (contentType.regionMatches(true, separatorIndex,
                        HTTP_CHARSET_PARAMETER, 0, HTTP_CHARSET_PARAMETER_LEN))
                    {
                        separatorIndex += HTTP_CHARSET_PARAMETER_LEN ;
                        if ((separatorIndex < length-1) && ('=' == contentType.charAt(separatorIndex)))
                        {
                            separatorIndex++ ;
                            if ('"' == contentType.charAt(separatorIndex))
                            {
                                return nonQuote(contentType, separatorIndex+1) ;
                            }
                            else
                            {
                                return nonWhitespace(contentType, separatorIndex) ;
                            }
                        }
                    }
                }
            }
        }
        return null ;
    }

    /**
     * Get the content type based on the SOAP details.
     * @param soapDetails The SOAP details.
     * @return The content type.
     */
    public static String getContentType(final SoapDetails soapDetails)
    {
        if (SoapDetails.SOAP_12_VERSION.equals(soapDetails.getVersion()))
        {
            return SOAP_12_CONTENT_TYPE ;
        }
        else
        {
            return SOAP_11_CONTENT_TYPE ;
        }
    }

    /**
     * Skip whitespace fomr the specified index.
     * @param value The current string.
     * @param startIndex The start index.
     * @return The index of the first non whitespace character or -1.
     */
    private static int skipWhitespace(final String value, final int startIndex)
    {
        int endIndex = startIndex ;
        final int length = value.length() ;
        while((endIndex < length) && Character.isWhitespace(value.charAt(endIndex)))
        {
            endIndex++ ;
        }
        return (endIndex < length ? endIndex : -1) ;
    }

    /**
     * Return the string from the current index until the next quote character.
     * @param value The current string.
     * @param startIndex The start index.
     * @return A string representing all characters up to the next quote character.
     */
    private static String nonQuote(final String value, final int startIndex)
    {
        int endIndex = startIndex ;
        final int length = value.length() ;
        while((endIndex < length) && '"' != value.charAt(endIndex))
        {
            endIndex++ ;
        }
        
        return value.substring(startIndex, endIndex) ;
    }

    /**
     * Return the string from the current index until the next whitespace character.
     * @param value The current string.
     * @param startIndex The start index.
     * @return A string representing all characters up to the next whitespace character.
     */
    private static String nonWhitespace(final String value, final int startIndex)
    {
        int endIndex = startIndex ;
        final int length = value.length() ;
        while((endIndex < length) && !Character.isWhitespace(value.charAt(endIndex)) && (';' != value.charAt(endIndex)))
        {
            endIndex++ ;
        }
        
        return value.substring(startIndex, endIndex) ;
    }

    /**
     * Read all the contents of the reader.
     * @param reader The specified reader.
     * @return The contents.
     * @throws IOException For errors during reading.
     */
    static String readAll(final Reader reader)
        throws IOException
    {
        final StringBuffer contents = new StringBuffer() ;
        final char[] buffer = new char[256] ;
        while(true)
        {
            final int count = reader.read(buffer) ;
            if (count > 0)
            {
                contents.append(buffer, 0, count) ;
            }
            else
            {
                break ;
            }
        }
        return checkForXMLDecl(contents) ;
    }
    
    /**
     * Check for the XML declaration and remove.
     * This method is only used if we are intending to log the SOAP message so that it is easy to combine the XML without creating invalid documents.
     * @param contents The current stream contents.
     * @return The stream contents as a string.
     */
    private static String checkForXMLDecl(final StringBuffer contents)
    {
        int count = 0 ;
        try
        {
            while(Character.isWhitespace(contents.charAt(count))) count++ ;
            if (contents.charAt(count) == '<')
            {
                if (contents.charAt(count+1) == '?')
                {
                    count+=2 ;
                    while(contents.charAt(count++) != '>') ;
                }
            }
            if (count > 0)
            {
                contents.delete(0, count) ;
            }
        }
        catch (final StringIndexOutOfBoundsException sioobe) {}
        
        return contents.toString() ;
    }
}
