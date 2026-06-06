/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2010
 * @author JBoss Inc.
 */
 package org.jboss.jbossts.star.provider;

/**
 * Exception for wrapping unexpected http response codes
 */
public class HttpResponseException extends Error
{
    private int expectedResponse;
    private int actualResponse;
    private String body;

    public HttpResponseException(Throwable cause, String body, int expectedResponse, int actualResponse)
    {
        super(cause);
        this.body = body;
        this.expectedResponse = expectedResponse;
        this.actualResponse = actualResponse;
    }

    public HttpResponseException(Throwable cause, String body, int[] expectedResponses, int actualResponse)
    {
        this(cause, body, (expectedResponses != null && expectedResponses.length != 0 ? expectedResponses[0] : -1), actualResponse);
    }
    
    public HttpResponseException(int expectedResponse, int actualResponse)
    {
        this(null, null, expectedResponse, actualResponse);
    }

    public int getExpectedResponse()
    {
        return expectedResponse;
    }

    public int getActualResponse()
    {
        return actualResponse;
    }

    public String getBody()
    {
        return body;
    }

    public String getMessage()
    {
        if (getCause() != null)
            return super.getMessage();

        if (expectedResponse != actualResponse)
            return "Unexpected status. Expected " +  expectedResponse + " got " + actualResponse;

        throw new Error("Invalid HttpResponseException thrown - there's no error and status is fine");
    }
}
