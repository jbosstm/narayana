/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * <xsd:simpleType name="Vote">
 *   <xsd:restriction base="xsd:string">
 *     <xsd:enumeration value="VoteCommit"/>
 *     <xsd:enumeration value="VoteRollback"/>
 *     <xsd:enumeration value="VoteReadOnly"/>
 *   </xsd:restriction>
 * </xsd:simpleType>
 */
/**
 * Class representing vote enumerations.
 * @author kevin
 */
public class Vote extends Enumerated
{
    /**
     * Serial version UID for serialisation.
     */
    
    /**
     * The commit vote.
     */
    public static final Vote VOTE_COMMIT = new Vote("VoteCommit") ;
    /**
     * The rollback vote.
     */
    public static final Vote VOTE_ROLLBACK = new Vote("VoteRollback") ;
    /**
     * The read only vote.
     */
    public static final Vote VOTE_READ_ONLY = new Vote("VoteReadOnly") ;
    
    /**
     * The map of enumerations.
     */
    private static final Map ENUM_MAP = generateMap(new Enumerated[] {
        VOTE_COMMIT, VOTE_ROLLBACK, VOTE_READ_ONLY
    }) ;
    
    /**
     * Construct the vote enumeration with the specified value.
     * @param value The value of the enumeration.
     */
    private Vote(final String value)
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
        return toVote((String)value) ;
    }

    /**
     * Return the enumeration for the specified value.
     * @param value The value.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the value is not valid.
     * @message com.arjuna.webservices.wsat.Vote_1 [com.arjuna.webservices.wsat.Vote_1] - Invalid vote enumeration: {0}
     */
    public static Vote toVote(final String value)
        throws InvalidEnumerationException
    {
        final Object vote = ENUM_MAP.get(value) ;
        if (vote == null)
        {
            final String pattern = WSTLogger.arjLoggerI18N.getString("com.arjuna.webservices.wsat.Vote_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {value}) ;
            throw new InvalidEnumerationException(message) ;
        }
        return (Vote)vote ;
    }
}
