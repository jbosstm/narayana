/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
