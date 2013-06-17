package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

/**
 * All XATMI services must implement the tpservice method.
 */
public interface Service {
    /**
     * This is a method that should be implemented by classes to provide the service behavior
     * 
     * @param svcinfo The inbound parameters
     * @return A response to the client
     * @throws ConnectionException If the service routine did not handle the request correctly
     * @throws ConfigurationException If the configuration cannot be read correctly
     */
    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException;
}
