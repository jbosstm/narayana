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

public class ListServersStatusTest extends TestCase {
    private static Logger log = LogManager.getLogger(ListServersStatusTest.class);

    private CommandHandler commandHandler;

    private boolean running = false;

    public void setUp() throws Exception {
        this.commandHandler = new CommandHandler();
    }

    public void tearDown() throws Exception {
        log.info("ListServersStatusTest::tearDown");

        if (running) {
            if (commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
                fail("Could not stop the server");
            } else {
                running = false;
            }
        }
    }

    public void testListServersStatusTestWithAdditionalArgs() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServersStatusTest::testListServersStatusTestWithAdditionalArgs");
        String command = "listServersStatus foo";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testListServersStatusTestWithoutServers() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServersStatusTest::testListServersStatusTestWithoutServers");
        String command = "listRunningServers";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command was not successful");
        }
    }

    public void testListServersStatusTestWithServers() throws IOException, MalformedObjectNameException, NullPointerException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServersStatusTest::testListServersStatusTestWithServers");
        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }
        running = true;
        log.info("Started");
        String command = "advertise testsui BAR";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command failed");
        }
        log.info("advertised");
        command = "listServersStatus";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command was not successful");
        }
        log.info("got status");
    }
}
