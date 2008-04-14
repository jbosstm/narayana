/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.wsat;

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
     * @param localName The localName of the state enumeration.
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
     * @message com.arjuna.webservices.wsat.State_1 [com.arjuna.webservices.wsat.State_1] - Invalid fault type enumeration: {0}
     */
    public static State toState(final String value)
        throws InvalidEnumerationException
    {
        final Object state = ENUM_MAP.get(value) ;
        if (state == null)
        {
            final String pattern = WSTLogger.log_mesg.getString("com.arjuna.webservices.wsat.State_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {value}) ;
            throw new InvalidEnumerationException(message) ;
        }
        return (State)state ;
    }
}
