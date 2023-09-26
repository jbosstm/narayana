/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.util;

import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;

import java.security.PrivilegedAction;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class MapBuilderAction implements PrivilegedAction<MAPBuilder> {

    private static final MapBuilderAction INSTANCE = new MapBuilderAction();

    private MapBuilderAction() {

    }

    public static MapBuilderAction getInstance() {
        return INSTANCE;
    }

    @Override
    public MAPBuilder run() {
        return MAPBuilderFactory.getInstance().getBuilderInstance();
    }

}