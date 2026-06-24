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
package com.jboss.transaction.txinterop.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestResult;

import com.arjuna.webservices.util.ClassLoaderHelper;
import com.jboss.transaction.txinterop.interop.MessageLogging;

/**
 * The test servlet.
 * @author kevin
 */
public class TestServlet extends HttpServlet
{
    /**
     * The servlet serial version UID.
     */
    private static final long serialVersionUID = 6764303043215036856L ;
    
    /**
     * The validation templates.
     */
    private Templates validationTemplates ;

    /**
     * Initialise the servlet.
     * @param config The servlet configuration.
     */
    public void init(final ServletConfig config)
        throws ServletException
    {
        super.init(config) ;
        
        final InputStream is = ClassLoaderHelper.getResourceAsStream(getClass(), "processor.xsl") ;
        if (is == null)
        {
            throw new ServletException("Cannot locate transformation stylesheet") ;
        }
        final TransformerFactory factory = TransformerFactory.newInstance() ;
        try
        {
            validationTemplates = factory.newTemplates(new StreamSource(is)) ;
        }
        catch (final TransformerConfigurationException tce)
        {
            throw new ServletException("Error creating transformation template!", tce) ;
        }
    }
    
    /**
     * Execute the test
     * @param request The HTTP servlet request.
     * @param response The HTTP servlet response.
     */
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        final String serviceURI = request.getParameter(TestConstants.PARAM_SERVICE_URI) ;
        final String test = request.getParameter(TestConstants.PARAM_TEST) ;
        final String testTimeoutValue = request.getParameter(TestConstants.PARAM_TEST_TIMEOUT) ;
        // final String asyncTestValue = request.getParameter(TestConstants.PARAM_ASYNC_TEST) ;
        String resultPageAddress = request.getParameter(TestConstants.PARAM_RESULT_PAGE);
        if (resultPageAddress == null || resultPageAddress.length() == 0)
        {
            resultPageAddress = TestConstants.DEFAULT_RESULT_PAGE_ADDRESS;
        }

        final int serviceURILength = (serviceURI == null ? 0 : serviceURI.length()) ;
        final int testLength = (test == null ? 0 : test.length()) ;
        
        long testTimeout = 0 ;
        
        boolean testTimeoutValid = false ;
        if ((testTimeoutValue != null) && (testTimeoutValue.length() > 0))
        {
            try
            {
                testTimeout = Long.parseLong(testTimeoutValue) ;
                testTimeoutValid = true ;
            }
            catch (final NumberFormatException nfe) {} // ignore
        }
        
        // final boolean asyncTest = (asyncTestValue != null) ;
        final boolean asyncTest = true ;

        if ((serviceURILength == 0) || (testLength == 0) || !testTimeoutValid)
        {
            final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/invalidParameters.html") ;
            dispatcher.forward(request, response) ;
            return ;
        }
        
        final HttpSession session = request.getSession() ;
        final String id = session.getId() ;
        final int logCount = getLogCount(session) ;
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss") ;
        final String date = format.format(new Date()) ;
        final String logName = date + "-" + id + "-" + logCount ;

        session.setAttribute(TestConstants.ATTRIBUTE_TEST_RESULT, null) ;
        session.setAttribute(TestConstants.ATTRIBUTE_TEST_VALIDATION, null) ;
        session.setAttribute(TestConstants.ATTRIBUTE_LOG_NAME, null) ;

        final String threadLog ;
        try
        {
            final TestResult result = TestRunner.execute(serviceURI, testTimeout, asyncTest, test) ;
            if (result != null)
            {
                session.setAttribute(TestConstants.ATTRIBUTE_TEST_RESULT, result) ;
                
                threadLog = MessageLogging.getThreadLog() ;
                
                try
                {
                    TestLogController.writeLog(logName, threadLog) ;
                    session.setAttribute(TestConstants.ATTRIBUTE_LOG_NAME, logName) ;
                }
                catch (final IOException ioe)
                {
                    log("Unexpected IOException writing message log", ioe) ;
                }
            }
            else
            {
                threadLog = null ;
            }
        }
        finally
        {
            MessageLogging.clearThreadLog() ;
        }
        
        if ((threadLog != null) && (threadLog.length() > 0))
        {
            try
            {
                final String testValidation = transform(threadLog) ;
                session.setAttribute(TestConstants.ATTRIBUTE_TEST_VALIDATION, testValidation) ;
            }
            catch (final Throwable th)
            {
                log("Unexpected throwable transforming message log", th) ;
            }
        }
        
        final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(resultPageAddress) ;
        dispatcher.forward(request, response) ;
    }
    
    /**
     * Get the log count from the session, incrementing afterwards.
     * @param session The current HTTP session.
     * @return The log count.
     */
    private int getLogCount(final HttpSession session)
    {
        final Object logCountObject = session.getAttribute(TestConstants.ATTRIBUTE_LOG_COUNT) ;
        final int logCount = (logCountObject == null ? 1 : ((Integer)logCountObject).intValue() + 1) ;
        session.setAttribute(TestConstants.ATTRIBUTE_LOG_COUNT, new Integer(logCount)) ;
        return logCount ;
    }
    
    /**
     * Transform the specified message log.
     * @param messageLog The specified message log.
     * @return The transformed result.
     * @throws TransformerConfigurationException For transformer configuration errors.
     * @throws TransformerException The transformation errors.
     */
    private String transform(final String messageLog)
        throws TransformerConfigurationException, TransformerException
    {
        final Source source = new StreamSource(new StringReader(messageLog)) ;
        final Transformer transformer = validationTemplates.newTransformer() ;
        final StringWriter writer = new StringWriter() ;
        final Result result = new StreamResult(writer) ;
        transformer.transform(source, result) ;
        return writer.toString() ;
    }

}
