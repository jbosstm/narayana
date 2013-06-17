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

public class ShutdownTest extends TestCase {
    private static final Logger log = LogManager.getLogger(ShutdownTest.class);

    private CommandHandler commandHandler;

    boolean shutdownRequired = false;

    public void setUp() throws Exception {
        this.commandHandler = new CommandHandler();
        shutdownRequired = false;
    }

    public void tearDown() throws Exception {
        log.info("ShutdownTest::tearDown");
        if (shutdownRequired && commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
            fail("Could not stop the server");
        }
    }

    public void testShutdownWithoutArgs() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownWithoutArgs");
        if (commandHandler.handleCommand("shutdown".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testShutdownWithNonIntId() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownWithNonIntId");
        if (commandHandler.handleCommand("shutdown testsui one".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testShutdownUnknownServer() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownUnknownServer");
        if (commandHandler.handleCommand("shutdown UNKNOWN".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testShutdownStoppedServer() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownStoppedServer");
        if (commandHandler.handleCommand("shutdown testsui 1".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testShutdownWithoutId() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownWithoutId");
        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }
        if (commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
            shutdownRequired = true;
            fail("Command was not successful");
        }
    }

    public void testShutdownWithId() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownWithId");
        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }
        log.info("Shutting down testsui 1");
        if (commandHandler.handleCommand("shutdown testsui 1".split(" ")) != 0) {
            shutdownRequired = true;
            fail("Command was not successful");
        }
        log.info("Shutting down testsui 2");
        if (commandHandler.handleCommand("shutdown testsui 2".split(" ")) != 0) {
            shutdownRequired = true;
            fail("Command was not successful");
        }
    }

    public void testShutdownWithInvalidId() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ShutdownTest::testShutdownWithInvalidId");
        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }
        if (commandHandler.handleCommand("shutdown testsui 3".split(" ")) == 0) {
            shutdownRequired = true;
            fail("Command was successful");
        }
        if (commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
            shutdownRequired = true;
            fail("Command was not successful");
        }
    }
}
