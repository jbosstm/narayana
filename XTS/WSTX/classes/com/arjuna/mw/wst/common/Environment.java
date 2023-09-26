/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wst.common;

/**
 */

public interface Environment
{

    public static final String COORDINATOR_URL = "org.jboss.jbossts.xts.coordinatorURL";
    public static final String COORDINATOR_SCHEME = "org.jboss.jbossts.xts.coordinator.scheme";
    public static final String COORDINATOR_HOST = "org.jboss.jbossts.xts.coordinator.host";
    public static final String COORDINATOR_PORT = "org.jboss.jbossts.xts.coordinator.port";
    public static final String COORDINATOR_PATH = "org.jboss.jbossts.xts.coordinator.path";

    public static final String COORDINATOR11_URL = "org.jboss.jbossts.xts11.coordinatorURL";
    public static final String COORDINATOR11_SCHEME = "org.jboss.jbossts.xts11.coordinator.scheme";
    public static final String COORDINATOR11_HOST = "org.jboss.jbossts.xts11.coordinator.host";
    public static final String COORDINATOR11_PORT = "org.jboss.jbossts.xts11.coordinator.port";
    public static final String COORDINATOR11_PATH = "org.jboss.jbossts.xts11.coordinator.path";
}