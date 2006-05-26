/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.webservices.transport.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapMessageLogging;
import com.arjuna.webservices.SoapProcessor;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.SoapServiceEndpointProvider;
import com.arjuna.webservices.util.ClassLoaderHelper;

/**
 * Servlet handling SOAP requests over HTTP.
 * @author kevin
 */
public class HttpServiceMultiplexorServlet extends HttpServlet implements SoapServiceEndpointProvider
{
    /**
     * The servlet serial version UID.
     */
    private static final long serialVersionUID = 7075789572555058716L ;

    /**
     * The key used for the request URL within a message exchange.
     */
    private static final byte[] REQUEST_URL_CONTEXT_PROPERTY = new byte[0] ;
    
    /**
     * The error code for unknown content types.
     */
    private static final int UNKNOWN_CONTENT_TYPE_STATUS = HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE ;
    /**
     * The error code for unknown services.
     */
    private static final int UNKNOWN_SERVICE_STATUS = HttpServletResponse.SC_NOT_FOUND ;
    /**
     * The content for unknown content type.
     */
    private static final String UNKNOWN_CONTENT_TYPE_CONTENT ;
    /**
     * The content for unknown services.
     */
    private static final String UNKNOWN_SERVICE_CONTENT ;
    
    static
    {
        UNKNOWN_CONTENT_TYPE_CONTENT = getResourceAsString("UnknownContentType.html") ;
        UNKNOWN_SERVICE_CONTENT = getResourceAsString("UnknownService.html") ;
    }
    
    /**
     * The Base HTTP URI for clients.
     */
    private transient String baseHttpURI ;
    /**
     * The Base HTTPS URI for clients.
     */
    private transient String baseHttpsURI ;
    /**
     * Flag to log packets.
     */
    private boolean logPackets = true ;
  
    /**
     * Initialise the servlet.
     * @param servletConfig The servlet configuration.
     * @throws ServletException for configuration errors.
     */
    public void init(final ServletConfig servletConfig)
        throws ServletException
    {
        config(servletConfig) ;
        // KEV configure the packet logging.
    }
    
