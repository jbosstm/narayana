package org.jboss.narayana.txframework.functional.common;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author paul.robinson@redhat.com, 2012-02-06
 */
public class SimpleHandler implements SOAPHandler<SOAPMessageContext> {

    private static int count = 0;

    public Set<QName> getHeaders() {
        count++;
        System.out.println("SimpleHandler.getHeaders()");
        return new HashSet<QName>();
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        count++;
        System.out.println("SimpleHandler.handleMessage()");
        return true;
    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        count++;
        System.out.println("SimpleHandler.handleFault()");
        return true;
    }

    public void close(MessageContext messageContext) {
        count++;
        System.out.println("SimpleHandler.close()");
    }

    public static int getCount() {
        return count;
    }
}
