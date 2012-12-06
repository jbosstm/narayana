package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import org.jboss.narayana.txframework.impl.handlers.wsat.WSATParticipantRegistry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

/**
 * @author paul.robinson@redhat.com, 2012-04-10
 */
public class RESTATHandler implements ProtocolHandler {

    private ServiceInvocationMeta serviceInvocationMeta;

    private static final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();

    public RESTATHandler(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException {
        this.serviceInvocationMeta = serviceInvocationMeta;

        registerParticipant(serviceInvocationMeta);
    }

    @Override
    public Object proceed(InvocationContext ic) throws Exception {
        return ic.proceed();
    }

    @Override
    public void notifySuccess() {
        //do nothing
    }

    @Override
    public void notifyFailure() {
        //Todo: ensure transaction rolled back
    }

    private void registerParticipant(ServiceInvocationMeta serviceInvocationMeta) throws ParticipantRegistrationException {

        HttpServletRequest req = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String enlistUrl = req.getHeader("enlistURL");
        UriInfo info = ResteasyProviderFactory.getContextData(UriInfo.class);
        String txid = getTransactionId(enlistUrl);
        
        synchronized (participantRegistry) {

            //Only create participant if there is not already a participant for this ServiceImpl and this transaction
            if (!participantRegistry.isRegistered(txid, serviceInvocationMeta.getServiceClass())) {
                RestParticipantEndpointImpl.enlistParticipant(txid, info, enlistUrl, serviceInvocationMeta);
                //todo: need to remove this when done.
                participantRegistry.register(txid, serviceInvocationMeta.getServiceClass());
            }
        }
    }

    private static String getTransactionId(String enlistUrl) {
        String[] parts = enlistUrl.split("/");
        return parts[parts.length - 1];
    }

}
