package org.jboss.jbossts.txframework.api.configuration.service;

/**
 * specifies values taken by the requestType field in {@link org.jboss.jbossts.txframework.api.annotation.service.ServiceRequest#requestType()}annotations
 */
public enum RequestType
{
    /**
     * this value indicates that a service request method will never make changes to data.
     */
    READ_ONLY,
    /**
     * this value indicates that a service request method may make changes to data and will employ an
     * injected control, where necessary, to notify read only status for a specific invocation.
     */
    MODIFY
}
