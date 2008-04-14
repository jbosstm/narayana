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

/*
 * <xsd:simpleType name="Outcome">
 *   <xsd:restriction base="xsd:string">
 *     <xsd:enumeration value="Commit"/>
 *     <xsd:enumeration value="Rollback"/>
 *   </xsd:restriction>
 * </xsd:simpleType>
 */
/**
 * Class representing outcome enumerations.
 * @author kevin
 */
public class Outcome extends Enumerated
{
    /**
     * Serial version UID for serialisation.
     */
    
    /**
     * The commit outcome.
     */
    public static final Outcome OUTCOME_COMMIT = new Outcome("Commit") ;
    /**
     * The rollback outcome.
     */
    public static final Outcome OUTCOME_ROLLBACK = new Outcome("Rollback") ;
    
    /**
     * The map of enumerations.
     */
    private static final Map ENUM_MAP = generateMap(new Enumerated[] {
        OUTCOME_COMMIT, OUTCOME_ROLLBACK
    }) ;
    
    /**
     * Construct the outcome enumeration with the specified value.
     * @param value The value of the enumeration.
     */
    private Outcome(final String value)
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
        return toOutcome((String)value) ;
    }

    /**
     * Return the enumeration for the specified value.
     * @param value The value.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the value is not valid.
     * 
     * @message com.arjuna.webservices.wsat.Outcome_1 [com.arjuna.webservices.wsat.Outcome_1] - Invalid outcome enumeration: {0}
     */
    public static Outcome toOutcome(final String value)
        throws InvalidEnumerationException
    {
        final Object outcome = ENUM_MAP.get(value) ;
        if (outcome == null)
        {
            final String pattern = WSTLogger.log_mesg.getString("com.arjuna.webservices.wsat.Outcome_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {value}) ;
            throw new InvalidEnumerationException(message) ;
        }
        return (Outcome)outcome ;
    }
}
