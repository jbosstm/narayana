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

public class UnadvertiseTest extends TestCase {
    private static final Logger log = LogManager.getLogger(UnadvertiseTest.class);

    private CommandHandler commandHandler;

    public void setUp() throws Exception {
        log.info("UnadvertiseTest::setUp");
        this.commandHandler = new CommandHandler();

        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }

        if (commandHandler.handleCommand("advertise testsui BAR".split(" ")) != 0) {
            if (commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
                log.error("Could not stop the server");
            }
            fail("Command was not successful");
        }
    }

    public void tearDown() throws Exception {
        log.info("UnadvertiseTest::tearDown");
        if (commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
            fail("Could not stop the server");
        }
    }

    public void testUnadvertise() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("UnadvertiseTest::testUnadvertise");
        if (commandHandler.handleCommand("unadvertise testsui BAR".split(" ")) != 0) {
            fail("Command was not successful");
        }
    }

    public void testUnadvertiseWithoutService() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("UnadvertiseTest::testUnadvertiseWithoutService");
        if (commandHandler.handleCommand("unadvertise testsui".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testUnadvertiseWithoutServer() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("UnadvertiseTest::testUnadvertiseWithoutServer");
        if (commandHandler.handleCommand("unadvertise BAR".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testUnadvertiseNoArgs() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("UnadvertiseTest::testUnadvertiseNoArgs");
        if (commandHandler.handleCommand("unadvertise".split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testAdvertiseNotAdvertised() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("UnadvertiseTest::testAdvertiseNotAdvertised");
        if (commandHandler.handleCommand("unadvertise testsui PBF".split(" ")) == 0) {
            fail("Command was successful");
        }
    }
}
