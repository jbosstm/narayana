/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Environment.java,v 1.6 2004/09/09 08:48:48 kconner Exp $
 */

package com.arjuna.wsc11.common;

/**
 */

public interface Environment
{

    public static final String XTS_BIND_ADDRESS = "org.jboss.jbossts.xts11.bind.address";
    public static final String XTS_BIND_PORT = "org.jboss.jbossts.xts11.bind.port";
    public static final String XTS_SECURE_BIND_PORT = "org.jboss.jbossts.xts11.bind.port.secure";
    public static final String XTS_COMMAND_LINE_COORDINATOR_URL = "org.jboss.jbossts.xts11.command.line.coordinatorURL";

}