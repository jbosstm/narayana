/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.common.tests.logging;

import com.arjuna.common.internal.util.logging.LoggingEnvironmentBean;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.Logi18n;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Verify logger classes produce the expected output.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class ExerciseLoggerImpls
{
    private static final String CLASS = ExerciseLoggerImpls.class.getName();
    
    @Test
    public void testJakartaLog4j() {
        testWithFactory("com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger", 0);
    }

    @Test
    public void testBasicLog() {

        commonPropertyManager.getBasicLogEnvironmentBean().setLogFile(null);
        commonPropertyManager.getBasicLogEnvironmentBean().setLevel("debug");
        testWithFactory("com.arjuna.common.internal.util.logging.basic.BasicLogFactory", 5);
    }

	private void testWithFactory(String factory, int skipHeaderLines) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream bufferedStream = new PrintStream(buffer);
		PrintStream originalStream = System.out;

        LoggingEnvironmentBean loggingEnvironmentBean = commonPropertyManager.getLoggingEnvironmentBean();
        String originalFactory = loggingEnvironmentBean.getLoggingFactory();
        String originalDebugLevel = loggingEnvironmentBean.getDebugLevel();
        Locale originalLocale = Locale.getDefault();

        loggingEnvironmentBean.setLoggingFactory(factory);
        loggingEnvironmentBean.setDebugLevel("0xffffffff");

		System.setOut(bufferedStream);
        LogFactory.reset(); // make sure it reloads the modified config.

        try {

            Locale.setDefault(new Locale("en", "US"));

            Logi18n logi18n = LogFactory.getLogi18n(CLASS, "logging_msg");
    		generateTestOutput(logi18n);

            LogNoi18n logNoi18n = LogFactory.getLogNoi18n("loggingtest");
            generateTestOutput(logNoi18n);

        } finally {
            loggingEnvironmentBean.setLoggingFactory(originalFactory);
            loggingEnvironmentBean.setDebugLevel(originalDebugLevel);
            Locale.setDefault(originalLocale);
            System.setOut(originalStream);
            LogFactory.reset();
        }
		verifyResult(buffer.toString(), skipHeaderLines);

	}

    private void generateTestOutput(Logi18n logi18n) {
        logi18n.debug("debug_message");
        logi18n.debug("debug_message", (Throwable)null);
        logi18n.debug("debug_param_message", new Object[] {"one", "two"});
        logi18n.debug("debug_param_message", new Object[] {"one", "two"}, null);

        logi18n.info("info_message");
        logi18n.info("info_message", (Throwable)null);
        logi18n.info("info_param_message", new Object[] {"one", "two"});
        logi18n.info("info_param_message", new Object[] {"one", "two"}, null);

        logi18n.warn("warn_message");
        logi18n.warn("warn_message", (Throwable)null);
        logi18n.warn("warn_param_message", new Object[] {"one", "two"});
        logi18n.warn("warn_param_message", new Object[] {"one", "two"}, null);

        logi18n.error("error_message");
        logi18n.error("error_message", (Throwable)null);
        logi18n.error("error_param_message", new Object[] {"one", "two"});
        logi18n.error("error_param_message", new Object[] {"one", "two"}, null);

        logi18n.fatal("fatal_message");
        logi18n.fatal("fatal_message", (Throwable)null);
        logi18n.fatal("fatal_param_message", new Object[] {"one", "two"});
        logi18n.fatal("fatal_param_message", new Object[] {"one", "two"}, null);

    }

    private void generateTestOutput(LogNoi18n logNoi18n) {
        
    }

    private void verifyResult(String result, int skipHeaderLines) {
        String[] lines = result.split("\r?\n");

        if(skipHeaderLines > 0) {
            lines = Arrays.copyOfRange(lines, skipHeaderLines, lines.length);
        }

        String[][] expected = new String[][] {
                {"DEBUG", "This is a debug message"},
                {"DEBUG", "This is a debug message"},
                {"DEBUG", "This is a debug message with params one and two"},
                {"DEBUG", "This is a debug message with params one and two"},

                {"INFO", "This is a info message"},
                {"INFO", "This is a info message"},
                {"INFO", "This is a info message with params one and two"},
                {"INFO", "This is a info message with params one and two"},

                {"WARN", "This is a warn message"},
                {"WARN", "This is a warn message"},
                {"WARN", "This is a warn message with params one and two"},
                {"WARN", "This is a warn message with params one and two"},
                
                {"ERROR", "This is a error message"},
                {"ERROR", "This is a error message"},
                {"ERROR", "This is a error message with params one and two"},
                {"ERROR", "This is a error message with params one and two"},
                
                {"FATAL", "This is a fatal message"},
                {"FATAL", "This is a fatal message"},
                {"FATAL", "This is a fatal message with params one and two"},
                {"FATAL", "This is a fatal message with params one and two"}
                
                

        };

        assertNotNull(lines);

        for(String line : lines) {
            System.out.println("LINE: "+line);
        }

        assertEquals(expected.length, lines.length);

        for(int i = 0; i < expected.length; i++) {
            String expectedPattern = ".*"+expected[i][0]+".*ExerciseLoggerImpls.*"+expected[i][1]+"$";
            assertTrue("Got actual value: "+lines[i], lines[i].matches(expectedPattern));
        }
    }
}
