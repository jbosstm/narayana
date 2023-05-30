/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

 package org.jboss.jbossts.star.provider;

/**
 * 404 exception
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}