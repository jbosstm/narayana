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

}
