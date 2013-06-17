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
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.jboss.narayana.blacktie.administration.BlacktieAdministration;

/**
 * All commands that can be invoked by the admin CLI tool must implement this interface.
 */
public interface Command {

    /**
     * Get an quickstart of the usage of the command.
     * 
     * @return A string showing the usage of the command
     */
    public String getQuickstartUsage();

    /**
     * This will initialize the arguments for the command, if the arguments are not sufficient it will raise an exception.
     * 
     * @param args The arguments as received on the command line
     * @throws IncompatibleArgsException If the arguments are invalid
     */
    public void initializeArgs(String[] args) throws IncompatibleArgsException;

    /**
     * Issue the command on the mbean server connection
     * 
     * @param configuration TODO
     * @param beanServerConnection The connection to use
     * @param blacktieAdmin The mbean to user
     * 
     * @throws InstanceNotFoundException If the mbean does not exist
     * @throws MBeanException If there is an mbean error
     * @throws ReflectionException Reflective errors
     * @throws IOException IPC errors
     */
    public void invoke(BlacktieAdministration connection, Properties configuration) throws CommandFailedException;
}
