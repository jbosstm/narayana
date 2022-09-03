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
import io.narayana.sra.demo.service.HotelService;
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
@Path(HotelController.HOTEL_PATH)
@SRA(SRA.Type.SUPPORTS)
public class HotelController extends SRAParticipant {
    public static final String HOTEL_PATH = "/hotel";
    public static final String HOTEL_NAME_PARAM = "hotelName";
    public static final String HOTEL_BEDS_PARAM = "beds";

    @Inject
    HotelService hotelService;

    @POST
    @Path("/book")
    @Produces(MediaType.APPLICATION_JSON)
    @SRA(SRA.Type.REQUIRED)
    public Booking bookRoom(@HeaderParam(RTS_HTTP_CONTEXT_HEADER) String sraId,
                            @QueryParam(HOTEL_NAME_PARAM) @DefaultValue("Default") String hotelName,
                            @QueryParam(HOTEL_BEDS_PARAM) @DefaultValue("1") Integer beds,
                            @QueryParam("mstimeout") @DefaultValue("500") Long timeout) {
        return hotelService.book(sraId, hotelName, beds);
    }

    @GET
    @Path("/info/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    @SRA(SRA.Type.SUPPORTS)
    public Booking getBooking(@PathParam("bookingId") String bookingId) {
        return hotelService.get(bookingId);
    }

    @Override
    protected SRAStatus updateParticipantState(SRAStatus status, String bookingId) {
        System.out.printf("SRA: %s: Updating hotel participant state to: %s", bookingId, status);
        switch (status) {
            case TransactionCommitted:
                hotelService.updateBookingStatus(bookingId, Booking.BookingStatus.CONFIRMED);
                return status;
            case TransactionRolledBack:
                hotelService.updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);
                return status;
            default:
                return status;
        }
    }
}
