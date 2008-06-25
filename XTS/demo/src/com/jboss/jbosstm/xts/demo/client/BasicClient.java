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
/*
 * BasicClient.java
 *
 * Copyright (c) 2003, 2004 Arjuna Technologies Ltd.
 *
 * $Id: BasicClient.java,v 1.10 2004/12/02 16:52:58 kconner Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.client;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.mw.wst11.client.JaxWSHeaderContextProcessor;
import com.arjuna.wst.TransactionRolledBackException;
import com.jboss.jbosstm.xts.demo.restaurant.IRestaurantServiceAT;
import com.jboss.jbosstm.xts.demo.restaurant.IRestaurantServiceBA;
import com.jboss.jbosstm.xts.demo.taxi.ITaxiServiceAT;
import com.jboss.jbosstm.xts.demo.taxi.ITaxiServiceBA;
import com.jboss.jbosstm.xts.demo.theatre.ITheatreServiceAT;
import com.jboss.jbosstm.xts.demo.theatre.ITheatreServiceBA;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * A very basic client application that drives the
 * (transactional) Web Services to arrange a night out.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Id: BasicClient.java,v 1.10 2004/12/02 16:52:58 kconner Exp $
 */
public class BasicClient extends HttpServlet
{
    /**
     * The client serial version UID.
     */
    private static final long serialVersionUID = 7728495576623420083L ;

    /**
     * The servlet context.
     */
    private ServletContext context ;

    /***** RESTAURANT SERVICE *****/

    /**
     * The namespace for the Restaurant webservice.
     */
    private static final String RESTAURANT_NS = "http://www.jboss.com/jbosstm/xts/demo/Restaurant" ;
    /**
     * The prefix for the Restaurant webservice.
     */
    private static final String RESTAURANT_PREFIX = "restaurant";
    /**
     * The local name for the AT Restaurant webservice.
     */
    private static final String RESTAURANT_SERVICE_AT = "RestaurantServiceATService";
    /**
     * The QName for the AT Restaurant webservice.
     */
    private static final QName RESTAURANT_SERVICE_AT_QNAME
            = new QName(RESTAURANT_NS, RESTAURANT_SERVICE_AT, RESTAURANT_PREFIX);
    /**
     * The local name for the BA Restaurant webservice.
     */
    private static final String RESTAURANT_SERVICE_BA = "RestaurantServiceBAService" ;
    /**
     * The QName for the AT Restaurant webservice.
     */
    private static final QName RESTAURANT_SERVICE_BA_QNAME
            = new QName(RESTAURANT_NS, RESTAURANT_SERVICE_BA, RESTAURANT_PREFIX);
    /**
     * The local name for the AT Restaurant _.
     */
    private static final String RESTAURANT_ENDPOINT_AT = "RestaurantServiceAT";
    /**
     * The QName for the AT Restaurant endpoint.
     */
    private static final QName RESTAURANT_ENDPOINT_AT_QNAME
            = new QName(RESTAURANT_NS, RESTAURANT_ENDPOINT_AT, RESTAURANT_PREFIX);
    /**
     * The local name for the BA Restaurant endpoint.
     */
    private static final String RESTAURANT_ENDPOINT_BA = "RestaurantServiceBA";
    /**
     * The QName for the BA Restaurant endpoint.
     */
    private static final QName RESTAURANT_ENDPOINT_BA_QNAME
            = new QName(RESTAURANT_NS, RESTAURANT_ENDPOINT_BA, RESTAURANT_PREFIX);

    /***** THEATRE SERVICE *****/

