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
import org.jboss.jbossts.star.client.SRAParticipant;
import io.narayana.sra.demo.model.Booking;
import io.narayana.sra.demo.service.FlightService;
import org.jboss.jbossts.star.client.SRAStatus;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import static org.jboss.jbossts.star.client.SRAClient.RTS_HTTP_CONTEXT_HEADER;

@RequestScoped
@Path(FlightController.FLIGHT_PATH)
@SRA(SRA.Type.SUPPORTS)
public class FlightController extends SRAParticipant {
    public static final String FLIGHT_PATH = "/flight";
    public static final String FLIGHT_NUMBER_PARAM = "flightNumber";
    public static final String ALT_FLIGHT_NUMBER_PARAM = "altFlightNumber";
    public static final String FLIGHT_SEATS_PARAM = "flightSeats";

    @Inject
    FlightService flightService;

    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    @SRA(SRA.Type.REQUIRED)
    public Booking bookFlight(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId,
                              @QueryParam(FLIGHT_NUMBER_PARAM) @DefaultValue("") String flightNumber,
                              @QueryParam(FLIGHT_SEATS_PARAM) @DefaultValue("1") Integer seats,
                              @QueryParam("mstimeout") @DefaultValue("500") Long timeout) {

        return flightService.book(sraId, flightNumber, seats);
    }

    @GET
    @Path("/info/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @SRA(SRA.Type.SUPPORTS)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return flightService.get(bookingId);
    }

    @Override
    protected SRAStatus updateParticipantState(SRAStatus status, String bookingId) {
        System.out.printf("SRA: %s: Updating flight participant state to: %s", bookingId, status);
        switch (status) {
            case TransactionCommitted:
                flightService.updateBookingStatus(bookingId, Booking.BookingStatus.CONFIRMED);
                return status;
            case TransactionRolledBack:
                flightService.updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
                return status;
            default:
                return status;
        }
    }
}
