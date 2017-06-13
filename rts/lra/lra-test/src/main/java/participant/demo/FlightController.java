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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.narayana.rts.lra.annotation.CompensatorStatus;
import org.jboss.narayana.rts.lra.annotation.LRA;
import org.jboss.narayana.rts.lra.annotation.NestedLRA;
import participant.model.Booking;
import participant.model.BookingStatus;
import participant.service.service.FlightService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static org.jboss.narayana.rts.lra.client.LRAClient.LRA_HTTP_HEADER;

//@ApplicationScoped
@RequestScoped
@Path(FlightController.FLIGHT_PATH)
@LRA(LRA.Type.SUPPORTS)
public class FlightController extends Compensator {
    public static final String FLIGHT_PATH = "/flight";
    public static final String FLIGHT_NUMBER_PARAM = "flightNumber";
    public static final String ALT_FLIGHT_NUMBER_PARAM = "altFlightNumber";
    public static final String FLIGHT_SEATS_PARAM = "flightSeats";

    @Inject
    private FlightService flightService;

    @POST
    @Path("/bookasync")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.REQUIRED)
    @NestedLRA
    public void bookFlightAsync(@Suspended final AsyncResponse asyncResponse,
                                @HeaderParam(LRA_HTTP_HEADER) String lraId,
                                @QueryParam(FLIGHT_NUMBER_PARAM) @DefaultValue("") String flightNumber,
                                @QueryParam(FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer seats,
                                @QueryParam("mstimeout") @DefaultValue("500") Long timeout) {

        flightService.bookAsync(lraId, flightNumber, seats)
                .thenApply(asyncResponse::resume)
                .exceptionally(e -> asyncResponse.resume(Response.status(INTERNAL_SERVER_ERROR).entity(e).build()));

        asyncResponse.setTimeout(timeout, TimeUnit.MILLISECONDS);
        asyncResponse.setTimeoutHandler(ar -> ar.resume(Response.status(SERVICE_UNAVAILABLE).entity("Operation timed out").build()));
    }

    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.REQUIRED)
    @NestedLRA
    public Booking bookFlight(@HeaderParam(LRA_HTTP_HEADER) String lraId,
                              @QueryParam(FLIGHT_NUMBER_PARAM) @DefaultValue("") String flightNumber,
                              @QueryParam(FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer seats,
                              @QueryParam("mstimeout") @DefaultValue("500") Long timeout) {

        return flightService.book(lraId, flightNumber, seats);
    }

    @GET
    @Path("/info/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @LRA(LRA.Type.SUPPORTS)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return flightService.get(bookingId);
    }

    @Override
    protected CompensatorStatus updateCompensator(CompensatorStatus status, String bookingId) {
        switch (status) {
            case Completed:
                flightService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED);
                return status;
            case Compensated:
                flightService.updateBookingStatus(bookingId, BookingStatus.CANCELLED);
                return status;
            default:
                return status;
        }
    }

    @Override
    protected String getCompensatorData(String bookingId) {
        try {
            return flightService.get(bookingId).toJson();
        } catch (NotFoundException | JsonProcessingException e) {
            System.out.printf("No booking for flight id %s%n", bookingId);
            return null;
        }
    }
}
