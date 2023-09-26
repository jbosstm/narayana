/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * a service is used to serve one or more requests from an input stream and post results on an output stream.
 * when the input sream is closed it is expected to close its output stream.
 * 
 * note that a single service instance may be requested to process incoming requests from multiple input
 * streams in parallel.
 *
 * note also that the service should be resilient to closure of the input and output streams during request
 * processing which can happen in resposne to asynchronous dispatch of a shutdown request to the object which
 * invoked the service.
 */
public interface Service
{
   public void doWork ( InputStream in, OutputStream out )
      throws IOException ;
}