    /**
     * Handle the post request.
     * @param request The http servlet request
     * @param response The http servlet response
     */
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        final SoapService soapService = getSoapService(request.getPathInfo()) ;
        if (soapService == null)
        {
            response.setStatus(UNKNOWN_SERVICE_STATUS) ;
            if (UNKNOWN_SERVICE_CONTENT != null)
            {
                response.getWriter().print(UNKNOWN_SERVICE_CONTENT) ;
            }
        }
        else
        {
            final String fullContentType = request.getContentType() ;
            final String contentType = HttpUtils.getContentType(fullContentType) ;
            final SoapProcessor soapProcessor = getSoapProcessor(soapService, contentType) ;
            if (soapProcessor == null)
            {
                response.setStatus(UNKNOWN_CONTENT_TYPE_STATUS) ;
                if (UNKNOWN_CONTENT_TYPE_CONTENT != null)
                {
                    response.getWriter().print(UNKNOWN_CONTENT_TYPE_CONTENT) ;
                }
            }
            else
            {
                final String action = getHeader(request, HttpUtils.SOAP_ACTION_HEADER) ;
                
                final MessageContext messageContext = new MessageContext() ;
                initialiseContext(messageContext, request) ;
                final MessageContext messageResponseContext = new MessageContext() ;
                final Reader reader ;
                // KEV create a reader to remove the XML declaration.
                if (logPackets)
                {
                    final String contents = readAll(request.getReader()) ;
                    SoapMessageLogging.appendThreadLog(contents) ;
                    reader = new StringReader(contents) ;
                }
                else
                {
                    reader = request.getReader() ;
                }
                final SoapMessage soapResponse ;
                try
                {
                    soapResponse = soapProcessor.process(messageContext,
                        messageResponseContext, action, reader) ;
                }
                finally
                {
                    if (logPackets)
                    {
                        SoapMessageLogging.clearThreadLog() ;
                    }
                }
                response.setContentType(contentType + HttpUtils.HTTP_DEFAULT_CHARSET_PARAMETER) ;
                if (soapResponse == null)
                {
                    response.setStatus(HttpServletResponse.SC_ACCEPTED) ;
                }
                else
                {
                    final String actionURI = soapResponse.getAction() ;
                    response.addHeader(HttpUtils.SOAP_ACTION_HEADER, '"' + actionURI + '"') ;
                    response.setStatus(soapResponse.isFault() ?
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR :
                        HttpServletResponse.SC_OK) ;
                    try
                    {
                        soapResponse.output(response.getWriter()) ;
                    }
                    catch (Throwable th)
                    {
                        throw new ServletException("Error sending response", th) ;
                    }
                }
            }
        }
    }
    
    /**
     * Get the SOAP service mapped onto the path.
     * @param pathInfo The path information.
     * @return The SOAP service or null if unknown.
     */
    private SoapService getSoapService(final String pathInfo)
    {
        if (pathInfo.length() > 0)
        {
            final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
            if ('/' == pathInfo.charAt(0))
            {
                return soapRegistry.getSoapService(pathInfo.substring(1)) ;
            }
            else
            {
                return soapRegistry.getSoapService(pathInfo) ;
            }
        }
        else
        {
            return null ;
        }
    }

    /**
     * Initialise the context.
     * @param messageContext The message context.
     * @param request The current request.
     */
    private void initialiseContext(final MessageContext messageContext, final HttpServletRequest request)
    {
        final StringBuffer requestURL = request.getRequestURL() ;
        final int separatorIndex = lastIndexOf(requestURL, '/') ; ;
        requestURL.setLength(separatorIndex) ;
        messageContext.setProperty(REQUEST_URL_CONTEXT_PROPERTY, request.getRequestURL().toString()) ;
        messageContext.setScheme(request.getScheme()) ;
    }
    
    /**
     * Find the last index of the specified character.
     * @param buffer The buffer.
     * @param charValue The char to find.
     * @return The index of the character or -1 if not present.
     */
    private int lastIndexOf(final StringBuffer buffer, final char charValue)
    {
        int index = buffer.length()-1 ;
        while((index >= 0) && (charValue != buffer.charAt(index)))
        {
            index-- ;
        }
        return index ;
    }
    
    /**
     * Get the URI of a service supported by this provider.
     * @param messageContext The message context.
     * @param serviceName The service name.
     * @return The service URI or null if not known.
     */
    public String getServiceURI(final MessageContext messageContext,
        final String serviceName)
    {
        final String requestUrlContext = (String)messageContext.getProperty(REQUEST_URL_CONTEXT_PROPERTY) ;
        if (requestUrlContext != null)
        {
            return requestUrlContext + '/' + serviceName ;
        }
        else
        {
            return getServiceURI(messageContext.getScheme(), serviceName) ;
        }
    }
    
    /**
     * Get the URI of a service supported by this provider.
     * @param scheme The addresing scheme.
     * @param serviceName The service name.
     * @return The service URI or null if not known.
     */
    public String getServiceURI(final String scheme, final String serviceName)
    {
        if (HttpUtils.HTTP_SCHEME.equals(scheme))
        {
            if (baseHttpURI != null)
            {
                return baseHttpURI + serviceName ;
            }
        }
        else if (HttpUtils.HTTPS_SCHEME.equals(scheme))
        {
            if (baseHttpsURI != null)
            {
                return baseHttpsURI + serviceName ;
            }
        }
        return null ;
    }

    /**
     * Get the SOAP processor based on the content type.
     * @param contentType The request content type.
     * @return The SOAP processor or null if the content type is not supported.
     */
    private SoapProcessor getSoapProcessor(final SoapService soapService, final String contentType)
    {
        if (HttpUtils.SOAP_11_CONTENT_TYPE.equalsIgnoreCase(contentType))
        {
            return soapService.getSOAP11Processor() ;
        }
        else if (HttpUtils.SOAP_12_CONTENT_TYPE.equalsIgnoreCase(contentType))
        {
            return soapService.getSOAP12Processor() ;
        }
        else
        {
            return null ;
        }
    }
    
    /**
     * Get the header and strip any whitespace and enclosing quotes.
     * @param request The current request.
     * @param headerName The name of the header.
     * @return The stripped header value or null if empty.
     */
    private String getHeader(final HttpServletRequest request, final String headerName)
    {
        final String headerValue = request.getHeader(headerName) ;
        if (headerValue == null)
        {
            return null ;
        }
        final StringBuffer buffer = new StringBuffer(headerValue) ;
        final int length = buffer.length() ;
        int startIndex = -1 ;
        boolean foundStartQuote = false ;
        for(int count = 0 ; count < length ; count++)
        {
            final char ch = buffer.charAt(count) ;
            if (!Character.isWhitespace(ch))
            {
                if ((ch == '"') && !foundStartQuote)
                {
                    foundStartQuote = true ;
                }
                else
                {
                    startIndex = count ;
                    break ;
                }
            }
        }
        if (startIndex == -1)
        {
            return null ;
        }
        int endIndex = -1 ;
        boolean foundEndQuote = false ;
        for(int count = length-1 ; count >= 0 ; count--)
        {
            final char ch = buffer.charAt(count) ;
            if (!Character.isWhitespace(ch))
            {
                if ((ch == '"') && foundStartQuote && !foundEndQuote)
                {
                    foundEndQuote = true ;
                }
                else
                {
                    endIndex = count ;
                    break ;
                }
            }
        }
        if (startIndex == endIndex)
        {
            return null ;
        }
        return buffer.substring(startIndex, endIndex+1) ;
    }
    
    /**
     * Configure the endpoint servlet.
     * @param servletConfig The servlet configuration.
     * @throws ServletException For errors during configuration.
     */
    private void config(final ServletConfig servletConfig)
        throws ServletException
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        soapRegistry.registerSoapServiceProvider(HttpUtils.HTTP_SCHEME, this) ;
        soapRegistry.registerSoapServiceProvider(HttpUtils.HTTPS_SCHEME, this) ;
        
        baseHttpURI = processURI(servletConfig.getInitParameter("BaseHttpURI")) ;
        baseHttpsURI = processURI(servletConfig.getInitParameter("BaseHttpsURI")) ;
    }
    
    /**
     * Process the URI to add a trailing / character.
     * @param uri The URI to process.
     * @return The processed URI.
     */
    private String processURI(final String uri)
    {
        if ((uri == null) || (uri.length() == 0))
        {
            return null ;
        }
        
        final int length = uri.length() ;
        if (uri.charAt(length-1) == '/')
        {
            return uri ;
        }
        return uri + '/' ;
    }
    
    /**
     * Handle the deserialisation of this class.
     * @param in The object input stream.
     * @throws IOException for IO errors.
     * @throws ClassNotFoundException for errors during class loading.
     */
    private void readObject(final ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject() ;
        try
        {
            config(getServletConfig()) ;
        }
        catch (final ServletException se)
        {
            throw new IOException(se.getMessage()) ;
        }
    }
    
    /**
     * Return the contents of the resource as a string.
     * @param resourceName The name of the resource.
     * @return The contents or null if an error occurs.
     */
    private static String getResourceAsString(final String resourceName)
    {
        try
        {
            return ClassLoaderHelper.getResourceAsString(HttpServiceMultiplexorServlet.class, resourceName) ;
        }
        catch (final IOException ioe)
        {
            return null ;
        }
    }
    
    /**
     * Read all the contents of the reader.
     * @param reader The specified reader.
     * @return The contents.
     * @throws IOException For errors during reading.
     */
    private static String readAll(final Reader reader)
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
        checkForXMLDecl(contents) ;
        return contents.toString() ;
    }
    
    private static void checkForXMLDecl(final StringBuffer buffer)
    {
        int count = 0 ;
        try
        {
            while(Character.isWhitespace(buffer.charAt(count))) count++ ;
            if (buffer.charAt(count) == '<')
            {
                if (buffer.charAt(count+1) == '?')
                {
                    count+=2 ;
                    while(buffer.charAt(count++) != '>') ;
                }
            }
            if (count > 0)
            {
                buffer.delete(0, count) ;
            }
        }
        catch (final StringIndexOutOfBoundsException sioobe) {}
    }
}
