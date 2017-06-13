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

import com.fasterxml.jackson.databind.ObjectMapper;
import participant.demo.FlightController;
import participant.demo.HotelController;
import participant.demo.TripController;
import participant.model.Booking;

import java.io.DataOutputStream;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class TripClient {
    private static String PRIMARY_SERVER;
    private static String TRIP_SERVICE_BASE_URL;
    private static String HOTEL_SERVICE_BASE_URL;
    private static String FLIGHT_SERVICE_BASE_URL;

    ObjectMapper objectMapper = new ObjectMapper();

    private static void initClient() {
        int servicePort = Integer.getInteger("service.http.port", 8081);

        PRIMARY_SERVER = "http://localhost:" + servicePort;
        TRIP_SERVICE_BASE_URL = String.format("%s%s", PRIMARY_SERVER, TripController.TRIP_PATH);
        HOTEL_SERVICE_BASE_URL = String.format("%s%s", PRIMARY_SERVER, HotelController.HOTEL_PATH);
        FLIGHT_SERVICE_BASE_URL = String.format("%s%s", PRIMARY_SERVER, FlightController.FLIGHT_PATH);
    }

    public static void main(String[] args) throws Exception {
        initClient();
        TripClient tripClient = new TripClient();

        Booking booking = tripClient.bookTrip("TheGrand", 2, "BA123", "RH456", 2);

        if (booking == null)
            return;

        // requestCancel the first flight found (and use the second one)
        Optional<Booking> firstFlight = Arrays.stream(booking.getDetails()).filter(b -> "Flight".equals(b.getType())).findFirst();

        firstFlight.ifPresent(Booking::requestCancel);

        System.out.printf("%nBooking Info:%n\t%s%n", booking);
        System.out.printf("Associated Bookings:%n");

        Arrays.stream(booking.getDetails()).forEach(b -> System.out.printf("\t%s%n", b));

        Booking confirmation = (args.length > 0 && args[0].equals("cancel"))
                    ? tripClient.cancel(booking)
                    : tripClient.confirm(booking);

        System.out.printf("%nBooking confirmation:%n\t%s%n", confirmation);
        Arrays.stream(confirmation.getDetails()).forEach(b -> System.out.printf("\t%s%n", b));
    }

    private Booking bookTrip(String hotelName, Integer hotelGuests,
                            String flightNumber, String altFlightNumber, Integer flightSeats) throws Exception {
        StringBuilder tripRequest =
                new StringBuilder(TRIP_SERVICE_BASE_URL)
                        .append("/book?")
                        .append(HotelController.HOTEL_NAME_PARAM).append('=').append(hotelName).append('&')
                        .append(HotelController.HOTEL_BEDS_PARAM).append('=').append(hotelGuests).append('&')
                        .append(FlightController.FLIGHT_NUMBER_PARAM).append('=').append(flightNumber).append('&')
                        .append(FlightController.ALT_FLIGHT_NUMBER_PARAM).append('=').append(altFlightNumber).append('&')
                        .append(FlightController.FLIGHT_SEATS_PARAM).append('=').append(flightSeats);

        URL url = new URL(tripRequest.toString());
        String json = updateResource(url, "POST", "");

        if (json == null)
            return null;

        return objectMapper.readValue(json, Booking.class);
    }

    private Booking confirm(Booking booking) throws Exception {
        return completeBooking(booking, "complete");
    }

    private Booking cancel(Booking booking) throws Exception {
        return completeBooking(booking, "compensate");
    }

    private Booking completeBooking(Booking booking, String how) throws Exception {
        URL confirmURL = new URL(TRIP_SERVICE_BASE_URL +"/" + how);
        String jsonBody = objectMapper.writeValueAsString(booking);
        String confirmation = updateResource(confirmURL, "PUT", jsonBody);

        return objectMapper.readValue(confirmation, Booking.class);
    }

    private String updateResource(URL resource, String method, String jsonBody) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) resource.openConnection();

        try (AutoCloseable ac = connection::disconnect) {

            connection.setDoOutput(true);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");

            try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
                dos.writeBytes(jsonBody);
            }

            int responseCode = connection.getResponseCode();

            try (InputStream ins = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                Scanner responseScanner = new java.util.Scanner(ins).useDelimiter("\\A");
                String res = responseScanner.hasNext()? responseScanner.next() : null;

                if (res != null && responseCode >= 400) {
                    System.out.println(res);

                    return null;
                }

                return res;
            }
        }
    }
}
