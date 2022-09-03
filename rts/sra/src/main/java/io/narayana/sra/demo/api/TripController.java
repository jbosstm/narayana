/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.sra.demo.api;

import org.jboss.jbossts.star.annotation.SRA;
import org.jboss.jbossts.star.annotation.Status;
import org.jboss.jbossts.star.client.SRAParticipant;
import io.narayana.sra.demo.model.Booking;
import io.narayana.sra.demo.service.BookingException;
import io.narayana.sra.demo.service.TripService;
import org.jboss.jbossts.star.client.SRAStatus;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

@RequestScoped
@Path(TripController.TRIP_PATH)
@SRA(SRA.Type.SUPPORTS)
public class TripController extends SRAParticipant {
    public static final String TRIP_PATH = "/trip";
    public static final String SERVICE_PORT_PROPERTY = "quarkus.http.port";

    private Client hotelClient;
    private Client flightClient;

    private WebTarget hotelTarget;
    private WebTarget flightTarget;

    @Inject
    TripService tripService;

    @PostConstruct
    void initController() {
        try {
            int servicePort = Integer.getInteger(SERVICE_PORT_PROPERTY, 8080);
            URL HOTEL_SERVICE_BASE_URL = new URL("http://localhost:" + servicePort);
            URL FLIGHT_SERVICE_BASE_URL = new URL("http://localhost:" + servicePort);

            hotelClient = ClientBuilder.newClient();
            flightClient = ClientBuilder.newClient();

            hotelTarget = hotelClient.target(URI.create(new URL(HOTEL_SERVICE_BASE_URL, HotelController.HOTEL_PATH).toExternalForm()));
            flightTarget = flightClient.target(URI.create(new URL(FLIGHT_SERVICE_BASE_URL, FlightController.FLIGHT_PATH).toExternalForm()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    void finiController() {
        hotelClient.close();
        flightClient.close();
    }

    /**
     * The quickstart scenario is:
     *
     * start LRA 1
     *   Book hotel
     *   start LRA 2
     *     start LRA 3
     *       Book flight option 1
     *     start LRA 4
     *       Book flight option 2
     *
     * @param hotelName hotel name
     * @param hotelGuests number of beds required
     * @param flightSeats number of people flying
     */
    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    // end = false because we want the SRA to be associated with a booking until the user confirms the booking
    @SRA(value = SRA.Type.REQUIRED, end = false)
    public Response bookTrip( @HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId,
                              @QueryParam(HotelController.HOTEL_NAME_PARAM) @DefaultValue("") String hotelName,
                              @QueryParam(HotelController.HOTEL_BEDS_PARAM) @DefaultValue("1") Integer hotelGuests,
                              @QueryParam(FlightController.FLIGHT_NUMBER_PARAM) @DefaultValue("") String flightNumber,
                              @QueryParam(FlightController.ALT_FLIGHT_NUMBER_PARAM) @DefaultValue("") String altFlightNumber,
                              @QueryParam(FlightController.FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer flightSeats,
                              @QueryParam("mstimeout") @DefaultValue("500") Long timeout) throws BookingException {

        Booking hotelBooking = bookHotel(sraId, hotelName, hotelGuests);
        Booking flightBooking1 = bookFlight(sraId, flightNumber, flightSeats);
        Booking flightBooking2 = bookFlight(sraId, altFlightNumber, flightSeats);

        Booking tripBooking = new Booking(sraId, "Trip", hotelBooking, flightBooking1, flightBooking2);

        return Response.status(Response.Status.CREATED).entity(tripBooking).build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @SRA(SRA.Type.NOT_SUPPORTED)
    public Response status(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId) throws NotFoundException {
        Booking booking = tripService.get(sraId);

        return Response.ok(booking.getStatus().name()).build(); // TODO convert to a CompensatorStatus if we we're enlisted in an SRA
    }

    private Booking bookHotel(String sraId, String name, int beds) throws BookingException {
        if (name == null || name.length() == 0 || beds <= 0)
            return null;

        WebTarget webTarget = hotelTarget
                .path("book")
                .queryParam(HotelController.HOTEL_NAME_PARAM, name)
                .queryParam(HotelController.HOTEL_BEDS_PARAM, beds);

        Response response = webTarget.request().header(RTS_HTTP_CONTEXT_HEADER, sraId).post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "hotel booking problem: " + response.getStatus());

        return response.readEntity(Booking.class);
    }

    private Booking bookFlight(String sraId, String flightNumber, int seats) throws BookingException {
        if (flightNumber == null || flightNumber.length() == 0 || seats <= 0)
            return null;

        WebTarget webTarget = flightTarget
                .path("book")
                .queryParam(FlightController.FLIGHT_NUMBER_PARAM, flightNumber)
                .queryParam(FlightController.FLIGHT_SEATS_PARAM, seats);

        Response response = webTarget.request().header(RTS_HTTP_CONTEXT_HEADER, sraId).post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "flight booking problem: " + response.getStatus());

        return response.readEntity(Booking.class);
    }

    @Override
    protected SRAStatus updateParticipantState(SRAStatus status, String activityId) {
        System.out.printf("SRA: %s: Updating trip participant state to: %s", activityId, status);
        return status;
    }
}

