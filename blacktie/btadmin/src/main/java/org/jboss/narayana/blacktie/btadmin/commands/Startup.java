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
package org.jboss.narayana.blacktie.btadmin.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.administration.BlacktieAdministration;
import org.jboss.narayana.blacktie.btadmin.Command;
import org.jboss.narayana.blacktie.btadmin.CommandFailedException;
import org.jboss.narayana.blacktie.btadmin.IncompatibleArgsException;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.Machine;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.Server;

/**
 * The shutdown command will shutdown the server specified
 */
public class Startup implements Command {
    /**
     * The logger to use for output
     */
    private static Logger log = LogManager.getLogger(Startup.class);

    /**
     * The name of the server.
     */
    private String serverName;

    /**
     * Show the usage of the command
     */
    public String getQuickstartUsage() {
        return "[<serverName>]";
    }

    public void initializeArgs(String[] args) throws IncompatibleArgsException {
        if (args.length == 1) {
            serverName = args[0];
        }
    }

    public void invoke(BlacktieAdministration connection, Properties configuration) throws CommandFailedException {
        List<Server> serverLaunchers = (List<Server>) configuration.get("blacktie.domain.serverLaunchers");
        boolean found = false;
        Iterator<Server> iterator = serverLaunchers.iterator();
        while (iterator.hasNext()) {
            Server next = iterator.next();
            if (serverName == null || serverName.equals(next.getName())) {
                if (serverName != null) {
                    log.debug("Listing machines for: " + serverName);
                } else {
                    log.debug("Listing machines");
                }
                List<Machine> localMachinesList;
                try {
                    localMachinesList = next.getLocalMachine();
                } catch (UnknownHostException e) {
                    log.error("Could not get the local machine");
                    throw new CommandFailedException(-1);
                }
                if (localMachinesList.size() != 0) {
                    Iterator<Machine> localMachines = localMachinesList.iterator();
                    while (localMachines.hasNext()) {
                        log.debug("Found machine");
                        Machine localMachine = localMachines.next();
                        String pathToExecutable = localMachine.getPathToExecutable();
                        String argLine = "-i " + localMachine.getServerId() + " -s " + localMachine.getServer().getName();
                        if (localMachine.getArgLine() != null) {
                            argLine = argLine + " " + localMachine.getArgLine();
                        }
                        String[] split = argLine.split("[ ]+");
                        String[] cmdarray = new String[split.length + 1 + 0];
                        cmdarray[0] = pathToExecutable;
                        System.arraycopy(split, 0, cmdarray, 1, split.length);
                        String[] envp = null;
                        File dir = new File(localMachine.getWorkingDirectory());
                        try {
                            ProcessBuilder pb = new ProcessBuilder();
                            pb.command(Arrays.<String>asList(cmdarray));
                            pb.directory(dir);
                            
                            String id = localMachine.getId();
                            String output_fname = id + "-out";
                            String error_fname = id + "-err";
                            pb.redirectError(new File(error_fname));
                            pb.redirectOutput(new File(output_fname));
                            Process exec = pb.start();

                            log.debug("Launched server: " + pathToExecutable + " " + argLine);
                            BufferedReader output = new BufferedReader(new InputStreamReader(new FileInputStream(output_fname)));
                            BufferedReader error = new BufferedReader(new InputStreamReader(new FileInputStream(error_fname)));
                            while (true) {
                                String readLine = output.readLine();
                                if (readLine == null) {
                                    readLine = error.readLine();
                                }
                                if(readLine != null) log.info(readLine);
                                if (readLine == null) {
                                    //throw new CommandFailedException(-3);
                                } else if (readLine.endsWith("serverinit failed")) {
                                    throw new CommandFailedException(-2);
                                } else if (readLine.endsWith("Server waiting for requests...")) {
                                    found = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            log.error("Could not launch the server", e);
                            throw new CommandFailedException(-1);
                        }
                    }
                }
            }
        }
        if (!found) {
            log.error("No machines configured for host");
            throw new CommandFailedException(-1);
        }
    }

    private class EatIO implements Runnable {
        private InputStream is;

        public EatIO(InputStream is) {
            this.is = is;
        }

        public void run() {
            int len;
            byte[] buf = new byte[1024];
            try {
                while ((len = is.read(buf)) > 0) {
                    // DO NOTHING
                }
            } catch (IOException e) {
                log.error("Could not write output");
            }
        }
    }
}
