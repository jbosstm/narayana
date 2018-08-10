/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.ats.internal.arjuna;

/**
 * Class with constants definitions for Xid formats and others.
 */
public final class FormatConstants {

    public static final int JTA_FORMAT_ID = 131077;

    public static final int JTS_FORMAT_ID = 131072;
    public static final int JTS_STRICT_FORMAT_ID = 131073;
    public static final int JTS_RESTRICTED_FORMAT_ID = 131074;

    public static final int XTS_BRIDGE_FORMAT_ID = 131080;

    public static final int RTS_BRIDGE_FORMAT_ID = 131081;

    /**
     * Returning true if format id corresponds with Narayana specific ids.
     */
    public static boolean isNarayanaFormatId(int xidFormatId) {
        return xidFormatId == JTA_FORMAT_ID
            || xidFormatId == JTS_FORMAT_ID
            || xidFormatId == XTS_BRIDGE_FORMAT_ID
            || xidFormatId == RTS_BRIDGE_FORMAT_ID;
    }
}
