/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.xts.servicetests.service;

import jakarta.xml.ws.Endpoint;

/**
 * A convenience class used to dynamically deploy XTS ServiceTest Service instances
 *
 * Unfortunately, this cannot be used because JBossWS does not support the Endpoint API
 */
public class XTSServiceTestServiceManager
{
    public static void publish(String url)
    {
        Endpoint.publish(url, new XTSServiceTestPortTypeImpl());
    }
}