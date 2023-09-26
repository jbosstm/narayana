/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.util;

import org.jboss.ws.api.addressing.MAPBuilder;

import java.security.AccessController;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PrivilegedMapBuilderFactory {

    private static final PrivilegedMapBuilderFactory INSTANCE = new PrivilegedMapBuilderFactory();

    private PrivilegedMapBuilderFactory() {

    }

    public static PrivilegedMapBuilderFactory getInstance() {
        return INSTANCE;
    }

    public MAPBuilder getBuilderInstance() {
        final MapBuilderAction mapBuilderAction = MapBuilderAction.getInstance();

        if (System.getSecurityManager() == null) {
            return mapBuilderAction.run();
        }

        return AccessController.doPrivileged(mapBuilderAction);
    }

}