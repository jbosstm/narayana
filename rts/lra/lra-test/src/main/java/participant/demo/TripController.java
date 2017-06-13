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
package participant.demo;

import org.jboss.narayana.rts.lra.annotation.CompensatorStatus;
import org.jboss.narayana.rts.lra.annotation.LRA;
import org.jboss.narayana.rts.lra.annotation.Status;
import participant.model.Booking;
import participant.model.BookingStatus;
import participant.service.service.TripService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.jboss.narayana.rts.lra.client.LRAClient.LRA_HTTP_HEADER;

@RequestScoped
@Path(TripController.TRIP_PATH)
@LRA(LRA.Type.SUPPORTS)
public class TripController extends Compensator {
    public static final String TRIP_PATH = "/trip";

    private Client hotelClient;
    private Client flightClient;

    private WebTarget hotelTarget;
    private WebTarget flightTarget;

    @Inject
    private TripService tripService;

    @PostConstruct
    private void initController() {
        try {
            int servicePort = Integer.getInteger("service.http.port", 8081);
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
    private void finiController() {
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
    // delayClose because we want the LRA to be associated with a booking until the user confirms the booking
    @LRA(value = LRA.Type.REQUIRED, delayClose = true)
    public Response bookTrip( @HeaderParam(LRA_HTTP_HEADER) String lraId,
                              @QueryParam(HotelController.HOTEL_NAME_PARAM) @DefaultValue("") String hotelName,
                              @QueryParam(HotelController.HOTEL_BEDS_PARAM) @DefaultValue("1") Integer hotelGuests,
                              @QueryParam(FlightController.FLIGHT_NUMBER_PARAM) @DefaultValue("") String flightNumber,
                              @QueryParam(FlightController.ALT_FLIGHT_NUMBER_PARAM) @DefaultValue("") String altFlightNumber,
                              @QueryParam(FlightController.FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer flightSeats,
                              @QueryParam("mstimeout") @DefaultValue("500") Long timeout) throws BookingException {

        Booking hotelBooking = bookHotel(hotelName, hotelGuests);
        Booking flightBooking1 = bookFlight(flightNumber, flightSeats);
        Booking flightBooking2 = bookFlight(altFlightNumber, flightSeats);

        Booking tripBooking = new Booking(lraId, "Trip", hotelBooking, flightBooking1, flightBooking2);

        return Response.status(Response.Status.CREATED).entity(tripBooking).build();
    }

    @PUT
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.SUPPORTS) // the confirmation could be part of an enclosing LRA
    public Booking confirmTrip(Booking booking) throws BookingException {
        tripService.confirmBooking(booking);

        if (!TripCheck.validateBooking(booking, hotelTarget, flightTarget))
            throw new BookingException(INTERNAL_SERVER_ERROR.getStatusCode(), "LRA response data does not match booking data");

        booking.setStatus(BookingStatus.CONFIRMED);

        return booking;
    }

    @PUT
    @Path("/compensate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.SUPPORTS) // the confirmation could be part of an enclosing LRA
    public Booking cancelTrip(Booking booking) throws BookingException {
        tripService.cancelBooking(booking);

        if (!TripCheck.validateBooking(booking, hotelTarget, flightTarget))
            throw new BookingException(INTERNAL_SERVER_ERROR.getStatusCode(), "LRA response data does not match booking data");

        booking.setStatus(BookingStatus.CANCELLED);

        return booking;
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Status
    @LRA(LRA.Type.NOT_SUPPORTED)
    public Response status(@HeaderParam(LRA_HTTP_HEADER) String lraId) throws NotFoundException {
        Booking booking = tripService.get(lraId);

        return Response.ok(booking.getStatus().name()).build(); // TODO convert to a CompensatorStatus if we we're enlisted in an LRA
    }

    private Booking bookHotel(String name, int beds) throws BookingException {
        if (name == null || name.length() == 0 || beds <= 0)
            return null;

        WebTarget webTarget = hotelTarget
                .path("book")
                .queryParam(HotelController.HOTEL_NAME_PARAM, name).queryParam(HotelController.HOTEL_BEDS_PARAM, beds);

        Response response = webTarget.request().post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "flight booking problem");

        return response.readEntity(Booking.class);
    }

    private Booking bookFlight(String flightNumber, int seats) throws BookingException {
        if (flightNumber == null || flightNumber.length() == 0 || seats <= 0)
            return null;

        WebTarget webTarget = flightTarget
                .path("book")
                .queryParam(FlightController.FLIGHT_NUMBER_PARAM, flightNumber)
                .queryParam(FlightController.FLIGHT_SEATS_PARAM, seats);

        Response response = webTarget.request().post(Entity.text(""));

        if (response.getStatus() != Response.Status.OK.getStatusCode())
            throw new BookingException(response.getStatus(), "flight booking problem");

        return response.readEntity(Booking.class);
    }

    @GET
    @Path("/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.SUPPORTS)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return tripService.get(bookingId);
    }

    @Override
    protected CompensatorStatus updateCompensator(CompensatorStatus status, String bookingId) {
        switch (status) {
            case Completed:
                tripService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
                return status;
            case Compensated:
                tripService.updateBookingStatus(bookingId, BookingStatus.CANCELLED);
                return status;
            default:
                return status;
        }
    }
}

