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

public class ListServiceStatusTest extends TestCase {
    private static final Logger log = LogManager.getLogger(ListServiceStatusTest.class);

    private CommandHandler commandHandler;

    boolean shutdownRequired = false;

    public void setUp() throws Exception {
        this.commandHandler = new CommandHandler();
        shutdownRequired = false;
    }

    public void tearDown() throws Exception {
        log.info("ListServiceStatusTest::tearDown");
        if (shutdownRequired && commandHandler.handleCommand("shutdown testsui".split(" ")) != 0) {
            fail("Could not stop the server");
        }
    }

    public void testListServiceStatusWithoutServerName() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServiceStatusTest::testListServiceStatusWithoutServerName");
        String command = "listServiceStatus";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testListServiceStatusWithoutServiceName() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServiceStatusTest::testListRunningServersWithoutServers");
        String command = "listServiceStatus testsui";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testListServiceStatusWithAdditionalParameters() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServiceStatusTest::testListServiceStatusWithAdditionalParameters");
        String command = "listServiceStatus testsui 1 BAR";
        if (commandHandler.handleCommand(command.split(" ")) == 0) {
            fail("Command was successful");
        }
    }

    public void testListServiceStatusWithNonRunningServer() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServiceStatusTest::testListServiceStatusWithNonRunningServer");
        String command = "listServiceStatus foo BAR";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command was not successful");
        }
    }

    public void testListServiceStatusWithRunningServer() throws IOException, MalformedObjectNameException,
            NullPointerException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        log.info("ListServiceStatusTest::testListServiceStatusWithRunningServer");
        if (commandHandler.handleCommand("startup testsui".split(" ")) != 0) {
            fail("Could not start the server");
        }
        shutdownRequired = true;
        String command = "advertise testsui BAR";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command failed");
        }
        command = "listServiceStatus testsui BAR";
        if (commandHandler.handleCommand(command.split(" ")) != 0) {
            fail("Command was not successful");
        }
    }
}
