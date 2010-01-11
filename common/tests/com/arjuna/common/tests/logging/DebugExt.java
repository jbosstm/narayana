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
import com.arjuna.common.util.logging.*;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class DebugExt
{
    @Test
    public void testDebugExt()
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream bufferedStream = new PrintStream(buffer);
        PrintStream originalStream = System.out;

        LoggingEnvironmentBean loggingEnvironmentBean = commonPropertyManager.getLoggingEnvironmentBean();
        String originalFactory = loggingEnvironmentBean.getLoggingFactory();
        String originalDebugLevel = loggingEnvironmentBean.getDebugLevel();
        String originalVisibilityLevel = loggingEnvironmentBean.getVisibilityLevel();
        String originalFacilityLevel = loggingEnvironmentBean.getFacilityLevel();

        loggingEnvironmentBean.setLoggingFactory("com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger");
        loggingEnvironmentBean.setDebugLevel("0x"+Long.toString(DebugLevel.FUNCS_AND_OPS, 16));
        loggingEnvironmentBean.setVisibilityLevel("0x"+Long.toString(VisibilityLevel.VIS_PACKAGE, 16));
        loggingEnvironmentBean.setFacilityLevel("0xffffffff"); // FacilityCode.FAC_ALL - Long.toString does the wrong thing.

		System.setOut(bufferedStream);
        LogFactory.reset(); // make sure it reloads the modified config.

        try {
    		writeLogMessages();
        } finally {
            loggingEnvironmentBean.setLoggingFactory(originalFactory);
            loggingEnvironmentBean.setDebugLevel(originalDebugLevel);
            loggingEnvironmentBean.setVisibilityLevel(originalVisibilityLevel);
            loggingEnvironmentBean.setFacilityLevel(originalFacilityLevel);
            System.setOut(originalStream);
            LogFactory.reset();
        }
		verifyResult(buffer.toString());
    }

    private static void writeLogMessages()
    {
        LogNoi18n myNoi18nLog = LogFactory.getLogNoi18n("DebugExt");

        myNoi18nLog.debug(DebugLevel.FUNCS_AND_OPS, VisibilityLevel.VIS_PACKAGE, FacilityCode.FAC_ALL,
                "This debug message is enabled since it matches default Finer Values");

        myNoi18nLog.debug(DebugLevel.CONSTRUCT_AND_DESTRUCT, VisibilityLevel.VIS_PACKAGE, FacilityCode.FAC_ALL,
                "This debug message is discarded since it does'nt match default Finer Values");

        myNoi18nLog.debug(DebugLevel.FULL_DEBUGGING, VisibilityLevel.VIS_PACKAGE, FacilityCode.FAC_ALL,
                "This debug message is enabled since it the Logger allows full debugging");
    }

    private static void verifyResult(String result) {
        String[] lines = result.split("\r?\n");

        assertNotNull(lines);
        assertEquals(2, lines.length);

        assertTrue("Got actual value: "+lines[0], lines[0].matches("\\s*DEBUG \\[main\\] .*enabled.*"));
        assertTrue("Got actual value: "+lines[1], lines[1].matches("\\s*DEBUG \\[main\\] .*enabled.*"));
    }
}