    /**
     * The namespace for the Theatre webservice.
     */
    private static final String THEATRE_NS = "http://www.jboss.com/jbosstm/xts/demo/Theatre" ;
    /**
     * The prefix for the Theatre webservice.
     */
    private static final String THEATRE_PREFIX = "theatre";
    /**
     * The local name for the AT Theatre webservice.
     */
    private static final String THEATRE_SERVICE_AT = "TheatreServiceATService";
    /**
     * The QName for the AT Theatre webservice.
     */
    private static final QName THEATRE_SERVICE_AT_QNAME
            = new QName(THEATRE_NS, THEATRE_SERVICE_AT, THEATRE_PREFIX);
    /**
     * The local name for the BA Theatre webservice.
     */
    private static final String THEATRE_SERVICE_BA = "TheatreServiceBAService" ;
    /**
     * The QName for the AT Theatre webservice.
     */
    private static final QName THEATRE_SERVICE_BA_QNAME
            = new QName(THEATRE_NS, THEATRE_SERVICE_BA, THEATRE_PREFIX);
    /**
     * The local name for the AT Theatre endpoint.
     */
    private static final String THEATRE_ENDPOINT_AT = "TheatreServiceAT";
    /**
     * The QName for the AT Theatre endpoint.
     */
    private static final QName THEATRE_ENDPOINT_AT_QNAME
            = new QName(THEATRE_NS, THEATRE_ENDPOINT_AT, THEATRE_PREFIX);
    /**
     * The local name for the BA Theatre endpoint.
     */
    private static final String THEATRE_ENDPOINT_BA = "TheatreServiceBA";
    /**
     * The QName for the BA Theatre endpoint.
     */
    private static final QName THEATRE_ENDPOINT_BA_QNAME
            = new QName(THEATRE_NS, THEATRE_ENDPOINT_BA, THEATRE_PREFIX);

    /***** TAXI SERVICE *****/

    /**
     * The namespace for the Taxi webservice.
     */
    private static final String TAXI_NS = "http://www.jboss.com/jbosstm/xts/demo/Taxi" ;
    /**
     * The prefix for the Taxi webservice.
     */
    private static final String TAXI_PREFIX = "taxi";
    /**
     * The local name for the AT Taxi webservice.
     */
    private static final String TAXI_SERVICE_AT = "TaxiServiceATService";
    /**
     * The QName for the AT Taxi webservice.
     */
    private static final QName TAXI_SERVICE_AT_QNAME
            = new QName(TAXI_NS, TAXI_SERVICE_AT, TAXI_PREFIX);
    /**
     * The local name for the BA Taxi webservice.
     */
    private static final String TAXI_SERVICE_BA = "TaxiServiceBAService" ;
    /**
     * The QName for the AT Taxi webservice.
     */
    private static final QName TAXI_SERVICE_BA_QNAME
            = new QName(TAXI_NS, TAXI_SERVICE_BA, TAXI_PREFIX);
    /**
     * The local name for the AT Taxi endpoint.
     */
    private static final String TAXI_ENDPOINT_AT = "TaxiServiceAT";
    /**
     * The QName for the AT Taxi endpoint.
     */
    private static final QName TAXI_ENDPOINT_AT_QNAME
            = new QName(TAXI_NS, TAXI_ENDPOINT_AT, TAXI_PREFIX);
    /**
     * The local name for the BA Taxi endpoint.
     */
    private static final String TAXI_ENDPOINT_BA = "TaxiServiceBA";
    /**
     * The QName for the BA Taxi endpoint.
     */
    private static final QName TAXI_ENDPOINT_BA_QNAME
            = new QName(TAXI_NS, TAXI_ENDPOINT_BA, TAXI_PREFIX);

    /***** Client Handles for Service Endpoint Ports *****/

    /**
     * The atomic transaction restaurant stub.
     */
    private IRestaurantServiceAT restaurantAT;
    /**
     * The atomic transaction theatre stub.
     */
    private ITheatreServiceAT theatreAT;
    /**
     * The atomic transaction taxi stub.
     */
    private ITaxiServiceAT taxiAT;
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

    /***** Endpoint Addresses *****/

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

    /***** WSDL file locations *****/

    /**
     * URL of restaurant AT
     */
    private final String restaurantATWSDL = "wsdl/RestaurantServiceAT.wsdl";
    /**
     * URL of restaurant BA
     */
    private final String restaurantBAWSDL = "wsdl/RestaurantServiceBA.wsdl";
    /**
     * URL of taxi AT                             */
    private final String taxiATWSDL = "wsdl/TaxiServiceAT.wsdl";
    /**
     * URL of taxi BA
     */
    private final String taxiBAWSDL = "wsdl/TaxiServiceBA.wsdl";
    /**
     * URL of theatre AT
     */
    private final String theatreATWSDL = "wsdl/TheatreServiceAT.wsdl";
    /**
     * URL of theatre BA
     */
    private final String theatreBAWSDL = "wsdl/TheatreServiceBA.wsdl";

    /**
     * The initialised flag.
     */
    private boolean initialised ;
    
