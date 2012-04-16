package org.jboss.narayana.txframework.functional.rest.at.simpleEJB;

import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.impl.handlers.restat.client.Required;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;

/**
 * @Author paul.robinson@redhat.com 09/04/2012
 */
public class Client {

    // construct the endpoint for the example web service that will take part in a transaction
    private static final int SERVICE_PORT = 8080;
    private static final String SERVICE_URL = "http://localhost:" + SERVICE_PORT + "/test";

    @Required
    public void invoke() throws Exception{

        Service1 service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        Service2 service2 = ProxyFactory.create(Service2.class, SERVICE_URL);

        ClientResponse response = (ClientResponse) service1.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

        response = (ClientResponse) service2.someServiceRequest(Service1.VOTE_COMMIT);
        response.releaseConnection();

    }

    public String getEventLog() {
        Service1 service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        ClientResponse response = (ClientResponse<EventLog>) service1.getEventLog();
        return (String) response.getEntity(String.class);
    }

    public void clearLogs() {

        Service1 service1 = ProxyFactory.create(Service1.class, SERVICE_URL);
        ClientResponse response = (ClientResponse) service1.clearLogs();
        response.releaseConnection();
    }
}
