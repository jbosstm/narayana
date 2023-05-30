/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.sra.demo.service;

import io.narayana.sra.demo.model.Booking;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class FlightService extends BookingStore {
    public Booking book(String bid, String flightNumber, Integer seats) {
        Booking booking = new Booking(bid, flightNumber, seats, "Flight");

        add(booking);

        return booking;
    }

    public CompletableFuture<Booking> bookAsync(String bid, String flightNumber, Integer seats) {
        return CompletableFuture.supplyAsync(() -> book(bid, flightNumber, seats));}
}