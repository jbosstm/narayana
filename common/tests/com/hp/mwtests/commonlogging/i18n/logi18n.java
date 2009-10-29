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
package com.hp.mwtests.commonlogging.i18n;

import com.arjuna.common.util.logging.Logi18n;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.internal.util.logging.commonPropertyManager;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import static org.junit.Assert.*;

public class logi18n
{
    public static final String CLASS = logi18n.class.getName();

    @Test
	public void testLogi18n() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintStream bufferedStream = new PrintStream(buffer);
		PrintStream originalStream = System.out;

		// TODO: how to configure this on a per-test (not per-JVM) basis?
        commonPropertyManager.getLoggingEnvironmentBean().setLoggingFactory("com.arjuna.common.internal.util.logging.jakarta.JakartaLogFactory;com.arjuna.common.internal.util.logging.jakarta.Log4JLogger");

		System.setOut(bufferedStream);
		writeLogMessages();
		System.setOut(originalStream);
		verifyResult(buffer.toString());
	}

   public static void writeLogMessages()
   {
       Logi18n log = LogFactory.getLogi18n(CLASS, "logging_msg");

       Locale.setDefault(new Locale("en", "US"));
       log.info("IDENTIFICATION", new String[] { "Foo", "Bar"});
       log.fatal("FATAL_Message");
       log.info("INFO_Message");

       Locale.setDefault(new Locale("fr", "FR"));
       log = LogFactory.getLogi18n(CLASS, "logging_msg");
       log.info("IDENTIFICATION", new String[] { "Foo", "Bar"});
       log.fatal("FATAL_Message");
       log.info("INFO_Message");
   }

    public static void verifyResult(String result) {
        String[] lines = result.split("\r?\n");

        assertNotNull(lines);
        assertEquals(6, lines.length);
        
        assertTrue("Got actual value: "+lines[0], lines[0].matches("\\s*INFO \\[main\\] \\(logi18n.java.*The FirstName is Foo and the LastName is Bar$"));
        assertTrue("Got actual value: "+lines[1], lines[1].matches("\\s*FATAL \\[main\\] \\(logi18n.java.*This is a Fatal message$"));
        assertTrue("Got actual value: "+lines[2], lines[2].matches("\\s*INFO \\[main\\] \\(logi18n.java.*This is an Info message$"));

        assertTrue("Got actual value: "+lines[3], lines[3].matches("\\s*INFO \\[main\\] \\(logi18n.java.*le prenom est Foo et le nom est Bar$"));
        assertTrue("Got actual value: "+lines[4], lines[4].matches("\\s*FATAL \\[main\\] \\(logi18n.java.*Ceci est un message Fatal$"));
        assertTrue("Got actual value: "+lines[5], lines[5].matches("\\s*INFO \\[main\\] \\(logi18n.java.*Ceci est un message pour information$"));
    }
}
    
    
    
