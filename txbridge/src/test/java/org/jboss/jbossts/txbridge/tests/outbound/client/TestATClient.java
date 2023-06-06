/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.client;

import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import com.arjuna.mw.wst11.client.WSTXFeature;
import org.jboss.jbossts.txbridge.outbound.JTAOverWSATFeature;
import org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "Outbound Test Client AT Servlet", urlPatterns = TestATClient.URL_PATTERN)
public class TestATClient extends HttpServlet {

    public static final int CLIENT_WITHOUT_FEATURES = 1;

    public static final int CLIENT_WITH_JTA_FEATURE = 2;

    public static final int CLIENT_WITH_WSTX_FEATURE = 3;

    public static final int CLIENT_WITH_BOTH_FEATURES = 4;

    public static final int CLIENT_WITH_MANUAL_HANDLERS = 5;

    public static final String URL_PATTERN = "/testATClient";

    private static final long serialVersionUID = 1L;

    private URL wsatServiceWsdlLocation;

    private URL simpleServiceWsdlLocation;

    private QName wsatServiceName;

    private QName simpleServiceName;

    private UserTransaction userTransaction;

    public void init(final ServletConfig config) throws ServletException {
        try {
            Context ic = new InitialContext();
            userTransaction = (UserTransaction) ic.lookup("java:comp/UserTransaction");

            wsatServiceWsdlLocation = new URL("http://" + getLocalHost()
                    + ":8080/txbridge-outbound-tests-service/TestATServiceImpl?wsdl");
            simpleServiceWsdlLocation = new URL("http://" + getLocalHost()
                    + ":8080/txbridge-outbound-tests-service/TestNonATServiceImpl?wsdl");
        } catch (Exception e) {
            throw new ServletException(e);
        }

        wsatServiceName = new QName("http://client.outbound.tests.txbridge.jbossts.jboss.org/", "TestATServiceImplService");
        simpleServiceName = new QName("http://client.outbound.tests.txbridge.jbossts.jboss.org/", "TestNonATServiceImplService");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int clientType = Integer.parseInt(request.getParameter("clientType"));
        boolean isTransaction = Boolean.parseBoolean(request.getParameter("isTransaction"));
        boolean isCommit = Boolean.parseBoolean(request.getParameter("isCommit"));
        boolean isWSATService = Boolean.parseBoolean(request.getParameter("isWSATService"));
        boolean isJTAOverWSATFeatureEnabled = Boolean.parseBoolean(request.getParameter("isJTAOverWSATFeatureEnabled"));
        boolean isWSTXFeatureEnabled = Boolean.parseBoolean(request.getParameter("isWSTXFeatureEnabled"));

        CommonTestService service = getServiceByClientType(clientType, isWSATService, isJTAOverWSATFeatureEnabled,
                isWSTXFeatureEnabled);
        JsonArray result = executeTest(service, isTransaction, isCommit, clientType);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Json.createWriter(out).writeArray(result);
        out.close();
    }

