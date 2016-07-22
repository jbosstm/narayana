/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.integration.archive;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

@WebServiceClient(name = TestService.SERVICE_NAME, targetNamespace = TestService.NAMESPACE)
public class TestServiceService extends Service {

    private final static URL HOTELSERVICEBASERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(TestServiceService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = TestServiceService.class.getResource(".");
            url = new URL(baseUrl, "/WEB-INF/wsdl/" + TestService.PORT_NAME + ".wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: '/WEB-INF/wsdl/" + TestService.PORT_NAME
                    + ".wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        HOTELSERVICEBASERVICE_WSDL_LOCATION = url;
    }

    public TestServiceService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public TestServiceService() {
        super(HOTELSERVICEBASERVICE_WSDL_LOCATION, new QName(TestService.NAMESPACE, TestService.SERVICE_NAME));
    }

    @WebEndpoint(name = TestService.PORT_NAME)
    public TestService getHotelService() {
        return super.getPort(new QName(TestService.NAMESPACE, TestService.PORT_NAME), TestService.class);
    }

}
