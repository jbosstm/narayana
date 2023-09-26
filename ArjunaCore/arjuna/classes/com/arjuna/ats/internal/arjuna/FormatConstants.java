/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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