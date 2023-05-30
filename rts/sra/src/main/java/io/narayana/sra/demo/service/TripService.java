/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.sra.demo.service;

import org.jboss.jbossts.star.client.SRAClient;
import io.narayana.sra.demo.model.Booking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class TripService extends BookingStore{
    private SRAClient lraClient;

    public TripService() throws MalformedURLException, URISyntaxException {
        this.lraClient = new SRAClient();
    }

    public Booking confirmBooking(Booking tripBooking) {
        System.out.printf("Confirming tripBooking id %s (%s) status: %s%n",
                tripBooking.getId(), tripBooking.getName(), tripBooking.getStatus());

        if (tripBooking.getStatus() == Booking.BookingStatus.CANCEL_REQUESTED)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Trying to setConfirmed a tripBooking which needs to be cancelled")
                    .build());

        Booking prev = add(tripBooking);

        if (prev != null)
            System.out.printf("Seen this tripBooking before%n");

        // check the booking to see if the client wants to requestCancel any dependent bookings
        Arrays.stream(tripBooking.getDetails()).filter(Booking::isCancelPending).forEach(b -> {
            lraClient.cancelSRA(SRAClient.sraToURL(b.getId(), "Invalid " + b.getType() + " tripBooking id format"));
            b.setCanceled();
        });

        tripBooking.setConfirming();

        lraClient.commitSRA(SRAClient.sraToURL(tripBooking.getId()));

        tripBooking.setConfirmed();

        return mergeBookingResponse(tripBooking);
    }

    public Booking cancelBooking(Booking booking) {
        System.out.printf("Canceling booking id %s (%s) status: %s%n",
                booking.getId(), booking.getName(), booking.getStatus());

        if (booking.getStatus() != Booking.BookingStatus.CANCEL_REQUESTED && booking.getStatus() != Booking.BookingStatus.PROVISIONAL)
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("To late to requestCancel booking").build());

        Booking prev = add(booking);

        if (prev != null)
            System.out.printf("Seen this booking before%n");

        booking.requestCancel();

        lraClient.cancelSRA(SRAClient.sraToURL(booking.getId(), "Invalid trip booking id format"));

        booking.setCanceled();

        return mergeBookingResponse(booking);
    }

    private Booking mergeBookingResponse(Booking tripBooking) {
        URL bookingId = SRAClient.sraToURL(tripBooking.getId());
        List<String> bookingDetails = lraClient.getResponseData(bookingId); // each string is a json encoded tripBooking

//        List<Booking> bookings = bookingDetails.stream().map(Booking::fromJson).collect(Collectors.toList());

        // convert the list of bookings into a map keyed by Booking::getId()
        Map<String, Booking> bookings = bookingDetails.stream()
                .map(Booking::fromJson)
                .collect(Collectors.toMap(Booking::getId, Function.identity()));

        // update tripBooking with bookings returned in the data returned from the trip setConfirmed request
        Arrays.stream(tripBooking.getDetails()) // the array of bookings in this trip booking
                .filter(b -> bookings.containsKey(b.getId())) // pick out bookings for which we have updated data
                .forEach(b -> b.merge(bookings.get(b.getId()))); // merge in the changes (returned from the setConfirmed request)

        return tripBooking;
    }
}