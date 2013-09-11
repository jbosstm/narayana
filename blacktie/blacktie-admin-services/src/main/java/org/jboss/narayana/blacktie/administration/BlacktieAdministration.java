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
package org.jboss.narayana.blacktie.administration;

/**
 * This is the Admin service operations.
 * 
 * Don't forget that all the operations need to be in the select statement of the Blacktie XATMI Administration Service
 */
public interface BlacktieAdministration {

    /**
     * Ensures we dont return zero length responses
     */
    public String LIST_TERMINATOR = "|";

    /**
     * Retrieve the Domain Name
     */
    public String getDomainName();

    /**
     * Retrieve the software version of domain
     */
    public String getSoftwareVersion();

    /**
     * Retrieve the server version
     */
    public String getServerVersionById(String serverName, int id);

    /**
     * Discover running servers
     */
    public java.util.List<String> listRunningServers();

    /**
     * Get current status of domain
     */
    public Boolean getDomainStatus();

    /**
     * This calls pauseServer for each server in the domain
     */
    public Boolean pauseDomain();

    /**
     * This calls resumeDomain for each server in the domain
     */
    public Boolean resumeDomain();

    /**
     * Halt servers, update configuration, restart
     */
    public Boolean reloadDomain();

    /**
     * reload server
     */
    public Boolean reloadServer(String serverName);

    /**
     * Get the name of the server that this service resides at.
     * 
     * @param serviceName The service name.
     * @return The server name.
     */
    public String getServerName(String serviceName);

    /**
     * Retrieves the counter for a service from all servers
     */
    public long getServiceCounter(String serverName, String serviceName);

    /**
     * Retrieves the counter for a service from specify server
     */
    public long getServiceCounterById(String serverName, int id, String serviceName);

    /**
     * Get the list of Ids of currently running servers
     */
    public java.util.List<Integer> listRunningInstanceIds(String serverName);

    /**
     * Describe the status of the servers in the domain
     */
    public String listServersStatus();

    /**
     * Describe the service status of server
     */
    public String listServiceStatus(String serverName, String serviceName);

    public String listServiceStatusById(String serverName, int id, String serviceName);

    /**
     * Advertise service
     */
    public Boolean advertise(String serverName, String serviceName);

    /**
     * Unadvertise service
     */
    public Boolean unadvertise(String serverName, String serviceName);

    /**
     * Shutdown server
     */
    public Boolean shutdown(String serverName, int id);

    /**
     * Get service response time
     */
    public String getResponseTimeById(String serverName, int id, String serviceName);

    public String getResponseTime(String serverName, String serviceName);

    /**
     * Retrieves the error counter for a service from all servers
     */
    public long getErrorCounter(String serverName, String serviceName);

    /**
     * Retrieves the error counter for a service from specify server
     */
    public long getErrorCounterById(String serverName, int id, String serviceName);
}
