/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.util;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Utility class providing access to build time and runtime configuration reporting functions.
 *
 * Replaces the old per-module Info (and in some cases Configuration and report) classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-10
 */
public class ConfigurationInfo
{
    public static String getVersion() {
        return "unknown";
    }

    public static String getSourceId() {
        return "unknown"; // .getBuildTimeProperty("SOURCEID");
    }

    public static void main (String[] args)
    {
        // build time info:
        System.out.println("Version: "+getVersion());
        System.out.println("SourceId: "+getSourceId());
        // run time info (probably empty as beans only load on demand):
        String beans = BeanPopulator.printState();
        System.out.print(beans);
    }
}
