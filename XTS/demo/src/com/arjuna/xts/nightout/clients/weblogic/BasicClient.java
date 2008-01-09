/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * BasicClient.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id$
 *
 */

package com.arjuna.xts.nightout.clients.weblogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;

import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mw.wst.UserBusinessActivityFactory;
import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.mw.wst.UserTransactionFactory;
import com.arjuna.mw.wst.client.JaxRPCHeaderContextProcessor;
import com.arjuna.xts.nightout.clients.weblogic.proxies.RestaurantServiceATPort;
import com.arjuna.xts.nightout.clients.weblogic.proxies.RestaurantServiceAT_Impl;
import com.arjuna.xts.nightout.clients.weblogic.proxies.RestaurantServiceBAPort;
import com.arjuna.xts.nightout.clients.weblogic.proxies.RestaurantServiceBA_Impl;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TaxiServiceATPort;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TaxiServiceAT_Impl;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TaxiServiceBAPort;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TaxiServiceBA_Impl;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TheatreServiceATPort;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TheatreServiceAT_Impl;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TheatreServiceBAPort;
import com.arjuna.xts.nightout.clients.weblogic.proxies.TheatreServiceBA_Impl;

/**
 * A very basic client application that drives the
 * (transactional) Web Services to arrange a night out.
 * <p/>
 * This is a reworking of the axis client.
 *
 * @author Kevin Conner (kevin.conner@arjuna.com)
 * @version $Id$
 */
public class BasicClient extends HttpServlet
{
    /**
     * The base namespace for the demo.
     */
    private static final String DEMO_BASE_NAMESPACE = "http://www.arjuna.com/xtsdemo/nightout/" ;
    /**
     * The atomic transaction restaurant service qname.
     */
    private static final QName RESTAURANT_SERVICE_AT_QNAME = new QName(DEMO_BASE_NAMESPACE + "RestaurantServiceAT", "RestaurantServiceATPort") ;
    /**
     * The business activity restaurant service qname.
     */
    private static final QName RESTAURANT_SERVICE_BA_QNAME = new QName(DEMO_BASE_NAMESPACE + "RestaurantServiceBA", "RestaurantServiceBAPort") ;
    /**
     * The atomic transaction taxi service qname.
     */
    private static final QName TAXI_SERVICE_AT_QNAME = new QName(DEMO_BASE_NAMESPACE + "TaxiServiceAT", "TaxiServiceATPort") ;
    /**
     * The business activity taxi service qname.
     */
    private static final QName TAXI_SERVICE_BA_QNAME = new QName(DEMO_BASE_NAMESPACE + "TaxiServiceBA", "TaxiServiceBAPort") ;
    /**
     * The atomic transaction theatre service qname.
     */
    private static final QName THEATRE_SERVICE_AT_QNAME = new QName(DEMO_BASE_NAMESPACE + "TheatreServiceAT", "TheatreServiceATPort") ;
    /**
     * The business activity theatre service qname.
     */
    private static final QName THEATRE_SERVICE_BA_QNAME = new QName(DEMO_BASE_NAMESPACE + "TheatreServiceBA", "TheatreServiceBAPort") ;
    
    /**
     * The servlet context.
     */
    private ServletContext context ;

    /**
     * The atomic transaction restaurant port.
     */
    private RestaurantServiceATPort restaurantAT;
    /**
     * The atomic transaction theatre port.
     */
    private TheatreServiceATPort theatreAT;
    /**
     * The atomic transaction taxi port.
     */
    private TaxiServiceATPort taxiAT;

    /**
     * The business activity restaurant port.
     */
    private RestaurantServiceBAPort restaurantBA;
    /**
     * The business activity theatre port.
     */
    private TheatreServiceBAPort theatreBA;
    /**
     * The business activity taxi port.
     */
    private TaxiServiceBAPort taxiBA;

