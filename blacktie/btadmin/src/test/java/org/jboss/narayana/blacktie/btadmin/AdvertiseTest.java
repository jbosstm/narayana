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
package org.jboss.narayana.blacktie.btadmin;

import java.io.IOException;

import javax.management.MalformedObjectNameException;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

public class AdvertiseTest extends TestCase {
    private static final Logger log = LogManager.getLogger(AdvertiseTest.class);

    private CommandHandler commandHandler;

    public void setUp() throws Exception {
        log.info("AdvertiseTest::setUp");
        this.commandHandler = new CommandHandler();

        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }
    }

    public void tearDown() throws Exception {
        log.info("AdvertiseTest::tearDown");
        if (commandHandler.handleCommand("shutdown".split(" ")) != 0) {
            fail("Could not stop the server");
        }
    }

    public void testAdvertise() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            ConfigurationException {
        log.info("AdvertiseTest::testAdvertise");
        String command = "advertise testsui BAR";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command failed");
        }
    }

    public void xtestAdvertiseWithoutService() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        log.info("AdvertiseTest::testAdvertiseWithoutService");
        String command = "advertise testsui";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void xtestAdvertiseWithoutServer() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        log.info("AdvertiseTest::testAdvertiseWithoutServer");
        String command = "advertise BAR";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void xtestAdvertiseNoArgs() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        log.info("AdvertiseTest::testAdvertiseNoArgs");
        String command = "advertise";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void xtestAdvertiseNoFunctionConfig() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException, ConfigurationException {
        log.info("AdvertiseTest::testAdvertiseNoFunctionConfig");
        String command = "advertise testsui PBF";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }
}
