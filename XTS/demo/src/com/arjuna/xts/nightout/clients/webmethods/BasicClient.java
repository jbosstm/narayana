/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.xts.nightout.clients.webmethods ;


import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mw.wst.UserBusinessActivityFactory;
import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.mw.wst.UserTransactionFactory;
import com.arjuna.xts.nightout.services.Restaurant.IRestaurantService;
import com.arjuna.xts.nightout.services.Restaurant.IRestaurantServiceBA;
import com.arjuna.xts.nightout.services.Taxi.ITaxiService;
import com.arjuna.xts.nightout.services.Taxi.ITaxiServiceBA;
import com.arjuna.xts.nightout.services.Theatre.ITheatreService;
import com.arjuna.xts.nightout.services.Theatre.ITheatreServiceBA;

import electric.registry.Registry;


/**
 * A very basic client application that drives the
 * (transactional) Web Services to arrange a night out.
 * <p/>
 * This is a reworking of the axis client.
 *
 * @author Malik SAHEB (malik.saheb@arjuna.com)
 * @version $Id$
 */
public class BasicClient extends HttpServlet
{
    /**
     * The client serial version UID.
     */
    private static final long serialVersionUID = -5260691945398399633L ;

    /**
     * The servlet context.
     */
    private ServletContext context ;

    /**
     * The atomic transaction restaurant stub.
     */
    private IRestaurantService restaurantAT;
    /**
     * The atomic transaction theatre stub.
     */
    private ITheatreService theatreAT;
    /**
     * The atomic transaction taxi stub.
     */
    private ITaxiService taxiAT;
    /**
     * The business activity restaurant stub.
     */
    private IRestaurantServiceBA restaurantBA;
    /**
     * The business activity theatre stub.
     */
    private ITheatreServiceBA theatreBA;
    /**
     * The business activity taxi stub.
     */
    private ITaxiServiceBA taxiBA;

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
        final String baseURL = "http://localhost:8004/xts-demo/services/" ;

        restaurantATURL = getURL(config, "restaurantATURL", baseURL + "RestaurantServiceAT.wsdl") ;
        restaurantBAURL = getURL(config, "restaurantBAURL", baseURL + "RestaurantServiceBA.wsdl") ;
        taxiATURL = getURL(config, "taxiATURL", baseURL + "TheatreServiceAT.wsdl") ;
        taxiBAURL = getURL(config, "taxiBAURL", baseURL + "TheatreServiceBA.wsdl") ;
        theatreATURL = getURL(config, "theatreATURL", baseURL + "TheatreServiceAT.wsdl") ;
        theatreBAURL = getURL(config, "theatreBAURL", baseURL + "TheatreServiceBA.wsdl") ;

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
	    try 
	    {
                restaurantAT = (IRestaurantService)Registry.bind(restaurantATURL, IRestaurantService.class) ;
                restaurantBA = (IRestaurantServiceBA)Registry.bind(restaurantBAURL,IRestaurantServiceBA.class) ;
                
                taxiAT = (ITaxiService)Registry.bind(taxiATURL, ITaxiService.class) ;
                taxiBA = (ITaxiServiceBA)Registry.bind(taxiBAURL, ITaxiServiceBA.class) ;
                
                theatreAT = (ITheatreService)Registry.bind(theatreATURL, ITheatreService.class) ;
                theatreBA = (ITheatreServiceBA)Registry.bind(theatreBAURL, ITheatreServiceBA.class) ;
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
