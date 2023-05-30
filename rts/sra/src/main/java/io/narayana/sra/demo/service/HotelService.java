/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.sra.demo.service;

import io.narayana.sra.demo.model.Booking;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class HotelService extends BookingStore {
    public Booking book(String bid, String hotel, Integer beds) {
        Booking booking = new Booking(bid, hotel, beds, "Hotel");

        add(booking);

        return booking;
    }

    public CompletableFuture<Booking> bookAsync(String bid, String hotel, Integer beds) {
        return CompletableFuture.supplyAsync(() -> book(bid, hotel, beds));
    }
}