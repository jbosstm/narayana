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
package io.narayana.sra.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Booking {
    @JsonProperty("id") private String id;
    @JsonProperty("name") private String name;
    @JsonProperty("quantity") private Integer quantity;
    @JsonProperty("status") private BookingStatus status;
    @JsonProperty("type") private String type;
    @JsonProperty("details") private Booking[] details;

    private IOException decodingException;

    public Booking() {
        this("", "", 0, "");
    }

    public Booking(String id, String name, Integer quantity, String type) {
        this(id, name, quantity, type, BookingStatus.PROVISIONAL, null);
    }

    public Booking(String id, String type, Booking... bookings) {
        this(id, "Aggregate Booking", 1, type, BookingStatus.PROVISIONAL, bookings);
    }

    @JsonCreator
    public Booking(@JsonProperty("id") String id,
                   @JsonProperty("name") String name,
                   @JsonProperty("quantity") Integer quantity,
                   @JsonProperty("type") String type,
                   @JsonProperty("status") BookingStatus status,
                   @JsonProperty("details") Booking[] details) {

        init(id, name, quantity, type, status, details);
    }

    public Booking(IOException decodingException) {
        this.decodingException = decodingException;
    }

    public Booking(Booking booking) {
        this.init(booking.getId(), booking.getName(), booking.getQuantity(), booking.getType(), booking.getStatus(), null);

        details = new Booking[booking.getDetails().length];

        IntStream.range(0, details.length).forEach(i -> details[i] = new Booking(booking.getDetails()[i]));
    }

    private void init(String id, String name, Integer quantity, String type, BookingStatus status, Booking[] details) {
        String[] segments = id.split("/");

        this.id = segments[segments.length - 1]; // this is just a demo so don't check for a single /
        this.name = name == null ? "" : name;
        this.quantity = quantity;
        this.type = type == null ? "" : type;
        this.status = status;
        this.details = details == null ? new Booking[0] : removeNullEnElements(details);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] removeNullEnElements(T[] a) {
        List<T> list = new ArrayList<T>(Arrays.asList(a));
        list.removeAll(Collections.singleton(null));
        return list.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), list.size()));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getType() {
        return type;
    }

    public Booking[] getDetails() {
        return details;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void requestCancel() {
        status = BookingStatus.CANCEL_REQUESTED;
    }

    public void setConfirmed() {
        status = BookingStatus.CONFIRMED;
    }

    public String toString() {
        return String.format("{\"id\":\"%s\",\"name\":\"%s\",\"quantity\":\"%d\",\"type\":\"%s\",\"status\":\"%s\"}",
                    id, name, quantity, type, status);
    }

    public void setCanceled() {
        status = BookingStatus.CANCELLED;
    }

    public void setConfirming() {
        status = BookingStatus.CONFIRMING;
    }

    @JsonIgnore
    public boolean isCancelPending() {
        return status == BookingStatus.CANCEL_REQUESTED;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public boolean merge(Booking booking) {
        if (!id.equals(booking.getId()))
            return false; // or throw an exception

        name = booking.getName();
        quantity = booking.getQuantity();
        status = booking.getStatus();

        return true;
    }

    @JsonIgnore
    public String getEncodedId() {
        try {
            return URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return id; // TODD do it in the constructor
        }
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(this);
    }

    public static Booking fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, Booking.class);
        } catch (IOException e) {
            return new Booking(e);
        }
    }

    public static List<Booking> listFromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return Arrays.asList(mapper.readValue(json, Booking[].class));
    }

    @JsonIgnore
    public IOException getDecodingException() {
        return decodingException;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;

        if (!getId().equals(booking.getId())) return false;
        if (!getName().equals(booking.getName())) return false;
        if (!getQuantity().equals(booking.getQuantity())) return false;
        if (getStatus() != booking.getStatus()) return false;
        if (!getType().equals(booking.getType())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getDetails(), booking.getDetails());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getQuantity().hashCode();
        result = 31 * result + getStatus().hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }

/*    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;

        Booking booking = (Booking) o;

        return getId().equals(booking.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }*/

    public enum BookingStatus {
        CONFIRMED, CANCELLED, PROVISIONAL, CONFIRMING, CANCEL_REQUESTED
    }
}
