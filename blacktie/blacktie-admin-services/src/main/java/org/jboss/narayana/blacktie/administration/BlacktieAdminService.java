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

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.administration.core.AdministrationProxy;

/**
 * This is the JMX interface into the blacktie administration proxy.
 */
public class BlacktieAdminService implements BlacktieAdminServiceMBean {
    private static final Logger log = LogManager.getLogger(BlacktieAdminService.class);
    //private QueueReaper reaper;
    private AdministrationProxy administrationProxy;

    /**
     * Start the service
     */
    public void start() throws Exception {
        administrationProxy = new AdministrationProxy();
        //reaper = new QueueReaper(administrationProxy);
        //reaper.startThread();

        administrationProxy.onConstruct();
        log.info("Admin Server Started");
    }

    /**
     * Stop the service
     */
    public void stop() throws Exception {
        //reaper.stopThread();
        administrationProxy.close();
        log.info("Admin Server Stopped");
    }
    
    /**
     * Retrieve the domain name
     */
    public String getDomainName() {
        return administrationProxy.getDomainName();
    }

    /**
     * Get the version of the blacktie software
     */
    public String getSoftwareVersion() {
        return administrationProxy.getSoftwareVersion();
    }

    /**
     * Get domain status
     */
    public Boolean getDomainStatus() {
        return administrationProxy.getDomainStatus();
    }

    /**
     * Pause the domain
     */
    public Boolean pauseDomain() {
        return administrationProxy.pauseDomain();
    }

    /**
     * Resume the domain
     */
    public Boolean resumeDomain() {
        return administrationProxy.resumeDomain();
    }

    /**
     * List the servers
     */
    public List<String> getServerList() {
        return administrationProxy.getServerList();
    }

    /**
     * List the running servers
     */
    public List<String> listRunningServers() {
        return administrationProxy.listRunningServers();
    }

    /**
     * List the running ids of a specific server
     * 
     * @param serverName The name of the server
     */
    public List<Integer> listRunningInstanceIds(String serverName) {
        return administrationProxy.listRunningInstanceIds(serverName);
    }

    /**
     * Get the servers status for the domain
     */
    public String listServersStatus() {
        return administrationProxy.listServersStatus();
    }

    /**
     * List the service status for a service
     * 
     * @param serverName The name of the server
     * @param serviceName The name of the service
     */
    public String listServiceStatus(String serverName, String serviceName) {
        return administrationProxy.listServiceStatus(serverName, serviceName);
    }

    /**
     * Advertise a new service
     * 
     * @param serverName The name of the server
     * @param serviceName The name of the service
     */
    public Boolean advertise(String serverName, String serviceName) {
        return administrationProxy.advertise(serverName, serviceName);
    }

    /**
     * Unadvertise a new service
     * 
     * @param serverName The name of the server
     * @param serviceName The name of the service
     */
    public Boolean unadvertise(String serverName, String serviceName) {
        return administrationProxy.unadvertise(serverName, serviceName);
    }

    /**
     * Shutdown a server
     * 
     * @param serverName The name of the server
     * @param id The id of the server
     */
    public Boolean shutdown(String serverName, int id) {
        return administrationProxy.shutdown(serverName, id);
    }

    /**
     * Get the service counter and restrict it to a certain server, 0 for all.
     * 
     * @param serverName The name of the server
     * @param id The id of the server
     * @param serviceName The name of the service
     */
    public long getServiceCounterById(String serverName, int id, String serviceName) {
        return administrationProxy.getServiceCounterById(serverName, id, serviceName);
    }

    /**
     * Get the service counter for the domain.
     * 
     * @param serverName The name of the server
     * @param serviceName The name of the service
     */

    public long getServiceCounter(String serverName, String serviceName) {
        return administrationProxy.getServiceCounter(serverName, serviceName);
    }

    /**
     * Reload the domain
     */
    public Boolean reloadDomain() {
        return administrationProxy.reloadDomain();
    }

    /**
     * Reload the server (causes the server to update its configuration and restart.
     * 
     * @param serverName The name of the server
     */
    public Boolean reloadServer(String serverName) {
        return administrationProxy.reloadServer(serverName);
    }

    /**
     * List the status of a service giving an optional id, 0 is all servers.
     * 
     * @param serverName The name of the server
     * @param id The id of the server
     * @param serviceName The name of the service
     */
    public String listServiceStatusById(String serverName, int id, String serviceName) {
        return administrationProxy.listServiceStatusById(serverName, id, serviceName);
    }

    /**
     * Get response time of service
     */
    public String getResponseTimeById(String serverName, int id, String serviceName) {
        return administrationProxy.getResponseTimeById(serverName, id, serviceName);
    }

    public String getResponseTime(String serverName, String serviceName) {
        return administrationProxy.getResponseTime(serverName, serviceName);
    }

    /**
     * Get message queue depth
     */
    public int getQueueDepth(String serverName, String serviceName) {
        return administrationProxy.getQueueDepth(serverName, serviceName);
    }

    /**
     * Get the server name
     */
    public String getServerName(String serviceName) {
        return administrationProxy.getServerName(serviceName);
    }

    /**
     * Get error counter for service
     */
    public long getErrorCounter(String serverName, String serviceName) {
        return administrationProxy.getErrorCounter(serverName, serviceName);
    }

    public long getErrorCounterById(String serverName, int id, String serviceName) {
        return administrationProxy.getErrorCounterById(serverName, id, serviceName);
    }

    public String getServerVersionById(String serverName, int id) {
        return administrationProxy.getServerVersionById(serverName, id);
    }
}
