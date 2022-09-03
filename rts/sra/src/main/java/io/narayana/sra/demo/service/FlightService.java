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
