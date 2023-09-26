/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.integration.archive;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
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