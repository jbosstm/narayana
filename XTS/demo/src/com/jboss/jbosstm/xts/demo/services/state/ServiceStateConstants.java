package com.jboss.jbosstm.xts.demo.services.state;

/**
 * constant values used by ServiceStateManager and its subclasses
 */
public class ServiceStateConstants {
    /**
     * bit mask identifying no tx
     */
    public static final int TX_TYPE_NONE = 0;
    /**
     * bit mask identifying a WS-AT tx
     */
    public static final int TX_TYPE_AT = 1;
    /**
     * bit mask identifying a WS-BA tx
     */
    public static final int TX_TYPE_BA = 2;
    /**
     * bit mask identifying the union of both TX types
     */
    public static final int TX_TYPE_BOTH = TX_TYPE_AT | TX_TYPE_BA;
}