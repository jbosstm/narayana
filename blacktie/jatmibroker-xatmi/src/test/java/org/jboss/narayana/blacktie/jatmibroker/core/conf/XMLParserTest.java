/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.jboss.narayana.blacktie.jatmibroker.core.conf;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class XMLParserTest extends TestCase {
    private static final Logger log = LogManager.getLogger(XMLParserTest.class);

    public void setUp() throws InterruptedException {
    }

    public void tearDown() {
    }

    public void testWrongEnvironmentXML() throws ConfigurationException {
        Properties prop = new Properties();
        try {
            XMLParser.loadProperties("btconfig.xsd", "Wrongbtconfig.xml", prop);
            fail("Should have thrown a parser exception or found the file");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testWrongEnvironmentXsd() {
        Properties prop = new Properties();
        try {
            XMLParser.loadProperties("btconfigERROR.xsd", "btconfigERROR.xml", prop);
            fail("Should have thrown a parser exception or found the file");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testAdminEnvironmentXML() throws Exception {
        Properties prop = new Properties();

        try {
            XMLParser.loadProperties("btconfig.xsd", "AdminEnvironment", prop);
            fail("Should have thrown a exception");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testSameService() throws Exception {
        Properties prop = new Properties();

        try {
            XMLParser.loadProperties("btconfig.xsd", "SameEnvironment.xml", prop);
            fail("Should have thrown a exception");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testUnknownMachine() throws Exception {
        Properties prop = new Properties();
        try {
            XMLParser.loadProperties("btconfig.xsd", "UnknownMachine.xml", prop);
            fail("Should have thrown a exception");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testSameServer() throws Exception {
        Properties prop = new Properties();
        try {
            XMLParser.loadProperties("btconfig.xsd", "SameServer.xml", prop);
            fail("Should have thrown a exception");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testSameBuffer() throws Exception {
        Properties prop = new Properties();

        try {
            XMLParser.loadProperties("btconfig.xsd", "SameBuffer.xml", prop);
            fail("Should have thrown a exception");
        } catch (ConfigurationException e) {
            // THIS IS OK
        }
    }

    public void testEnvironmentXML() throws Exception {
        Properties prop = new Properties();
        XMLParser.loadProperties("btconfig.xsd", "btconfig.xml", prop);

        String domain = "fooapp";
        String transid = "TransactionManagerService.OTS";
        String args = "2";
        String arg1 = "-ORBInitRef";
        String arg2 = "NameService=corbaloc::";
        String arg3 = ":3528/NameService";

        String server = "myserv";
        String advertised = "true";
        String size = "1";
        String function = "org.jboss.narayana.blacktie.jatmibroker.xatmi.services.TPCallXOctetService";

        String userlist = "guest:true:true,blacktie:true:true";
        String version = "5.2.13.Final-SNAPSHOT";

        assertTrue(server.equals(prop.getProperty("blacktie.TestOne.server")));
        assertTrue(function.equals(prop.getProperty("blacktie.JAVA_Converse.java_class_name")));
        // assertTrue(library
        // .equals(prop.getProperty("blacktie.TestOne.library_name")));
        assertTrue(advertised.equals(prop.getProperty("blacktie.JAVA_Converse.advertised")));
        assertTrue(domain.equals(prop.getProperty("blacktie.domain.name")));
        assertTrue(version.equals(prop.getProperty("blacktie.domain.version")));
        assertTrue(transid.equals(prop.getProperty("blacktie.trans.factoryid")));
        assertTrue(args.equals(prop.getProperty("blacktie.orb.args")));
        assertTrue(arg1.equals(prop.getProperty("blacktie.orb.arg.1")));
        assertTrue(((String) prop.getProperty("blacktie.orb.arg.2")).startsWith(arg2));
        assertTrue(((String) prop.getProperty("blacktie.orb.arg.2")).endsWith(arg3));

        assertTrue(size.equals(prop.getProperty("blacktie.JAVA_Converse.size")));
        assertTrue(Boolean.TRUE.equals(prop.get("blacktie.ConvService.conversational")));
        assertTrue("queue".equals(prop.getProperty("blacktie.JAVA_Converse.type")));
        assertTrue("queue".equals(prop.getProperty("blacktie.XMLParserTest.type")));
        assertTrue("topic".equals(prop.getProperty("blacktie.JAVA_Topic.type")));
        assertTrue("xml".equals(prop.getProperty("blacktie.XMLParserTest.coding_type")));
    }
}
