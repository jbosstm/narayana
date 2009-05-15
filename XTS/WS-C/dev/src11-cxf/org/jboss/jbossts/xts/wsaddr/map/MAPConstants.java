package org.jboss.jbossts.xts.wsaddr.map;

import org.apache.cxf.ws.addressing.AddressingConstants;
import org.apache.cxf.ws.addressing.JAXWSAConstants;

/**
 * MAPConstants is a wrapper class which works with class MAP. This is the JBossWS CXF version
 */
public class MAPConstants
{
    MAPConstants(AddressingConstants implementation)
    {
        this.implementation = implementation;
    }

    public String getNoneURI() {
        return implementation.getNoneURI();
    }

    public static final String CLIENT_ADDRESSING_PROPERTIES = JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;

    public static final String CLIENT_ADDRESSING_PROPERTIES_INBOUND = JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND;

    public static final String CLIENT_ADDRESSING_PROPERTIES_OUTBOUND = JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND;

    public static final String SERVER_ADDRESSING_PROPERTIES_INBOUND = JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;

    public static final String SERVER_ADDRESSING_PROPERTIES_OUTBOUND = JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;

    private AddressingConstants implementation;
}
