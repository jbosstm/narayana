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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.arjuna.webservices.SoapFault10;
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
            throw new SoapFault10(SoapFaultType.FAULT_SENDER, WSCLogger.i18NLogger.get_webservices_transport_http_HttpClient_1()) ;
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
            throw new SoapFault10(SoapFaultType.FAULT_SENDER, WSCLogger.i18NLogger.get_webservices_transport_http_HttpClient_2()) ;
        }
        
        final boolean threadLogEnabled = SoapMessageLogging.isThreadLogEnabled() ;
        final String requestContents ;
        if (threadLogEnabled)
        {
            requestContents = serialiseRequest(request) ;
            SoapMessageLogging.appendThreadLog(requestContents) ;
        }
        else
        {
            requestContents = null ;
        }
        
        final HttpURLConnection httpURLConnection ;
        try
        {
            httpURLConnection = (HttpURLConnection)serviceURL.openConnection() ;
        }
        catch (final ClassCastException cce)
        {
            throw new SoapFault10(SoapFaultType.FAULT_SENDER, WSCLogger.i18NLogger.get_webservices_transport_http_HttpClient_3()) ;
        }
        
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
        
        if (requestContents != null)
        {
            httpURLConnection.setRequestProperty(HttpUtils.HTTP_CONTENT_LENGTH_HEADER,
                Integer.toString(requestContents.length())) ;
        }
        
        final int port = serviceURL.getPort() ;
        final String host = (port > 0 ? serviceURL.getHost() + ":" + port : serviceURL.getHost()) ;
        httpURLConnection.setRequestProperty(HttpUtils.HTTP_HOST_HEADER, host) ;
        httpURLConnection.setRequestMethod("POST") ;
        
        httpURLConnection.connect() ;
        final OutputStream os = httpURLConnection.getOutputStream() ;
        try
        {
            final PrintWriter writer = new PrintWriter(os) ;
            if (requestContents != null)
            {
                writer.print(requestContents) ;
            }
            else
            {
                request.output(writer) ;
            }
            writer.flush() ;
        }
        finally
        {
            os.close() ;
        }
        
        final int responseCode = httpURLConnection.getResponseCode() ;
        
        if ((responseCode != HttpURLConnection.HTTP_OK) &&
            (responseCode != HttpURLConnection.HTTP_ACCEPTED) &&
            (responseCode != HttpURLConnection.HTTP_INTERNAL_ERROR))
        {
            final String message = WSCLogger.i18NLogger.get_webservices_transport_http_HttpClient_4(Integer.toString(responseCode));
            throw new SoapFault10(SoapFaultType.FAULT_SENDER, message) ;
        }
        
        final String fullResponseContentType = httpURLConnection.getContentType() ;
//        final String responseContentType = HttpUtils.getContentType(fullResponseContentType) ;
        // Ignore responses that aren't the same version of SOAP
//        if ((contentType != null) && !contentType.equals(responseContentType))
//        {
//            if (threadLogEnabled)
//            {
//                SoapMessageLogging.appendThreadLog(null) ;
//            }
//            return null ;
//        }
        
        final int contentLength = httpURLConnection.getContentLength() ;
        if (contentLength == 0)
        {
            if (threadLogEnabled)
            {
                SoapMessageLogging.appendThreadLog(null) ;
            }
            return null ;
        }
        
        final SoapService soapService = request.getSoapService() ;
        final String encoding = HttpUtils.getContentTypeEncoding(fullResponseContentType) ;
        final InputStream is ;
        if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR)
        {
            is = httpURLConnection.getErrorStream() ;
        }
        else
        {
            is = httpURLConnection.getInputStream() ;
        }
        try
        {
            final BufferedInputStream bis = new BufferedInputStream(is) ;
            final InputStreamReader isr = (encoding == null ? new InputStreamReader(bis) : new InputStreamReader(bis, encoding)) ;
            
            final Reader reader ;
            if (threadLogEnabled || (contentLength <= 0))
            {
                final String responseContents = HttpUtils.readAll(isr) ;
                
                if (responseContents.length() == 0)
                {
                    if (threadLogEnabled)
                    {
                        SoapMessageLogging.appendThreadLog(null) ;
                    }
                    return null ;
                }
                
                if (threadLogEnabled)
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
            is.close() ;
        }
    }
}
