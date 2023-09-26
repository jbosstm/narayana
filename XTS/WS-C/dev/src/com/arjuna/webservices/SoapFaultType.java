/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices;

import java.text.MessageFormat;
import java.util.Map;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.util.Enumerated;
import com.arjuna.webservices.util.InvalidEnumerationException;

/**
 * Class representing the soap fault type.
 * @author kevin
 */
public class SoapFaultType extends Enumerated
{
    /**
     * Serial version UID for serialisation.
     */
    private static final long serialVersionUID = 6597369531649776751L ;
    
    /**
     * The version mismatch type.
     */
    public static final SoapFaultType FAULT_VERSION_MISMATCH = new SoapFaultType("VersionMismatch") ;
    /**
     * The must understand type.
     */
    public static final SoapFaultType FAULT_MUST_UNDERSTAND = new SoapFaultType("MustUnderstand") ;
    /**
     * The data encoding unknown type.
     */
    public static final SoapFaultType FAULT_DATA_ENCODING_UNKNOWN = new SoapFaultType("DataEncodingUnknown") ;
    /**
     * The sender type.
     */
    public static final SoapFaultType FAULT_SENDER = new SoapFaultType("Sender") ;
    /**
     * The receiver type.
     */
    public static final SoapFaultType FAULT_RECEIVER = new SoapFaultType("Receiver") ;
    
    /**
     * The map of enumerations.
     */
    private static final Map ENUM_MAP = generateMap(new Enumerated[] {
        FAULT_VERSION_MISMATCH, FAULT_MUST_UNDERSTAND, FAULT_DATA_ENCODING_UNKNOWN,
        FAULT_SENDER, FAULT_RECEIVER
    }) ;
    
    /**
     * Construct the state enumeration with the specified value.
     * @param localName The localName of the state enumeration.
     */
    private SoapFaultType(final String value)
    {
        super(value) ;
    }
    
    /**
     * Get the value of this enumeration.
     * @return the value.
     */
    public String getValue()
    {
        return (String)getKey() ;
    }
    
    /**
     * Resolve the enumeration for the specified value.
     * @param value The value.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the value is not valid.
     */
    protected Enumerated resolveEnum(final Object value)
        throws InvalidEnumerationException
    {
        return toState((String)value) ;
    }
    
    /**
     * Return the enumeration for the specified value.
     * @param value The value.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the value is not valid.
     */
    public static SoapFaultType toState(final String value)
        throws InvalidEnumerationException
    {
        final Object state = ENUM_MAP.get(value) ;
        if (state == null)
        {
            throw new InvalidEnumerationException(WSCLogger.i18NLogger.get_webservices_SoapFaultType_1(value)) ;
        }
        return (SoapFaultType)state ;
    }
}