    /**
     * URL of restaurant AT
     */
    private String restaurantATURL ;
    /**
     * URL of restaurant BA
     */
    private String restaurantBAURL ;
    /**
     * URL of taxi AT
     */
    private String taxiATURL ;
    /**
     * URL of taxi BA
     */
    private String taxiBAURL ;
    /**
     * URL of theatre AT
     */
    private String theatreATURL ;
    /**
     * URL of theatre BA
     */
    private String theatreBAURL ;

    /**
     * The initialised flag.
     */
    private boolean initialised ;

    /**
     * Initialise the servlet.
     * @param servletConfig The servlet configuration.
     */
    public void init(final ServletConfig config)
        throws ServletException
    {
        final String baseURL = "http://localhost:7001/xtsdemowebservices/" ;

        restaurantATURL = getURL(config, "restaurantATURL", baseURL + "RestaurantServiceAT?WSDL") ;
        restaurantBAURL = getURL(config, "restaurantBAURL", baseURL + "RestaurantServiceBA?WSDL") ;
        taxiATURL = getURL(config, "taxiATURL", baseURL + "TheatreServiceAT?WSDL") ;
        taxiBAURL = getURL(config, "taxiBAURL", baseURL + "TheatreServiceBA?WSDL") ;
        theatreATURL = getURL(config, "theatreATURL", baseURL + "TheatreServiceAT?WSDL") ;
        theatreBAURL = getURL(config, "theatreBAURL", baseURL + "TheatreServiceBA?WSDL") ;

        context = config.getServletContext();
    }

