/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.sra.demo.service;

public class BookingException extends Exception {
    int reason;

    public int getReason() {
        return reason;
    }

    public BookingException(int reason, String message) {
        super(message);

        this.reason = reason;

    }
}