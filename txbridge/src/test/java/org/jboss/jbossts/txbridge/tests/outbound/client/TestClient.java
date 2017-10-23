/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.outbound.client;

import com.arjuna.ats.jta.exceptions.RollbackException;
import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import org.jboss.logging.Logger;
import org.jboss.jbossts.txbridge.outbound.JaxWSTxOutboundBridgeHandler;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet which includes test methods for exercising the txbridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
@WebServlet(name="Outbound Test Client Servlet", urlPatterns=TestClient.URL_PATTERN)
public class TestClient extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(TestClient.class);

    public static final String URL_PATTERN = "/testclient";

    private UserTransaction userTransaction;
    private ServletContext context;
    private TestService testService;

    /**
     * Initialise the servlet.
     *
     * @param config The servlet configuration.
     */
    public void init(final ServletConfig config)
            throws ServletException {
        try {
            Context ic = new InitialContext();
            userTransaction = (UserTransaction) ic.lookup("java:comp/UserTransaction");

            URL wsdlLocation = new URL("http://" + getLocalHost() + ":8080/txbridge-outbound-tests-service/TestServiceImpl?wsdl");
            QName serviceName = new QName("http://client.outbound.tests.txbridge.jbossts.jboss.org/", "TestServiceImplService");

            Service service = Service.create(wsdlLocation, serviceName);
            testService = service.getPort(TestService.class);

            BindingProvider bindingProvider = (BindingProvider) testService;
            List<Handler> handlers = new ArrayList<Handler>(2);
            handlers.add(new JaxWSTxOutboundBridgeHandler());
            handlers.add(new JaxWSHeaderContextProcessor());
            bindingProvider.getBinding().setHandlerChain(handlers);

            context = config.getServletContext();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String result = "OK";
        try {
            log.info("starting the transaction...");

            userTransaction.begin();

            log.info("transaction ID= " + userTransaction.toString());

            log.info("calling business Web Services...");

            testService.doNothing();

            log.info("terminating the transaction...");

            terminateTransaction(false);
        } catch (final RollbackException re) {
            log.info("Transaction rolled back");
            result = re.getClass().getName();
        } catch (Exception e) {
            log.info("problem: ", e);
            result = e.getClass().getName();
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.println("finished : " + result);
        out.close();
    }

    private void terminateTransaction(boolean shouldCommit) throws Exception {
        log.info("shouldCommit=" + shouldCommit);

        if (shouldCommit) {
            userTransaction.commit();
        } else {
            userTransaction.rollback();
        }
    }

    static String getLocalHost() {
        return isIPv6() ? "[::1]" : "localhost";
    }

    static boolean isIPv6() {
        try {
            if (InetAddress.getLocalHost() instanceof Inet6Address || System.getenv("IPV6_OPTS") != null)
                return true;
        } catch (final UnknownHostException uhe) {
        }

        return false;
    }
}
