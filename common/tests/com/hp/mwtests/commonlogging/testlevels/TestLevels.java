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
package com.hp.mwtests.commonlogging.testlevels;

import com.arjuna.common.util.logging.Logi18n;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.internal.util.logging.commonPropertyManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * JUnit test that verifies log statments made through the CLF appear at the expexted log level.
 * It does this by replacing System.out with a memory backed buffer, writing some log messages
 * and then checking the buffer contents for the expected regexps.
 *
 * Note: due to the way logging is initialized, this is somewhat fragile.
 * In general you can run only one such test in a given JVM without risking interference.
 */
public class TestLevels
{
   /**
    * for logging purposes.
    */
   public static final String CLASS = TestLevels.class.getName();

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

		// test the releveling for AS integration:
		// TODO: how to configure this on a per-test (not per-JVM) basis?
        commonPropertyManager.getLoggingEnvironmentBean().setLoggingSystem("log4j_releveler");

		System.setOut(bufferedStream);
		writeLogMessages();
		System.setOut(originalStream);
		verifyResult(buffer.toString(), true);
	}

	public static void writeLogMessages() {
		// Don't init the log in a member variable - it must be done AFTER System.out is changed.
		// TODO: this needs to be cleaner, or we need each test in it's own JVM!
		Logi18n log = LogFactory.getLogi18n(CLASS, "TestLevels");
		log.debug("testMessage", new Object[] {"1st", "debug"});
		log.info("testMessage", new Object[] {"1st", "info"});
		log.warn("testMessage", new Object[] {"1st", "warn"});
		log.error("testMessage", new Object[] {"1st", "error"});
		log.fatal("testMessage", new Object[] {"1st", "fatal"});
	}

	public static void verifyResult(String result, boolean expectReleveling) {
        String[] lines = result.split("\r?\n");
		assertNotNull(lines);
		assertEquals(5, lines.length);
		assertTrue("Got actual value: "+lines[0], lines[0].matches("\\s*DEBUG \\[main\\] \\(TestLevels.java.*"));

		if(expectReleveling) {
			assertTrue("Got actual value: "+lines[1], lines[1].matches("\\s*DEBUG \\[main\\] \\(TestLevels.java.*"));
		} else {
			assertTrue("Got actual value: "+lines[1], lines[1].matches("\\s*INFO \\[main\\] \\(TestLevels.java.*"));
		}
		assertTrue("Got actual value: "+lines[2], lines[2].matches("\\s*WARN \\[main\\] \\(TestLevels.java.*"));
		assertTrue("Got actual value: "+lines[3], lines[3].matches("\\s*ERROR \\[main\\] \\(TestLevels.java.*"));
		assertTrue("Got actual value: "+lines[4], lines[4].matches("\\s*FATAL \\[main\\] \\(TestLevels.java.*"));
	}
}

