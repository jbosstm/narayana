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
 * (C) 2005-2009,
 * @author JBoss Inc.
 */
/*
 * BasicClient.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id: BasicClient.java,v 1.10 2004/12/02 16:52:58 kconner Exp $
 *
 */
package org.jboss.jbossts.txbridge.demo.client;

import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import com.arjuna.wst.TransactionRolledBackException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.naming.InitialContext;
import javax.naming.Context;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * A very basic client application that drives the tx bridge demo.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class BasicClient extends HttpServlet
{
    private ServletContext context;

    private Bistro bistro;

    /**
     * Initialise the servlet.
     * @param config The servlet configuration.
     */
    public void init(final ServletConfig config)
        throws ServletException
    {
        try
        {
            URL wsdlLocation = new URL("http://localhost:8080/txbridge-demo-service/BistroImpl?wsdl");
            QName serviceName = new QName("http://bistro.demo.txbridge.jbossts.jboss.org/", "BistroImplService");
            QName portName = new QName("http://bistro.demo.txbridge.jbossts.jboss.org/", "BistroImplPort");

            Service service = Service.create(wsdlLocation, serviceName);
            bistro = service.getPort(portName, Bistro.class);

            // we could have used @HandlerChain but it's nice to show a bit of variety...
            BindingProvider bindingProvider = (BindingProvider)bistro;
            List<Handler> handlers = new ArrayList<Handler>(1);
            handlers.add(new JaxWSHeaderContextProcessor());
            bindingProvider.getBinding().setHandlerChain(handlers);

            context = config.getServletContext();

        }
        catch(Exception e)
        {
            throw new ServletException(e);
        }
    }

    /**
     * Simple wrapper to allow our test method to be invoked when
     * running in a servlet container, taking parameters from the
     * request URL and displaying the outcome on the resulting html page.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // get business logic params from the form submission.
        int numberOfSeats = Integer.parseInt(request.getParameter("seats"));
        String txType = request.getParameter("txType");

        String result = "Transaction finished OK.";

        try
        {
            if("AtomicTransaction".equals(txType))
            {
                testAtomicTransaction(numberOfSeats);
            }
            else if("JTA".equals(txType))
            {
                testJTATransaction(numberOfSeats);
            }
            else
            {
                result = "Unknown transaction type " + txType;
            }
        }
        catch (final TransactionRolledBackException tre)
        {
            result = "Transaction rolled back." ;
            System.out.println("Transaction rolled back") ;
        }
        catch (Exception e)
        {
            result = "Transaction failed! Cause: " + e.toString();
            System.out.println("CLIENT: problem: ");
            e.printStackTrace(System.out);
        }

        request.setAttribute("result", result);
        context.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    /**
     * Run a simple WS-AT transaction involving a transactional Web Service
     * that is configured with the inbound bridge. This client uses WS-AT type
     * transaction handling, whilst the service understands JTA (XA) type.
     * <p/>
     * Note: due to the implementation of the transaction infrastructure,
     * this method must be invoked with a web application container. It will
     * not run correctly in a standalone java application.
     *
     * @param numberOfSeats the required number of seats
     * @throws Exception for any unexpected errors, such as a failure to commit.
     */
    private void testAtomicTransaction(int numberOfSeats) throws Exception
    {
        System.out.println("CLIENT: obtaining userTransaction...");

        com.arjuna.mw.wst11.UserTransaction ut = UserTransactionFactory.userTransaction();

        System.out.println("CLIENT: starting the transaction...");

        ut.begin();

        System.out.println("CLIENT: transaction ID= " + ut.toString());

        System.out.println("CLIENT: calling business Web Services...");

        //////////////////////

        System.out.println("CLIENT: bookingCount: "+bistro.getBookingCount());

        bistro.bookSeats(numberOfSeats);

        System.out.println("CLIENT: bookingCount: "+bistro.getBookingCount());

        //////////////////////

        System.out.println("CLIENT: calling commit on the transaction...");

        ut.commit();

        System.out.println("done.");
        System.out.flush();
    }

    /**
     * Run a simple JTA transaction involving a transactional Web Service,
     * with communication passing though the outbound bridge. This client
     * uses JTA (XA) type transaction handling, whilst the service understands
     * WS-AT type only.
     * <p/>
     * Note: due to the implementation of the transaction infrastructure,
     * this method must be invoked with a web application container. It will
     * not run correctly in a standalone java application.
     *
     * @param numberOfSeats the required number of seats
     * @throws Exception for any unexpected errors, such as a failure to commit.
     */
    private void testJTATransaction(int numberOfSeats) throws Exception
    {
        System.out.println("CLIENT: Obtaining userTransaction...");

        Context initialContext = new InitialContext();
        javax.transaction.UserTransaction ut = (javax.transaction.UserTransaction)initialContext.lookup("java:comp/UserTransaction");

        System.out.println("CLIENT: starting the transaction...");

        ut.begin();

        System.out.println("CLIENT: transaction ID= "+ ut);

        // we reuse the existing WS-AT aware service from the XTS demo app
        URL wsdlLocation = new URL("http://localhost:8080/xtsdemowebservices/RestaurantServiceAT?wsdl");
        QName serviceName = new QName("http://www.jboss.com/jbosstm/xts/demo/Restaurant", "RestaurantServiceATService");
        Service service = Service.create(wsdlLocation, serviceName);

        // use a modified client interface with @HandlerChain configured on it.
        Restaurant restaurant = service.getPort(Restaurant.class);

        System.out.println("CLIENT: calling business Web Services...");

        restaurant.bookSeats(numberOfSeats);

        System.out.println("CLIENT: calling commit on the transaciton...");

        ut.commit();

        System.out.println("done");
        System.out.flush();
    }
}
