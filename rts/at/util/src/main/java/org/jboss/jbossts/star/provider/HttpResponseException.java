/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

 package org.jboss.jbossts.star.provider;

/**
 * Exception for wrapping unexpected http response codes
 * @deprecated The new class will be updated to extend Exception instead of Error
 */
@Deprecated // The new class will be updated to extend Exception instead of Error
public class HttpResponseException extends Error {
    private int expectedResponse;
    private int actualResponse;
    private String body;

    public HttpResponseException(Throwable cause, String body, int expectedResponse, int actualResponse) {
        super(cause);
        this.body = body;
        this.expectedResponse = expectedResponse;
        this.actualResponse = actualResponse;
    }

    public HttpResponseException(Throwable cause, String body, int[] expectedResponses, int actualResponse) {
        this(cause, body, (expectedResponses != null && expectedResponses.length != 0 ? expectedResponses[0] : -1), actualResponse);
    }

    public HttpResponseException(int expectedResponse, int actualResponse) {
        this(null, null, expectedResponse, actualResponse);
    }

    public int getExpectedResponse() {
        return expectedResponse;
    }

    public int getActualResponse() {
        return actualResponse;
    }

    public String getBody() {
        return body;
    }

    public String getMessage() {
        if (getCause() != null)
            return super.getMessage();

        if (expectedResponse != actualResponse)
            return "Unexpected status. Expected " +  expectedResponse + " got " + actualResponse;

        throw new Error("Invalid HttpResponseException thrown - there's no error and status is fine");
    }
}