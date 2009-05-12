package com.arjuna.webservices11.wsaddr.map;

import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.namespace.QName;

/**
 * MAPConstants is a wrapper class which works with class MAP
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
