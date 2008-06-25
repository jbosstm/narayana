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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A simple filter to sanitise the content type.
 * @author kevin
 */
public class HttpContentTypeFilter implements Filter
{
    /**
     * Initialise the filter.
     * @param filterConfig The filter config.
     */
    public void init(final FilterConfig filterConfig)
        throws ServletException
    {
    }

    /**
     * Filter the request.
     * @param filterConfig The filter config.
     */
    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        final String contentType = request.getContentType() ;
        final String charset = HttpUtils.getContentTypeEncoding(contentType) ;
        
        if ((charset != null) && (charset.length() > 0))
        {
            request.setCharacterEncoding(charset) ;
        }
        chain.doFilter(request, response) ;
    }

    /**
     * Destroy the filter.
     */
    public void destroy()
    {
    }
}
