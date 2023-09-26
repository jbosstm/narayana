/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

 package org.jboss.jbossts.star.provider;

/**
 * Unable to contact the REST based Transaction Coordinator
 */
public class TMUnavailableException extends RuntimeException {
    public TMUnavailableException(String message) {
        super(message);
    }
}