    /**
     * Initialise if necessary
     */
    private synchronized void initialise()
    throws ServletException
    {
        if (!initialised)
        {
            final List handlerList = new ArrayList() ;
            handlerList.add(new HandlerInfo(JaxRPCHeaderContextProcessor.class, null, new QName[] {JaxRPCHeaderContextProcessor.HEADER_QNAME})) ;
            try
            {
                final RestaurantServiceAT_Impl restaurantServiceAT_Impl = new RestaurantServiceAT_Impl(restaurantATURL) ;
                restaurantServiceAT_Impl.getHandlerRegistry().setHandlerChain(RESTAURANT_SERVICE_AT_QNAME, handlerList) ;
                restaurantAT = restaurantServiceAT_Impl.getRestaurantServiceATPort() ;
                
                final RestaurantServiceBA_Impl restaurantServiceBA_Impl = new RestaurantServiceBA_Impl(restaurantBAURL) ;
                restaurantServiceBA_Impl.getHandlerRegistry().setHandlerChain(RESTAURANT_SERVICE_BA_QNAME, handlerList) ;
                restaurantBA = restaurantServiceBA_Impl.getRestaurantServiceBAPort() ;
                
                final TaxiServiceAT_Impl taxiServiceAT_Impl = new TaxiServiceAT_Impl(taxiATURL) ;
                taxiServiceAT_Impl.getHandlerRegistry().setHandlerChain(TAXI_SERVICE_AT_QNAME, handlerList) ;
                taxiAT = taxiServiceAT_Impl.getTaxiServiceATPort() ;
                
                final TaxiServiceBA_Impl taxiServiceBA_Impl = new TaxiServiceBA_Impl(taxiBAURL) ;
                taxiServiceBA_Impl.getHandlerRegistry().setHandlerChain(TAXI_SERVICE_BA_QNAME, handlerList) ;
                taxiBA = taxiServiceBA_Impl.getTaxiServiceBAPort() ;
                
                final TheatreServiceAT_Impl theatreServiceAT_Impl = new TheatreServiceAT_Impl(theatreATURL) ;
                theatreServiceAT_Impl.getHandlerRegistry().setHandlerChain(THEATRE_SERVICE_AT_QNAME, handlerList) ;
                theatreAT = theatreServiceAT_Impl.getTheatreServiceATPort() ;
                
                final TheatreServiceBA_Impl theatreServiceBA_Impl = new TheatreServiceBA_Impl(theatreBAURL) ;
                theatreServiceBA_Impl.getHandlerRegistry().setHandlerChain(THEATRE_SERVICE_BA_QNAME, handlerList) ;
                theatreBA = theatreServiceBA_Impl.getTheatreServiceBAPort() ;
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
                throw new ServletException(ex);
            }
            initialised = true ;
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
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        initialise() ;

        // get business logic params from the form submission.
        int restaurantSeats = Integer.parseInt(request.getParameter("restaurant"));
        int theatreSeats = Integer.parseInt(request.getParameter("theatrecount"));
        int theatreArea = Integer.parseInt(request.getParameter("theatrearea"));
        int taxiCount = Integer.parseInt(request.getParameter("taxi"));
        boolean bookTaxi = (taxiCount >= 1 ? true : false);

        String result = "Transaction finished OK.";
        String txType = request.getParameter("txType");

        try
        {
            if ("AtomicTransaction".equals(txType))
            {
                testAtomicTransaction(restaurantSeats, theatreSeats, theatreArea, bookTaxi);
            }
            else if ("BusinessActivity".equals(txType))
            {
                testBusinessActivity(restaurantSeats, theatreSeats, theatreArea, bookTaxi);
            }
            else
            {
                result = "Unknown transaction type " + txType;
            }
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
     * Run a simple transaction involving three Web Services.
     * Use the Atomic Transaction type.
     * <p/>
     * Note: due to the implementation of the transaction infrastructure,
     * this method must be invoked with a web application container. It will
     * not run correctly in a standalone java application.
     *
     * @throws Exception for any unexpected errors, such as a failure to commit.
     */
    private void testAtomicTransaction(int restaurantSeats, int theatreSeats, int theatreArea, boolean bookTaxi) throws Exception
    {
        System.out.println("CLIENT: obtaining userTransaction...");

        UserTransaction ut = UserTransactionFactory.userTransaction();

        System.out.println("CLIENT: starting the transaction...");

        ut.begin();

        System.out.println("CLIENT: transaction ID= " + ut.toString());

        System.out.println("CLIENT: calling business Web Services...");

        restaurantAT.bookSeats(restaurantSeats);
        theatreAT.bookSeats(theatreSeats, theatreArea);
        if (bookTaxi)
        {
            taxiAT.bookTaxi();
        }

        System.out.println("CLIENT: calling commit on the transaction...");

        ut.commit();

        System.out.println("done.");
        System.out.flush();
    }

    /**
     * Run a simple transaction involving three Web Services.
     * Use the Business Activity type.
     * <p/>
     * Note: due to the implementation of the transaction infrastructure,
     * this method must be invoked with a web application container. It will
     * not run correctly in a standalone java application.
     *
     * @throws Exception for any unexpected errors, such as a failure to commit.
     */
    private void testBusinessActivity(int restaurantSeats, int theatreSeats, int theatreArea, boolean bookTaxi) throws Exception
    {
        System.out.println("CLIENT: obtaining userBusinessActivity...");

        UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();

        System.out.println("CLIENT: starting the transaction...");

        uba.begin();

        System.out.println("CLIENT: transaction ID= " + uba.toString());

        System.out.println("CLIENT: calling business Web Services...");

        boolean isOK = false ;
        try
        {
            if (restaurantBA.bookSeats(restaurantSeats) && theatreBA.bookSeats(theatreSeats, theatreArea))
            {
                isOK = !bookTaxi || taxiBA.bookTaxi() ;
            }
        }
        catch (final Throwable th)
        {
            System.out.println("CLIENT: caught exception processing bookings, cancelling (" + th.getMessage() + ")") ;
        }

        if (isOK)
        {
            System.out.println("CLIENT: all OK");
            System.out.println("CLIENT: calling close on the transaction...");
            uba.close();
        }
        else
        {
            System.out.println("CLIENT: one or more services failed, calling cancel.");
            uba.cancel();
        }

        System.out.println("CLIENT: done.");
        System.out.flush();
    }

    /**
     * @param config The servlet config
     * @param property The property name
     * @param defautlValue The default value.
     * @return The initialisation property value or the default value if not present. 
     */
    private String getURL(final ServletConfig config, final String property, final String defaultValue)
    {
        final String value = config.getInitParameter(property) ;
        return (value == null ? defaultValue : value) ;
    }
}
