/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package com.arjuna.common.tests.logging;

import com.arjuna.common.internal.util.logging.LoggingEnvironmentBean;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.internal.util.logging.commonPropertyManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * JUnit test that verifies log statements made through the CLF appear at the expected log level.
 * It does this by replacing System.out with a memory backed buffer, writing some log messages
 * and then checking the buffer contents for the expected regexps.
 *
 * Note: due to the way logging is initialized, this is somewhat fragile.
 */
public class TestLevels
{
   /**
    * for logging purposes.
    */
   private static final String CLASS = TestLevels.class.getName();

   /**
    *
    * @message testMessage This is the {0} message, logged at level {1}.
    *
    * @param args
    */
   public static void main(String[] args)
   {
	   // CommonLogging-properties.xml: <common><properties><property name= value=>
	   // cd common/install/lib
	   // java -cp jbossts-common.jar:tests/common_tests.jar:../../../ext/commons-logging.jar:../../../ext/log4j-1.2.8.jar:../../etc/ com.hp.mwtests.commonlogging.testlevels.TestLevels
//	   junit.textui.TestRunner.run(suite());
	}

//	public static Test suite() {
//		return new TestSuite(TestLevels.class);
//	}


    @Test
	public void testLog4j() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream bufferedStream = new PrintStream(buffer);
		PrintStream originalStream = System.out;

        LoggingEnvironmentBean loggingEnvironmentBean = commonPropertyManager.getLoggingEnvironmentBean();
        String originalFactory = loggingEnvironmentBean.getLoggingFactory();

		// test the releveling for AS integration:
        loggingEnvironmentBean.setLoggingFactory("com.arjuna.common.internal.util.logging.jakarta.JakartaRelevelingLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger");

		System.setOut(bufferedStream);
        LogFactory.reset(); // make sure it reloads the modified config.

        try {
    		writeLogMessages();
        } finally {
            loggingEnvironmentBean.setLoggingFactory(originalFactory);
            System.setOut(originalStream);
            LogFactory.reset();
        }
		verifyResult(buffer.toString(), true);
	}

	private static void writeLogMessages() {
		// Don't init the log in a member variable - it must be done AFTER System.out is changed.
		LogNoi18n log = LogFactory.getLogNoi18n(CLASS);
		log.debug("testMessage");
	}

	private static void verifyResult(String result, boolean expectReleveling) {
        String[] lines = result.split("\r?\n");
		assertNotNull(lines);
		assertEquals(1, lines.length);
		assertTrue("Got actual value: "+lines[0], lines[0].matches("\\s*DEBUG \\[main\\] \\(JakartaRelevelingLogger.java.*"));

	}
}

