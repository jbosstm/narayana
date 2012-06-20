package org.jboss.narayana.txframework.functional.clients;
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import org.jboss.narayana.txframework.functional.interfaces.AT;
import org.jboss.narayana.txframework.functional.interfaces.JAXWSHandlerAnnotation;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JAXWSHandlerAnnotationClient {

    public static JAXWSHandlerAnnotation newInstance() throws Exception {

        URL wsdlLocation = new URL("http://localhost:8080/test/JAXWSHandlerAnnotatonService/JAXWSHandlerAnnotatonImpl?wsdl");
        QName serviceName = new QName("http://www.jboss.com/functional/JAXWSHandlerAnnotatonImpl", "JAXWSHandlerAnnotatonService");
        QName portName = new QName("http://www.jboss.com/functional/JAXWSHandlerAnnotatonImpl", "JAXWSHandlerAnnotatonPort");

        Service service = Service.create(wsdlLocation, serviceName);
        JAXWSHandlerAnnotation client = service.getPort(portName, JAXWSHandlerAnnotation.class);

        return client;
    }
}

