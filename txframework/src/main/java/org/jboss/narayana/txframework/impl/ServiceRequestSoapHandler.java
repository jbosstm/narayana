package org.jboss.narayana.txframework.impl;

import javax.ejb.Stateless;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.handlers.HandlerFactory;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Reference;

public class ServiceRequestSoapHandler implements SOAPHandler<SOAPMessageContext> {

    private ProtocolHandler interceptor;

    private boolean shouldIntercept = false;

    public Set<QName> getHeaders() {

        System.out.println("ServiceRequestSoapHandler.getHeaders()");
        return new HashSet<QName>();
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {

        Boolean outbound = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outbound) {
            return handleOutboundMessage(soapMessageContext);
        } else {
            return handleInboundMessage(soapMessageContext);
        }
    }

    private boolean handleInboundMessage(SOAPMessageContext soapMessageContext) {
        System.out.println("ServiceRequestSoapHandler.handleInboundMessage()");

        Object instance = getInstance(soapMessageContext);
        Method method = getMethod(soapMessageContext);

        shouldIntercept = !isEJB(instance) && isServiceRequestMethod(method);

        if (shouldIntercept) {
            try {
                interceptor= HandlerFactory.createInstance(instance, method);
            } catch (TXFrameworkException e) {
                //todo: use logger
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }


    private boolean isServiceRequestMethod(Method method)
    {
        try {
            ServiceRequest serviceRequest = method.getAnnotation(ServiceRequest.class);
            return serviceRequest != null;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isEJB(Object instance)
    {
        try {
            Stateless stateless = instance.getClass().getAnnotation(Stateless.class);
            return stateless != null;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleOutboundMessage(SOAPMessageContext soapMessageContext) {
        System.out.println("ServiceRequestSoapHandler.handleOutboundMessage()");

        if (shouldIntercept) {
            try {
                interceptor.notifySuccess();
            } catch (TXFrameworkException e) {
                //todo: use logger
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        System.out.println("ServiceRequestSoapHandler.handleFault()");

        if (shouldIntercept) {
            try {
                interceptor.notifyFailure();
            } catch (TXFrameworkException e) {
                //todo: use logger
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void close(MessageContext messageContext) {
        System.out.println("ServiceRequestSoapHandler.close()");
    }

    private Object getInstance(MessageContext ctx) {

        Endpoint endpoint = ((WrappedMessageContext) ctx).getWrappedMessage().getExchange().get(Endpoint.class);
        Reference ref = endpoint.getInstanceProvider().getInstance(endpoint.getTargetBeanName());
        return ref.getValue();
    }

    private Method getMethod(MessageContext ctx) {

        Exchange exchange = ((WrappedMessageContext) ctx).getWrappedMessage().getExchange();
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        MethodDispatcher md = (MethodDispatcher) exchange.get(Service.class).get(MethodDispatcher.class.getName());
        return md.getMethod(bop);
    }
}
