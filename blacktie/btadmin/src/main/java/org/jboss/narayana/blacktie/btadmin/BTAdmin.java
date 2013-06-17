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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.management.MalformedObjectNameException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

/**
 * Launcher for the btadmin tool.
 * 
 * @author tomjenkinson
 */
public class BTAdmin {
    private static Logger log = LogManager.getLogger(BTAdmin.class);

    private static InputStreamReader isr = new InputStreamReader(System.in);
    private static BufferedReader br = new BufferedReader(isr);

    public static void main(String[] args) throws IOException {
        int exitStatus = -1;
        if (System.getProperty("log4j.configuration") == null && !new File("log4cxx.properties").exists()
                && !new File("log4j.xml").exists()) {
            BasicConfigurator.configure();
            log.info("BasicConfigurator ran");
        }

        boolean interactive = args.length == 0;
        boolean done = false;
        try {
            CommandHandler commandHandler = new CommandHandler();
            do {
                if (interactive) {
                    System.out.print("> ");
                    args = br.readLine().split("\\s+"); // split on white space
                }

                try {
                    exitStatus = commandHandler.handleCommand(args);
                    if (exitStatus == 0) {
                        log.trace("Command was successful");
                    } else {
                        log.trace("Command failed");
                    }
                    if (args.length > 0 && args[0].equals("quit")) {
                        done = true;
                    }
                } catch (Exception e) {
                    log.error("Could not invoke command: " + e.getMessage(), e);
                }
            } while (interactive && !done);
        } catch (MalformedObjectNameException e) {
            log.error("MBean name was badly structured: " + e.getMessage(), e);
        } catch (ConfigurationException e) {
            log.error("BlackTie Configuration invalid: " + e.getMessage(), e);
        }

        if (!interactive) {
            // Exit the launcher with the value of the command
            // This must be a halt so that any executed servers are not reaped
            // by the JVM. If spawned servers die when launcher does we will
            // need to investigate using setppid or something to set the
            // spawned process as daemons
            Runtime.getRuntime().halt(exitStatus);
        }
    }
}
