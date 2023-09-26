/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsat;

import java.text.MessageFormat;
import java.util.Map;

import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.Enumerated;
import com.arjuna.webservices.util.InvalidEnumerationException;

/**
 * Class representing AT state enumerations.
 * @author kevin
 */
public class State extends Enumerated
{
    /**
     * The Active state.
     */
    public static final State STATE_ACTIVE = new State("Active") ;
    /**
     * The Preparing state.
     */
    public static final State STATE_PREPARING = new State("Preparing") ;
    /**
     * The Prepared state.
     */
    public static final State STATE_PREPARED = new State("Prepared") ;
    /**
     * The PreparedSuccess state.
     */
    public static final State STATE_PREPARED_SUCCESS = new State("PreparedSuccess") ;
    /**
     * The Committing state.
     */
    public static final State STATE_COMMITTING = new State("Committing") ;
    /**
     * The Aborting state.
     */
    public static final State STATE_ABORTING = new State("Aborting") ;

    /**
     * The map of enumerations.
     */
    private static final Map ENUM_MAP = generateMap(new Enumerated[] {
        STATE_ACTIVE, STATE_PREPARING, STATE_PREPARED, STATE_PREPARED_SUCCESS,
        STATE_COMMITTING, STATE_ABORTING
    }) ;

    /**
     * Construct the state enumeration with the specified value.
     * @param value The localName of the state enumeration.
     */
    private State(final String value)
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
     * @throws com.arjuna.webservices.util.InvalidEnumerationException if the value is not valid.
     */
    protected Enumerated resolveEnum(final Object value)
        throws InvalidEnumerationException
    {
        return toState11((String)value) ;
    }

    /**
     * Return the enumeration for the specified value.
     * @param value The value.
     * @return The enumeration.
     * @throws com.arjuna.webservices.util.InvalidEnumerationException if the value is not valid.
     */
    public static State toState11(final String value)
        throws InvalidEnumerationException
    {
        final Object state = ENUM_MAP.get(value) ;
        if (state == null)
        {
            throw new InvalidEnumerationException(WSTLogger.i18NLogger.get_webservices11_wsat_State_1(value)) ;
        }
        return (State)state ;
    }
}