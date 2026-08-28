/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * $Id: DummyListenerService.java,v 1.2 2004/06/24 13:52:53 nmcl Exp $
 */

package com.jboss.transaction.txinterop.proxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ProxyListenerService extends HttpServlet
{
    /**
     * The SOAP 1.1 content type.
     */
    private static final String SOAP_11_CONTENT_TYPE = "text/xml" ;
    /**
     * The SOAP 1.2 content type.
     */
    private static final String SOAP_12_CONTENT_TYPE = "application/soap+xml" ;
    /**
     * The name of the SOAP Action header.
     */
    public static final String SOAP_ACTION_HEADER = "SOAPAction" ;
    /**
     * The name of the Transfer encoding header.
     */
    public static final String TRANSFER_ENCODING_HEADER = "transfer-encoding" ;
    /**
     * The name of the SOAP Action header.
     */
    public static final String TRANSFER_ENCODING_VALUE_CHUNKED = "chunked" ;
    /**
     * The default data size.
     */
    private static final int DEFAULT_DATA_SIZE = 256 ;
    
    /**
     * Initialise the servlet.
     * @param config The servlet configuration.
     */
    public void init(final ServletConfig config)
        throws ServletException
    {
        super.init(config);
        
        // Initialise the local host:port/urlstub for the proxy.
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        String bindAddress = wscEnvironmentBean.getBindAddress11();
        int bindPort = wscEnvironmentBean.getBindPort11();
        String baseURI = "http://" + bindAddress + ":" +  bindPort + "/interop11";
        final String proxyServiceURI = baseURI + "/proxy";
        ProxyURIRewriting.setProxyURI(proxyServiceURI) ;
    }
    
    /**
     * Handle the post request.
     * @param httpServletRequest The current HTTP servlet request.
     * @param httpServletResponse The current HTTP servlet response.
     */
    public void doPost(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
        throws ServletException, IOException
    {
        final String conversationIdentifier = getConversationIdentifier(httpServletRequest) ;
        
        final byte[] requestContents = getContents(httpServletRequest.getInputStream()) ;
        final String soapAction = httpServletRequest.getHeader(SOAP_ACTION_HEADER) ;
        final boolean jbossClient = ProxyConversation.isInternalConversationId(conversationIdentifier) ;
        final String alternateConversationIdentifier = ProxyConversation.getAlternateConversationId(conversationIdentifier) ;
        
        final ProxyConversationState state = ProxyConversation.getConversationState(conversationIdentifier) ;

System.out.println("KEV: processing SOAP action " + trimAction(soapAction)) ;
        // Search header for wsa:To and wsa:Address elements, changing their URL parts as we go.
        // Get the target URL from the to.
        try
        {
            final StringWriter newMessageWriter = new StringWriter() ;
            final WriterSAXHandler writerHandler = new WriterSAXHandler(newMessageWriter) ;
            final AddressingProxySAXHandler addressingHandler = new AddressingProxySAXHandler(writerHandler, alternateConversationIdentifier) ;
            
            ContentHandler stateHandler = (state == null ? null : state.getHandler(addressingHandler)) ;
            ContentHandler parserHandler = (stateHandler == null ? addressingHandler : stateHandler) ;
            
            final XMLReader xmlReader = XMLReaderFactory.createXMLReader() ;
            xmlReader.setContentHandler(parserHandler) ;
            xmlReader.parse(new InputSource(new ByteArrayInputStream(requestContents))) ;
            
            final StringBuffer newMessageBuffer = newMessageWriter.getBuffer() ;
            final StringBuffer messageBuffer = (jbossClient ? newMessageBuffer : new StringBuffer(new String(requestContents))) ;
            
            ProxyConversation.appendConversation(conversationIdentifier, checkForXMLDecl(messageBuffer)) ;

            final String identifier = addressingHandler.getIdentifier() ;
            if ((state != null) && state.handleAction(trimAction(soapAction), identifier))  
            {
                httpServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED) ;
                httpServletResponse.flushBuffer() ;
System.out.println("KEV: handled SOAP action " + trimAction(soapAction)) ;
                return ;
            }
            
            // parse input stream
            
            final URL destURL = new URL(addressingHandler.getToAddress()) ;
            final HttpURLConnection destConnection = (HttpURLConnection)destURL.openConnection() ;
            try
            {
                destConnection.setDoOutput(true) ;
                destConnection.setUseCaches(false) ;
                
                // copy the headers
                final Enumeration headerNameEnum = httpServletRequest.getHeaderNames() ;
                while(headerNameEnum.hasMoreElements())
                {
                    final String name = (String)headerNameEnum.nextElement() ;
                    if (name.equalsIgnoreCase(TRANSFER_ENCODING_HEADER))
                    {
                        // any messages we send are not chunked!
                        final String value = httpServletRequest.getHeader(name) ;
                        if (!value.equalsIgnoreCase(TRANSFER_ENCODING_VALUE_CHUNKED))
                        {
                            // this may actually cause a problem depending upon the encoding but try it anyway
                            destConnection.setRequestProperty(name, value) ;
                        }
                    }
                    else
                    {
                        final String value = httpServletRequest.getHeader(name) ;
                        destConnection.setRequestProperty(name, value) ;
                    }
                }
                
                // Set content length
                destConnection.setRequestProperty("Content-Length", Integer.toString(newMessageBuffer.length())) ;
        		final int port = destURL.getPort() ;
        		final String host = (port > 0 ? destURL.getHost() + ":" + port : destURL.getHost()) ;
                destConnection.setRequestProperty("Host", host) ;
                destConnection.setRequestMethod("POST") ;
                // Connect
                destConnection.connect() ;
                // Write the new request
                final OutputStream os = destConnection.getOutputStream() ;
                os.write(newMessageBuffer.toString().getBytes()) ;
                os.flush() ;
                os.close() ;
                
                final int responseCode = destConnection.getResponseCode() ;
                final String fullContentType = destConnection.getContentType() ;
                final String contentType = getContentType(fullContentType) ;
                switch (responseCode)
                {
                    case HttpServletResponse.SC_OK:
                    case HttpServletResponse.SC_ACCEPTED:
                        if ((contentType != null) && !(SOAP_11_CONTENT_TYPE.equals(contentType) ||
                            SOAP_12_CONTENT_TYPE.equals(contentType)))
                        {
                            httpServletResponse.setStatus(HttpServletResponse.SC_ACCEPTED) ;
                            httpServletResponse.flushBuffer() ;
                            break ;
                        }
                        // FALL THRU
                    default:
                        // Pass the response back.
                        httpServletResponse.setStatus(destConnection.getResponseCode()) ;
                        if (fullContentType != null)
                        {
                            httpServletResponse.setContentType(fullContentType) ;
                        }
                        
                        // Copy data
                        final int datasize = DEFAULT_DATA_SIZE ;
                        final char[] data = new char[datasize] ;
                        int readCount ;
                        
                        final InputStream is ;
                        if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR)
                        {
                            is = destConnection.getErrorStream() ;
                        }
                        else
                        {
                            is = destConnection.getInputStream() ;
                        }
                        if (is != null) {
                            final byte[] responseBytes = getContents(is) ;
                            if (responseBytes.length > 0) {
                                try
                                {
                                    final StringWriter newResponseWriter = new StringWriter() ;
                                    final WriterSAXHandler responseWriterHandler = new WriterSAXHandler(newResponseWriter) ;
                                    final AddressingProxySAXHandler responseAddressingHandler = new AddressingProxySAXHandler(responseWriterHandler, alternateConversationIdentifier) ;

                                    // refetch the state handler so it gets a chance to go away
                                    stateHandler = (state == null ? null : state.getHandler(responseAddressingHandler)) ;
                                    parserHandler = (stateHandler == null ? responseAddressingHandler : stateHandler) ;

                                    // always use the standard handler for replies?
                                    final XMLReader responseXmlReader = XMLReaderFactory.createXMLReader() ;
                                    responseXmlReader.setContentHandler(parserHandler) ;
                                    responseXmlReader.parse(new InputSource(new ByteArrayInputStream(responseBytes))) ;

                                    final StringBuffer newResponseBuffer = newResponseWriter.getBuffer() ;
                                    final String newResponseString = newResponseBuffer.toString();

                                    final String responseString = (jbossClient ? newResponseString : new String(responseBytes));
                                    if ((contentType != null) && !(SOAP_11_CONTENT_TYPE.equals(contentType) ||
                                            SOAP_12_CONTENT_TYPE.equals(contentType)))
                                    {
                                        ProxyConversation.appendConversation(conversationIdentifier, escapeContents(responseString)) ;
                                    }
                                    else
                                    {
                                        ProxyConversation.appendConversation(conversationIdentifier, responseString) ;
                                    }
                                    final ServletOutputStream sos = httpServletResponse.getOutputStream() ;
                                    sos.print(newResponseString) ;
                                    sos.flush() ;
                                    httpServletResponse.setContentLength(newResponseString.length()) ;
                                }
                                finally
                                {
                                    is.close() ;
                                }
                            } else {
                                httpServletResponse.setContentLength(0) ;
                            }
                        } else {
                            httpServletResponse.setContentLength(0) ;
                        }
                        break ;
                }
            }
            finally
            {
                destConnection.disconnect() ;
            }
        }
        catch (Exception exception)
        {
            System.err.println("Proxy Listener Service: " + exception);
            exception.printStackTrace() ;
        }
        catch (Error error)
        {
            System.err.println("Proxy Listener Service: " + error);
            error.printStackTrace() ;
        }
    }
    
    /**
     * Trim quotes from the action.
     * @param action The action.
     * @return The trimmed action.
     */
    private static String trimAction(final String action)
    {
        final int length = (action == null ? 0 : action.length()) ;
        if ((length < 2) || (action.charAt(0) != '"') || (action.charAt(length-1) != '"'))
        {
            return action ;
        }
        return action.substring(1, length-1) ;
    }

    /**
     * Get the content type part.
     * @param fullContentType The full content type.
     * @return The content type.
     */
    private static String getContentType(final String fullContentType)
    {
        if (fullContentType == null)
        {
            return null ;
        }
        final int separatorIndex = fullContentType.indexOf(';') ;
        return (separatorIndex == -1 ? fullContentType : fullContentType.substring(0, separatorIndex)) ;
    }

    /**
     * Get the conversation identifier from the request.
     * @return The conversation identifier.
     */
    private static String getConversationIdentifier(final HttpServletRequest httpServletRequest)
    {
        final String pathInfo = httpServletRequest.getPathInfo() ;
        final int separator = pathInfo.indexOf('/', 1) ;
        return pathInfo.substring(1, separator) ;
    }
    
    /**
     * Get the contents of the input stream
     * @param is The input stream.
     * @return The contents.
     * @throws IOException for errors.
     */
    private byte[] getContents(final InputStream is)
    	throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
        final byte[] buffer = new byte[1024] ;
        int readCount ;
        do
        {
            readCount = is.read(buffer, 0, buffer.length) ;
            if (readCount > 0)
            {
                baos.write(buffer, 0, readCount) ;
            }
        }
        while(readCount > 0) ;
        return baos.toByteArray() ;
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
    
    /**
     * Escape the contents of the string.
     * @param contents The original contents.
     * @return The escaped contents.
     */
    private static String escapeContents(final String contents)
    {
        final int length = contents.length() ;
        StringWriter escapedContents = null ;
        
        for(int count = 0 ; count < length ; count++)
        {
            final char ch = contents.charAt(count) ;
            if ((ch == '<') || (ch == '>') || (ch == '&') || (ch == '"'))
            {
                if (escapedContents == null)
                {
                    escapedContents = new StringWriter(length) ;
                    if (count > 0)
                    {
                        escapedContents.write(contents, 0, count-1) ;
                    }
                }
                if (ch == '<')
                {
                    escapedContents.write("&lt;") ;
                }
                else if (ch == '>')
                {
                    escapedContents.write("&gt;") ;
                }
                else if (ch == '&')
                {
                    escapedContents.write("&amp;") ;
                }
                else if (ch == '"')
                {
                    escapedContents.write("&quot;") ;
                }
            }
            else if (escapedContents != null)
            {
                escapedContents.write(ch) ;
            }
        }
        return (escapedContents == null ? contents : escapedContents.toString()) ;
    }
}