    /**
     * NOTE: explicit checking of the client type is needed, because manually added handlers require active transaction in order
     * to make successful web service call.
     *
     * @param service
     * @param isTransaction
     * @param isCommit
     * @param clientType
     * @return
     */
    private JsonArray executeTest(CommonTestService service, boolean isTransaction, boolean isCommit, int clientType) {
        JsonArray invocations = null;

        if (clientType != CLIENT_WITH_MANUAL_HANDLERS) {
            service.reset();
        }

        try {
            if (isTransaction) {
                userTransaction.begin();
                if (clientType == CLIENT_WITH_MANUAL_HANDLERS) {
                    service.reset();
                }
            }

            service.doNothing();

            if (isTransaction && isCommit) {
                userTransaction.commit();
            } else if (isTransaction) {
                userTransaction.rollback();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (clientType != CLIENT_WITH_MANUAL_HANDLERS) {
            invocations = Json.createArrayBuilder(service.getTwoPhaseCommitInvocations()).build();
        } else {
            try {
                userTransaction.begin();
                invocations = Json.createArrayBuilder(service.getTwoPhaseCommitInvocations()).build();
                userTransaction.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return invocations;
    }

    private CommonTestService getServiceByClientType(int type, boolean isWSATService, boolean isJTAOverWSATFeatureEnabled,
            boolean isWSTXFeatureEnabled) {

        switch (type) {
            case CLIENT_WITHOUT_FEATURES:
                return getServiceWithoutFeatures(isWSATService);

            case CLIENT_WITH_JTA_FEATURE:
                return getServiceWithJTAOverWSATFeature(isWSATService, isJTAOverWSATFeatureEnabled);

            case CLIENT_WITH_WSTX_FEATURE:
                return null;

            case CLIENT_WITH_BOTH_FEATURES:
                return getServiceWithBothFeatures(isWSATService, isJTAOverWSATFeatureEnabled, isWSTXFeatureEnabled);

            case CLIENT_WITH_MANUAL_HANDLERS:
                return getServiceWithManualHandlers(isWSATService);

            default:
                throw new RuntimeException("Invalid client type");
        }
    }

    private CommonTestService getServiceWithoutFeatures(boolean isWSATService) {
        CommonTestService testService;

        if (isWSATService) {
            Service service = Service.create(wsatServiceWsdlLocation, wsatServiceName);
            testService = service.getPort(TestATService.class);
        } else {
            Service service = Service.create(simpleServiceWsdlLocation, simpleServiceName);
            testService = service.getPort(TestNonATService.class);
        }

        return testService;
    }

    private CommonTestService getServiceWithJTAOverWSATFeature(boolean isWSATService, boolean isJTAOverWSATFeatureEnabled) {
        CommonTestService testService;

        if (isWSATService) {
            Service service = Service.create(wsatServiceWsdlLocation, wsatServiceName);
            testService = service.getPort(TestATService.class, new JTAOverWSATFeature(isJTAOverWSATFeatureEnabled));
        } else {
            Service service = Service.create(simpleServiceWsdlLocation, simpleServiceName);
            testService = service.getPort(TestNonATService.class, new JTAOverWSATFeature(isJTAOverWSATFeatureEnabled));
        }

        return testService;
    }

    private CommonTestService getServiceWithBothFeatures(boolean isWSATService, boolean isJTAOverWSATFeatureEnabled,
            boolean isWSTXFeatureEnabled) {

        CommonTestService testService;

        if (isWSATService) {
            Service service = Service.create(wsatServiceWsdlLocation, wsatServiceName);
            testService = service.getPort(TestATService.class, new JTAOverWSATFeature(isJTAOverWSATFeatureEnabled),
                    new WSTXFeature(isWSTXFeatureEnabled));
        } else {
            Service service = Service.create(simpleServiceWsdlLocation, simpleServiceName);
            testService = service.getPort(TestNonATService.class, new JTAOverWSATFeature(isJTAOverWSATFeatureEnabled),
                    new WSTXFeature(isWSTXFeatureEnabled));
        }

        return testService;
    }

    private CommonTestService getServiceWithManualHandlers(boolean isWSATService) {
        CommonTestService testService;

        if (isWSATService) {
            Service service = Service.create(wsatServiceWsdlLocation, wsatServiceName);
            testService = service.getPort(TestATService.class);
        } else {
            Service service = Service.create(simpleServiceWsdlLocation, simpleServiceName);
            testService = service.getPort(TestNonATService.class);
        }

        BindingProvider bindingProvider = (BindingProvider) testService;
        List<Handler> handlers = new ArrayList<Handler>(2);
        handlers.add(new JaxWSTxOutboundBridgeHandler());
        handlers.add(new JaxWSHeaderContextProcessor());
        bindingProvider.getBinding().setHandlerChain(handlers);

        return testService;
    }

    private String getLocalHost() {
        return isIPv6() ? "[::1]" : "localhost";
    }

    private boolean isIPv6() {
        try {
            if (InetAddress.getLocalHost() instanceof Inet6Address || System.getenv("IPV6_OPTS") != null)
                return true;
        } catch (final UnknownHostException uhe) {
        }

        return false;
    }
}