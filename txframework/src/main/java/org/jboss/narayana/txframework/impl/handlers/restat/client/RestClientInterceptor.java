package org.jboss.narayana.txframework.impl.handlers.restat.client;

import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import javax.ws.rs.ext.Provider;

/**
 * @Author paul.robinson@redhat.com 08/04/2012
 */
@ClientInterceptor
@Provider
public class RestClientInterceptor implements ClientExecutionInterceptor {

    public ClientResponse execute(ClientExecutionContext clientExecutionContext) throws Exception {

        //Add getDurableParticipantEnlistmentURI if a REST-TX is running
        TxSupport txSupport = UserTransaction.getTXSupport();
        if (txSupport != null) {
            String enlistURL = txSupport.getDurableParticipantEnlistmentURI();
            clientExecutionContext.getRequest().header("enlistURL", enlistURL);
        }

        return clientExecutionContext.proceed();
    }
}
