/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.xts.servicetests.ejb;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jboss.jbossts.xts.servicetests.service.recovery.TestATRecoveryModule;
import org.jboss.jbossts.xts.servicetests.service.recovery.TestBARecoveryModule;
import org.jboss.jbossts.xts.servicetests.test.XTSServiceTest;
import org.jboss.logging.Logger;

@Singleton
@Startup
public class XTSServiceTestRunnerEJBImpl implements XTSServiceTestRunnerEJB {

	@PostConstruct
	public void start() {
		log.info("Starting XTSServiceTestRunner");

		// ensure that the xts service test AT recovery helper module is registered
		TestATRecoveryModule.register();
		TestBARecoveryModule.register();
	}
	
	@PreDestroy
	public void stop() {
		// ensure that the xts service test AT recovery helper module is unregistered
		TestATRecoveryModule.unregister();
		TestBARecoveryModule.unregister();

		log.info("Stopped XTSServiceTestRunner");
	}

	@SuppressWarnings("rawtypes")
	public void runTest(String testName) throws Exception {
		if(testName != null) {
			log.info("XTSServiceTestRunner run test " + testName);
			Class testClass;
			ClassLoader cl = XTSServiceTestRunnerEJBImpl.class.getClassLoader();

			try {
				testClass = cl.loadClass(testName);
			} catch (ClassNotFoundException cnfe) {
				log.warn("XTSServiceTestRunner : cannot find test class " + testName, cnfe);
				throw new Exception("XTSServiceTestRunner : cannot find test class " + testName, cnfe);
			}

			try {
				testInstance = (XTSServiceTest)testClass.newInstance();
			} catch (InstantiationException ie) {
				log.warn("XTSServiceTestRunner : cannot instantiate test class " + testName, ie);
				throw new Exception("XTSServiceTestRunner : cannot instantiate test class " + testName, ie);
			} catch (IllegalAccessException iae) {
				log.warn("XTSServiceTestRunner : cannot access constructor for test class " + testName, iae);
				throw new Exception("XTSServiceTestRunner : cannot access constructor for test class " + testName, iae);
			}

			// since we are running in the AS startup thread we need a separate thread for the test

			testThread = new Thread() {
				@SuppressWarnings("unused")
				private XTSServiceTest test = testInstance;
				public void run()
				{
					testInstance.run();
				}
			};

			testThread.start();

			if (testThread != null) {
				log.info("Joining test thread " + testName);

				testThread.join();

				log.info("Joined test thread " + testName);
			}
		}
	}

	private final Logger log = org.jboss.logging.Logger.getLogger(XTSServiceTestRunnerEJBImpl.class);

	private XTSServiceTest testInstance;

	private Thread testThread;

}
