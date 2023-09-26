/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc.common;

/**
 */

public interface Environment
{
    public static final String XTS_BIND_ADDRESS = "org.jboss.jbossts.xts.bind.address";
    public static final String XTS_BIND_PORT = "org.jboss.jbossts.xts.bind.port";
    public static final String XTS_SECURE_BIND_PORT = "org.jboss.jbossts.xts.bind.port.secure";
    public static final String XTS_COMMAND_LINE_COORDINATOR_URL = "org.jboss.jbossts.xts.command.line.coordinatorURL";
    public static final String XTS11_BIND_ADDRESS = "org.jboss.jbossts.xts11.bind.address";
    public static final String XTS11_BIND_PORT = "org.jboss.jbossts.xts11.bind.port";
    public static final String XTS11_SECURE_BIND_PORT = "org.jboss.jbossts.xts11.bind.port.secure";
    public static final String XTS11_COMMAND_LINE_COORDINATOR_URL = "org.jboss.jbossts.xts11.command.line.coordinatorURL";
}