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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arjuna.webservices.util.ClassLoaderHelper;

/**
 * The test servlet.
 * @author kevin
 */
public class TestLogServlet extends HttpServlet
{
    /**
     * The servlet serial version UID.
     */
    private static final long serialVersionUID = 2566877081747112520L ;
    
    /**
     * The not found HTML page.
     */
    private String notFoundResponse ;
    /**
     * The directory contents HTML page.
     */
    private String directoryContents ;
    
    /**
     * Initialise the servlet.
     * @param servletConfig The servlet configuration.
     */
    public void init(final ServletConfig servletConfig)
        throws ServletException
    {
        super.init(servletConfig) ;
        try
        {
            notFoundResponse = ClassLoaderHelper.getResourceAsString(TestLogServlet.class, "notFoundResponse.html") ;
            directoryContents = ClassLoaderHelper.getResourceAsString(TestLogServlet.class, "directoryContents.html") ;
        }
        catch (final IOException ioe)
        {
            throw new ServletException("Failed to load HTML pages", ioe) ;
        }
    }
    
    /**
     * Return the specified logs
     * @param request The HTTP servlet request.
     * @param response The HTTP servlet response.
     */
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        final String logName = request.getPathInfo() ;
        if ((logName == null) || (logName.length() == 0))
        {
            response.sendRedirect("logs/") ;
            return ;
        }
        else if ("/".equals(logName))
        {
            response.setContentType("text/html") ;
            response.setStatus(HttpServletResponse.SC_OK) ;
            response.setContentLength(directoryContents.length()) ;
            response.getOutputStream().print(directoryContents) ;
        }
        else
        {
            final String contents ;
            try
            {
                contents = TestLogController.readLog(logName) ;
            }
            catch (final Throwable th)
            {
                log("Error reading log file", th) ;
                response.setContentType("text/html") ;
                response.setStatus(HttpServletResponse.SC_NOT_FOUND) ;
                response.setContentLength(notFoundResponse.length()) ;
                response.getOutputStream().print(notFoundResponse) ;
                return ;
            }
            
            response.setContentType("text/xml") ;
            response.setStatus(HttpServletResponse.SC_OK) ;
            response.setContentLength(contents.length()) ;
            response.getOutputStream().print(contents) ;
        }
    }
}
