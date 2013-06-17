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

import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.administration.BlacktieAdministration;
import org.jboss.narayana.blacktie.btadmin.Command;
import org.jboss.narayana.blacktie.btadmin.CommandFailedException;
import org.jboss.narayana.blacktie.btadmin.IncompatibleArgsException;

/**
 * The advertise command
 */
public class Advertise implements Command {
    /**
     * The logger to use for output
     */
    private static Logger log = LogManager.getLogger(Advertise.class);

    /**
     * The name of the server.
     */
    private String serverName;

    /**
     * The name of the service.
     */
    private String serviceName;

    /**
     * Show the usage of the command
     */
    public String getQuickstartUsage() {
        return "<serverName> <serviceName>";
    }

    public void initializeArgs(String[] args) throws IncompatibleArgsException {
        serverName = args[0];
        serviceName = args[1];
    }

    public void invoke(BlacktieAdministration connection, Properties configuration) throws CommandFailedException {
        Boolean advertise = connection.advertise(serverName, serviceName);
        if (advertise) {
            log.info("Service advertised");
        } else {
            log.error("Service could not be advertised");
            throw new CommandFailedException(-1);
        }
    }
}
