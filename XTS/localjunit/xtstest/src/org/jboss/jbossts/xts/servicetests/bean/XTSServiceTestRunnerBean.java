/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.jbossts.xts.servicetests.bean;

import org.jboss.logging.Logger;
import org.jboss.jbossts.xts.servicetests.test.XTSServiceTest;
import org.jboss.jbossts.xts.servicetests.service.recovery.TestATRecoveryModule;
import org.jboss.jbossts.xts.servicetests.service.recovery.TestBARecoveryModule;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * A service bean used to run XTS service tests at JBoss startup. A specific test is configurerd by setting an
 * environment variable. It will normally execute in a JVM configured to use the Byteman agent and an appropriately
 * defined Byteman rule set.
 */

public class XTSServiceTestRunnerBean
        implements ServletContextListener // since we ahve toi use a war and web.xml wee
                                          // run this as a listener rather than as via jboss-beans.xml
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        try {
            start();
        } catch (Exception e) {
            log.warn("TEST IS INVALID DUE TO START PROBLEM", e);
            // ignore
        }
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        try {
            stop();
        } catch (Exception e) {
            log.warn("TEST IS INVALID DUE TO STOP PROBLEM", e);
            // ignore
        }
    }

    public XTSServiceTestRunnerBean()
    {
        testName = System.getProperty(TEST_NAME_KEY);
        testInstance = null;
    }

    /**
     * the service start method which adds a thread to run the current test
     * @throws Exception
     */
    public void start() throws Exception
    {
        log.info("Starting XTSServiceTestRunner");

        // ensure that the xts service test AT recovery helper module is registered
        
        TestATRecoveryModule.register();
        TestBARecoveryModule.register();

        if (testName != null) {
            log.info("Starting XTS Service Test " + testName);
            Class testClass;
            ClassLoader cl = XTSServiceTestRunnerBean.class.getClassLoader();

            try {
                testClass = cl.loadClass(testName);
            } catch (ClassNotFoundException cnfe) {
                log.warn("XTSServiceTestRunner : cannot find test class " + testName, cnfe);
                throw new Exception("XTSServiceTestRunner : cannot find test class " + testName, cnfe);
            }

            try {
                testInstance = (XTSServiceTest)testClass.getDeclaredConstructor().newInstance(); // assumes there is a no-arg constructor
            } catch (InstantiationException ie) {
                log.warn("XTSServiceTestRunner : cannot instantiate test class " + testName, ie);
                throw new Exception("XTSServiceTestRunner : cannot instantiate test class " + testName, ie);
            } catch (IllegalAccessException iae) {
                log.warn("XTSServiceTestRunner : cannot access constructor for test class " + testName, iae);
                throw new Exception("XTSServiceTestRunner : cannot access constructor for test class " + testName, iae);
            } catch (Throwable e) {
                log.warn("XTSServiceTestRunner : cannot construct new instance for test class " + testName, e);
                throw new Exception("XTSServiceTestRunner : cannot construct new instance for test class " + testName, e);
            }

            // since we are running in the AS startup thread we need a separate thread for the test

            testThread = new Thread() {
                private XTSServiceTest test = testInstance;
                public void run()
                {
                    testInstance.run();
                }
            };

            testThread.start();
        }

        log.info("Started XTSServiceTestRunner");
    }

    /**
     * teh service stop method which joins the thread running the current test if it exists
     * @throws Exception
     */
    public void stop() throws Exception
    {
        log.info("Stopping XTSServiceTestRunner");

        if (testThread != null) {
            log.info("Joining test thread " + testName);

            testThread.join();

            log.info("Joined test thread " + testName);
        }

        // ensure that the xts service test AT recovery helper module is unregistered

        TestATRecoveryModule.unregister();
        TestBARecoveryModule.unregister();

        log.info("Stopped XTSServiceTestRunner");
    }

    private final Logger log = org.jboss.logging.Logger.getLogger(XTSServiceTestRunnerBean.class);

    private final String testName;

    private XTSServiceTest testInstance;

    private Thread testThread;

    private final static String TEST_NAME_KEY = "org.jboss.jbossts.xts.servicetests.XTSServiceTestName";
}