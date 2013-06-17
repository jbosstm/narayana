/* JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General public  License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General public  License for more details.
 * You should have received a copy of the GNU Lesser General public  License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.nbf;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TestNBFSchemaParser extends TestCase {
    private static final Logger log = LogManager.getLogger(TestNBFSchemaParser.class);

    public void test() {
        log.info("TestNBFSchemaParser:test");
        NBFSchemaParser parser = new NBFSchemaParser();
        Map<String, String> flds;

        assertTrue(parser.parse("buffers/test.xsd"));
        assertTrue("test".equals(parser.getBufferName()));
        flds = parser.getFileds();
        assertTrue(flds.size() == 1);
        for (Entry<String, String> entry : flds.entrySet()) {
            log.info(entry.getKey() + " : " + entry.getValue());
        }

        assertTrue(parser.parse("buffers/employee.xsd"));
        assertTrue("employee".equals(parser.getBufferName()));
        flds = parser.getFileds();
        for (Entry<String, String> entry : flds.entrySet()) {
            log.info(entry.getKey() + " : " + entry.getValue());
        }
        assertTrue(flds.size() == 2);
    }

}
