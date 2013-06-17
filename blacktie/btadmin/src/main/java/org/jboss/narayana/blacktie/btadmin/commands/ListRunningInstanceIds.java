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

import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.administration.BlacktieAdministration;
import org.jboss.narayana.blacktie.btadmin.Command;
import org.jboss.narayana.blacktie.btadmin.CommandHandler;
import org.jboss.narayana.blacktie.btadmin.IncompatibleArgsException;

/**
 * List the running instance ids of a server.
 */
public class ListRunningInstanceIds implements Command {
    /**
     * The logger to use for output
     */
    private static Logger log = LogManager.getLogger(ListRunningInstanceIds.class);

    /**
     * The server name.
     */
    private String serverName;

    /**
     * Get the usage of the command.
     */
    public String getQuickstartUsage() {
        return "<serverName>";
    }

    /**
     * Initialize the arguments for the command
     */
    public void initializeArgs(String[] args) throws IncompatibleArgsException {
        serverName = args[0];
    }

    public void invoke(BlacktieAdministration connection, Properties configuration) {
        List<Integer> ids = connection.listRunningInstanceIds(serverName);
        log.info(CommandHandler.convertList("listRunningInstanceIds", ids));
    }

}
