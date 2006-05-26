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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import javax.xml.stream.XMLStreamException;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapMessage;
import com.arjuna.webservices.SoapMessageLogging;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.transport.TransportSoapClient;

/**
 * The HTTP client implementation.
 * @author kevin
 *
 */
public class HttpClient extends TransportSoapClient
{
    /**
     * The headers to set on each request.
     */
    private static final String[][] HTTP_HEADERS = {
        {"User-Agent", "Arjuna/2.0"},
    } ;
    
    /**
     * Invoke a request on a service.
     * @param request The request object.
     * @param url The destination URL.
     * @return The response object.
     * @throws SoapFault For errors processing the request.
     * @throws IOException for processing errors.
     * 
     * @message com.arjuna.webservices.transport.http.HttpClient_1 [com.arjuna.webservices.transport.http.HttpClient_1] - No response from RPC request
     */
    public SoapMessage invoke(final SoapMessage request, final String url)
        throws SoapFault, IOException
    {
        final SoapMessage response = invokeRequest(request, url) ;
        if (response == null)
        {
            throw new SoapFault(SoapFaultType.FAULT_SENDER, WSCLogger.log_mesg.getString("com.arjuna.webservices.transport.http.HttpClient_1")) ;
        }
        return response ;
    }
    
    /**
     * Invoke a one way request on a service.
     * @param request The request object.
     * @param url The destination URL.
     * @throws SoapFault For errors processing the request.
     * @throws IOException for processing errors.
     */
    public void invokeOneWay(final SoapMessage request, final String url)
        throws SoapFault, IOException
    {
        invokeRequest(request, url) ;
    }
    
