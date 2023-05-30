/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
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