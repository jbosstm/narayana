/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.xts.servicetests.test;

/**
 * API implemented by classes which are used to execute a specific XTS service test
 */
public interface XTSServiceTest
{
    public void run();
    public boolean isSuccessful();
    public Exception getException();

    // System properties which can be set to configure the behaviour of tests

    public static final String SERVICE_URL1_KEY = "org.jboss.jbossts.xts.servicetests.serviceURL1";
    public static final String SERVICE_URL2_KEY = "org.jboss.jbossts.xts.servicetests.serviceURL2";
    public static final String SERVICE_URL3_KEY = "org.jboss.jbossts.xts.servicetests.serviceURL3";
    public static final String SUBORDINATE_SERVICE_URL1_KEY = "org.jboss.jbossts.xts.servicetests.subordinate.serviceURL1";
    public static final String SUBORDINATE_SERVICE_URL2_KEY = "org.jboss.jbossts.xts.servicetests.subordinate.sserviceURL2";
    public static final String SUBORDINATE_SERVICE_URL3_KEY = "org.jboss.jbossts.xts.servicetests.subordinate.sserviceURL3";
}