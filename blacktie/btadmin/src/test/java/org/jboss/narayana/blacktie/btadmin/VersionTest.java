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

public class VersionTest extends TestCase {
    private static final Logger log = LogManager.getLogger(VersionTest.class);

    private CommandHandler commandHandler;

    public void setUp() throws Exception {
        this.commandHandler = new CommandHandler();
    }

    public void tearDown() {
    }

    public void testVersion() throws IOException, MalformedObjectNameException, NullPointerException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        log.info("VersionTest::testVersion");
        if (commandHandler.handleCommand("version".split(" ")) != 0) {
            fail("Command was unsuccessful");
        }
    }
}
