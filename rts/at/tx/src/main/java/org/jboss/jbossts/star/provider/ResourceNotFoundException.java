/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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