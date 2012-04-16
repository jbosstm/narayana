package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.narayana.txframework.api.annotation.management.TxManagement;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.api.management.ATTxControl;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import org.jboss.narayana.txframework.impl.handlers.wsat.ATTxControlImpl;
import org.jboss.narayana.txframework.impl.handlers.wsat.WSATParticipantRegistry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com, 2012-04-10
 */
public class RESTATHandler implements ProtocolHandler {

    private Object serviceImpl;
    private ATTxControl atTxControl;

    private static final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();

    public RESTATHandler(Object serviceImpl, Method serviceMethod) throws TXFrameworkException {
        this.serviceImpl = serviceImpl;

        registerParticipant(serviceImpl);

        atTxControl = new ATTxControlImpl();
        injectTxManagement(atTxControl);
    }

    //todo: Use CDI to do injection
    private void injectTxManagement(ATTxControl atTxControl) throws TXFrameworkException {
        Field[] fields = serviceImpl.getClass().getFields();
        for (Field field : serviceImpl.getClass().getFields()) {
            if (field.getAnnotation(TxManagement.class) != null) {
                try {
                    //todo: check field type
                    field.set(serviceImpl, atTxControl);
                } catch (IllegalAccessException e) {
                    throw new TXFrameworkException("Unable to inject TxManagement impl to field '" + field.getName() + "' on '" + serviceImpl.getClass().getName() + "'", e);
                }
            }
        }
        //didn't find an injection point. No problem as this is optional
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

    private void registerParticipant(Object participant) throws ParticipantRegistrationException {

        HttpServletRequest req = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
        String enlistUrl = req.getHeader("enlistURL");
        UriInfo info = ResteasyProviderFactory.getContextData(UriInfo.class);
        String txid = getTransactionId(enlistUrl);
        
        synchronized (participantRegistry) {

            //Only create participant if there is not already a participant for this ServiceImpl and this transaction
            if (!participantRegistry.isRegistered(txid, participant.getClass())) {
                RestParticipantEndpointImpl.enlistParticipant(txid, info, enlistUrl, participant);
                //todo: need to remove this when done.
                participantRegistry.register(txid, participant.getClass());
            }
        }
    }

    private static String getTransactionId(String enlistUrl) {
        String[] parts = enlistUrl.split("/");
        return parts[parts.length - 1];
    }

}
