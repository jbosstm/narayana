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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
 * The shutdown command will shutdown the server specified. This method is non-blocking, i.e. the server is requested to
 * shutdown, it will still be alive possibly
 */
public class Shutdown implements Command {
    /**
     * The logger to use for output
     */
    private static Logger log = LogManager.getLogger(Shutdown.class);

    /**
     * The name of the server.
     */
    private String serverName;

    /**
     * The ID of the server, will be 0 (all) if not provided
     */
    private int id = 0;

    /**
     * The Operation System type which used to check server alive
     */
    private static String OS = System.getProperty("os.name").toLowerCase();

    private int checkPeriodMillis = 2 * 1000;
    private int numChecks = 10;

    /**
     * Show the usage of the command
     */
    public String getQuickstartUsage() {
        return "[<serverName> [<serverId>]]";
    }

    public void initializeArgs(String[] args) throws IncompatibleArgsException {
        if (args.length > 0) {
            serverName = args[0];
            if (args.length == 2) {
                try {
                    id = Integer.parseInt(args[1]);
                    log.trace("Successfully parsed: " + args[1]);
                } catch (NumberFormatException nfe) {
                    throw new IncompatibleArgsException(
                            "The third argument was expected to be the (integer) instance id to shutdown");
                }
            }
        }
    }

    public void invoke(BlacktieAdministration connection, Properties configuration) throws CommandFailedException {
        List<ServerToStop> serversToStop = new ArrayList<ServerToStop>();

        List<Server> serverLaunchers = (List<Server>) configuration.get("blacktie.domain.serverLaunchers");
        if (serverName == null) {
            Iterator<Server> launchers = serverLaunchers.iterator();
            while (launchers.hasNext()) {
                Server server = launchers.next();
                Iterator<Machine> iterator2;
                try {
                    iterator2 = server.getLocalMachine().iterator();
                } catch (UnknownHostException e) {
                    log.error("Could not get the local machine");
                    throw new CommandFailedException(-1);
                }
                while (iterator2.hasNext()) {
                    Machine machine = iterator2.next();
                    ServerToStop serverToStop = new ServerToStop();
                    serverToStop.setName(server.getName());
                    serverToStop.setId(machine.getServerId());
                    serversToStop.add(serverToStop);
                }
            }
        } else {
            ServerToStop serverToStop = new ServerToStop();
            serverToStop.setName(serverName);
            serverToStop.setId(id);
            serversToStop.add(serverToStop);
        }
        if (serversToStop.size() != 0) {
            Iterator<ServerToStop> iterator = serversToStop.iterator();
            while (iterator.hasNext()) {
                ServerToStop next = iterator.next();
                String name = next.getName();
                int id = next.getId();
                Boolean result = connection.shutdown(name, id);
                boolean shutdown = false;
                if (result) {
                    log.info("Server shutdown successfully: " + name + " with id: " + id);
                    log.info("waiting for " + name + ":" + id + " shutdown complete");
                    for (int i = 0; i < numChecks; i++) {
                        if(checkServerAlive(name, id)) {
                            try{
                                Thread.sleep(checkPeriodMillis);
                            } catch (Exception e) {}
                            log.info(name + ":" + id + " is still alive, sleeping for a further " + checkPeriodMillis + "ms");
                        }  else {
                            log.info(name + ":" + id + " shutdown complete");
                            shutdown = true;
                            break;
                        }
                    }
                    if(shutdown == false) {
                        log.error(name + ":" + id + " has not shutdown complete");
                        throw new CommandFailedException(-1);
                    }
                } else {
                    log.error("Server could not be shutdown (may already be stopped)");
                    throw new CommandFailedException(-1);
                }
            }
        } else {
            log.error("No servers were configured for shutdown");
            throw new CommandFailedException(-1);

        }
    }

    private boolean checkServerAlive(String name, int id) {
        String cmd = null;
        ProcessBuilder pb = null;
        boolean toReturn = false;
        if(OS.indexOf("linux") >= 0){
            log.debug(OS + " check for linux");
            if(id != 0) {
                cmd = "ps -ef | grep \"\\\\-i " + id + " \\\\-s " + name + "\" | grep -v grep";
            } else {
                cmd = "ps -ef | grep \"\\\\-s " + name + "\" | grep -v grep";
            }
            log.debug(cmd);
            pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        } else if(OS.indexOf("win") >= 0) {
            log.debug(OS + " check for windows ");
            if(id != 0) {
                cmd = "wmic process get commandline | findstr /c:\"\\-i " + id + " \\-s " + name + "\" | findstr /v findstr";
            } else {
                cmd = "wmic process get commandline | findstr /c:\"\\-s " + name + "\" | findstr /v findstr";
            }
            log.debug(cmd);
            pb = new ProcessBuilder("cmd", "/c", cmd);
        } else {
            log.warn(OS + " no check");
            return false;
        }

        try {
            Process p = pb.start();
            if(OS.indexOf("win") >= 0) {
                //wmic is blocking for stdin. so we need to close it.
                p.getOutputStream().close();
            }
            p.waitFor();
            if(p.exitValue() == 0) {
                log.debug("check cmd " + cmd + " output");
                InputStream is = p.getInputStream();
                BufferedReader ein = new BufferedReader(new InputStreamReader(is));
                String res = ein.readLine();
                if(res != null) {
                    log.debug("cmd " + cmd + " output: " + res);
                    toReturn = true; 
                }
                is.close();
            }
        } catch (Exception e) {
            log.error("run " + cmd + " failed with " + e);
        }
        return toReturn;
    }

    private class ServerToStop {
        private String name;
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