    /**
     * Initialise the servlet.
     * @param config The servlet configuration.
     */
    public void init(final ServletConfig config)
        throws ServletException
    {
        final String baseURL = "http://localhost:8080/xtsdemowebservices/" ;

        restaurantATURL = getURL(config, "restaurantATURL", baseURL + RESTAURANT_SERVICE_AT);
        restaurantBAURL = getURL(config, "restaurantBAURL", baseURL + RESTAURANT_SERVICE_BA);
        taxiATURL = getURL(config, "taxiATURL", baseURL + TAXI_SERVICE_AT);
        taxiBAURL = getURL(config, "taxiBAURL", baseURL + TAXI_SERVICE_BA);
        theatreATURL = getURL(config, "theatreATURL", baseURL + THEATRE_SERVICE_AT);
        theatreBAURL = getURL(config, "theatreBAURL", baseURL + THEATRE_SERVICE_BA);

        context = config.getServletContext();
    }

    /**
     * configure the XTS client handler which manages transaction flow for invocations of the services
     *
     * @param bindingProvider
     */
    private void configureClientHandler(BindingProvider bindingProvider)
    {
        Handler handler = new JaxWSHeaderContextProcessor();
        List<Handler> handlers = Collections.singletonList(handler);
        bindingProvider.getBinding().setHandlerChain(handlers);
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
                restaurantAT = getService(RESTAURANT_SERVICE_AT_QNAME, RESTAURANT_ENDPOINT_AT_QNAME,
                        restaurantATURL, restaurantATWSDL, IRestaurantServiceAT.class);
                configureClientHandler((BindingProvider)restaurantAT);

                restaurantBA = getService(RESTAURANT_SERVICE_BA_QNAME, RESTAURANT_ENDPOINT_BA_QNAME,
                        restaurantBAURL, restaurantBAWSDL, IRestaurantServiceBA.class);
                configureClientHandler((BindingProvider)restaurantBA);

                theatreAT = getService(THEATRE_SERVICE_AT_QNAME, THEATRE_ENDPOINT_AT_QNAME,
                        theatreATURL, theatreATWSDL, ITheatreServiceAT.class);
                configureClientHandler((BindingProvider)theatreAT);

                theatreBA = getService(THEATRE_SERVICE_BA_QNAME, THEATRE_ENDPOINT_BA_QNAME,
                        theatreBAURL, theatreBAWSDL, ITheatreServiceBA.class);
                configureClientHandler((BindingProvider)theatreBA);

                taxiAT = getService(TAXI_SERVICE_AT_QNAME, TAXI_ENDPOINT_AT_QNAME,
                        taxiATURL, taxiATWSDL, ITaxiServiceAT.class);
                configureClientHandler((BindingProvider)taxiAT);

                taxiBA = getService(TAXI_SERVICE_BA_QNAME, TAXI_ENDPOINT_BA_QNAME,
                        taxiBAURL, taxiBAWSDL, ITaxiServiceBA.class);
                configureClientHandler((BindingProvider)taxiBA);
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
                if (!testBusinessActivity(restaurantSeats, theatreSeats, theatreArea, bookTaxi))
                {
                    result = "Transaction cancelled/compensated.";
                }
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
    private boolean testBusinessActivity(int restaurantSeats, int theatreSeats, int theatreArea, boolean bookTaxi) throws Exception
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

        return isOK;
    }

    /**
     * @param config The servlet config
     * @param property The property name
     * @param defaultValue The default value.
     * @return The initialisation property value or the default value if not present. 
     */
    private String getURL(final ServletConfig config, final String property, final String defaultValue)
    {
        final String value = config.getInitParameter(property) ;
        return (value == null ? defaultValue : value) ;
    }
    
    /**
     * Get an endpoint reference for a service so we can create a JaxWS port for it
     * @param serviceName the QName of the service in question..
     * @param endpointName the QName of the endpoint associated with the service
     * @param address a string representation of the service URL. null is ok if this is a service located in the
     * same app as the client
     * @return a W3CEndpointReference from which the service port can be obtained.
     */
    private <T> T getService(final QName serviceName, final QName endpointName,
                                            final String address, final String wsdlURL, final Class<T> clazz)
            throws MalformedURLException
    {
        URL url = BasicClient.class.getResource("../../../../../../../" + wsdlURL);
        Service service = Service.create(url, serviceName);
        T port = service.getPort(endpointName, clazz);
        BindingProvider bindingProvider = ((BindingProvider) port);
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
        return port;
    }
}