    /**
     * Invoke the request.
     * @param request The request object.
     * @param url The destination URL.
     * @return The response object.
     * @throws SoapFault For errors processing the request.
     * 
     * @message com.arjuna.webservices.transport.http.HttpClient_2 [com.arjuna.webservices.transport.http.HttpClient_2] - Invalid destination URL
     * @message com.arjuna.webservices.transport.http.HttpClient_3 [com.arjuna.webservices.transport.http.HttpClient_3] - Unsupported URL type, not HTTP or HTTPS
     * @message com.arjuna.webservices.transport.http.HttpClient_4 [com.arjuna.webservices.transport.http.HttpClient_4] - Invalid response code returned: {0}
     */
    private SoapMessage invokeRequest(final SoapMessage request, final String url)
        throws SoapFault, IOException
    {
        final URL serviceURL ;
        try
        {
            serviceURL = new URL(url) ;
        }
        catch (final MalformedURLException murle)
        {
            throw new SoapFault(SoapFaultType.FAULT_SENDER, WSCLogger.log_mesg.getString("com.arjuna.webservices.transport.http.HttpClient_2")) ;
        }
        
        final String requestContents = serialiseRequest(request) ;
        if (SoapMessageLogging.isThreadLogEnabled())
        {
            SoapMessageLogging.appendThreadLog(requestContents) ;
        }
        
        final HttpURLConnection httpURLConnection ;
        try
        {
            httpURLConnection = (HttpURLConnection)serviceURL.openConnection() ;
        }
        catch (final ClassCastException cce)
        {
            throw new SoapFault(SoapFaultType.FAULT_SENDER, WSCLogger.log_mesg.getString("com.arjuna.webservices.transport.http.HttpClient_3")) ;
        }
        
        try
        {
            httpURLConnection.setDoOutput(true) ;
            httpURLConnection.setUseCaches(false) ;
            
            final int numHeaders = HTTP_HEADERS.length ;
            for(int count = 0 ; count < numHeaders ; count++)
            {
                final String[] header = HTTP_HEADERS[count] ;
                httpURLConnection.setRequestProperty(header[0], header[1]) ;
            }
            
            final SoapDetails soapDetails = request.getSoapDetails() ;
            final String contentType = HttpUtils.getContentType(soapDetails) ;
            httpURLConnection.setRequestProperty(HttpUtils.HTTP_CONTENT_TYPE_HEADER, contentType + HttpUtils.HTTP_DEFAULT_CHARSET_PARAMETER) ;
            httpURLConnection.setRequestProperty(HttpUtils.HTTP_ACCEPT_HEADER, contentType) ;
            
            final String requestAction = request.getAction() ;
            final String actionValue = (requestAction == null ? "" : requestAction) ;
            
            // KEV - fix action handling for different SOAP versions
            httpURLConnection.setRequestProperty(HttpUtils.SOAP_ACTION_HEADER, '"' + actionValue + '"') ;
            
            httpURLConnection.setRequestProperty(HttpUtils.HTTP_CONTENT_LENGTH_HEADER,
                Integer.toString(requestContents.length())) ;
            
            final int port = serviceURL.getPort() ;
            final String host = (port > 0 ? serviceURL.getHost() + ":" + port : serviceURL.getHost()) ;
            httpURLConnection.setRequestProperty(HttpUtils.HTTP_HOST_HEADER, host) ;
            httpURLConnection.setRequestProperty(HttpUtils.HTTP_CONNECTION_HEADER, "close") ;
            httpURLConnection.setRequestMethod("POST") ;
            
            httpURLConnection.connect() ;
            final OutputStream os = httpURLConnection.getOutputStream() ;
            final PrintWriter writer = new PrintWriter(os) ;
            writer.print(requestContents) ;
            writer.flush() ;
            
            final int responseCode = httpURLConnection.getResponseCode() ;
            
            if ((responseCode != HttpURLConnection.HTTP_OK) &&
                (responseCode != HttpURLConnection.HTTP_ACCEPTED) &&
                (responseCode != HttpURLConnection.HTTP_INTERNAL_ERROR))
            {
                final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.transport.http.HttpClient_4") ;
                final String message = MessageFormat.format(pattern, new Object[] {new Integer(responseCode)}) ;
                throw new SoapFault(SoapFaultType.FAULT_SENDER, message) ;
            }
            
            final String fullResponseContentType = httpURLConnection.getContentType() ;
//            final String responseContentType = HttpUtils.getContentType(fullResponseContentType) ;
            // Ignore responses that aren't the same version of SOAP
//            if ((contentType != null) && !contentType.equals(responseContentType))
//            {
//                if (SoapMessageLogging.isThreadLogEnabled())
//                {
//                    SoapMessageLogging.appendThreadLog(null) ;
//                }
//                return null ;
//            }
            
            final int contentLength = httpURLConnection.getContentLength() ;
            if (contentLength == 0)
            {
                if (SoapMessageLogging.isThreadLogEnabled())
                {
                    SoapMessageLogging.appendThreadLog(null) ;
                }
                return null ;
            }
            
            final SoapService soapService = request.getSoapService() ;
            final String encoding = HttpUtils.getContentTypeEncoding(fullResponseContentType) ;
            final BufferedInputStream bis ;
            if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR)
            {
                bis = new BufferedInputStream(httpURLConnection.getErrorStream()) ;
            }
            else
            {
                bis = new BufferedInputStream(httpURLConnection.getInputStream()) ;
            }
            final InputStreamReader isr = (encoding == null ? new InputStreamReader(bis) : new InputStreamReader(bis, encoding)) ;
            
            final Reader reader ;
            if ((contentLength <= 0) || SoapMessageLogging.isThreadLogEnabled())
            {
                final String responseContents = readStream(isr) ;
                
                if (responseContents.length() == 0)
                {
                    if (SoapMessageLogging.isThreadLogEnabled())
                    {
                        SoapMessageLogging.appendThreadLog(null) ;
                    }
                    return null ;
                }
                
                if (SoapMessageLogging.isThreadLogEnabled())
                {
                    SoapMessageLogging.appendThreadLog(responseContents) ;
                }
                
                reader = new StringReader(responseContents) ;
            }
            else
            {
                reader = isr ;
            }
            // KEV - fix action handling for different SOAP versions
            final String action = httpURLConnection.getHeaderField(HttpUtils.SOAP_ACTION_HEADER) ;
            final MessageContext responseMessageContext = new MessageContext() ;
            final MessageContext dummyMessageContext = new MessageContext() ;
            try
            {
                return parseResponse(soapService, responseMessageContext, dummyMessageContext, action, reader, soapDetails) ;
            }
            catch (final XMLStreamException xmlse)
            {
                throw new IOException(xmlse.toString()) ;
            }
        }
        finally
        {
            httpURLConnection.disconnect() ;
        }
    }
    
    /**
     * Get the contents of the stream for logging.
     * @param isr The input stream reader.
     * @return The stream contents.
     * @throws IOException For errors during reading.
     */
    private String readStream(final InputStreamReader isr)
        throws IOException
    {
        final StringBuffer stringBuffer = new StringBuffer() ;
        final char[] charBuffer = new char[256] ;
        while(true)
        {
            final int count = isr.read(charBuffer) ;
            if (count > 0)
            {
                stringBuffer.append(charBuffer, 0, count) ;
            }
            else
            {
                break ;
            }
        }
        checkForXMLDecl(stringBuffer) ;
        
        return stringBuffer.toString() ;
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
