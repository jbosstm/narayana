/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsba;

import java.text.MessageFormat;
import java.util.Map;

import javax.xml.namespace.QName;

import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.Enumerated;
import com.arjuna.webservices.util.InvalidEnumerationException;

/*
 * <xsd:simpleType name="StateType">
 *   <xsd:restriction base="xsd:QName">
 *     <xsd:enumeration value="wsba:Active"/>
 *     <xsd:enumeration value="wsba:Canceling"/>
 *     <xsd:enumeration value="wsba:Canceling-Active"/>
 *     <xsd:enumeration value="wsba:Canceling-Completing"/>
 *     <xsd:enumeration value="wsba:Completing"/>
 *     <xsd:enumeration value="wsba:Completed"/>
 *     <xsd:enumeration value="wsba:Closing"/>
 *     <xsd:enumeration value="wsba:Compensating"/>
 *     <xsd:enumeration value="wsba:Failing-Active"/>
 *     <xsd:enumeration value="wsba:Failing-Canceling"/>
 *     <xsd:enumeration value="wsba:Failing-Completing"/>
 *     <xsd:enumeration value="wsba:Failing-Compensating"/>
 *     <xsd:enumeration value="wsba:NotCompleting"/>
 *     <xsd:enumeration value="wsba:Exiting"/>
 *     <xsd:enumeration value="wsba:Ended"/>
 *   </xsd:restriction>
 * </xsd:simpleType>
 */
/**
 * Class representing state enumerations.
 * @author kevin
 */
public class State extends Enumerated
{
    /**
     * Serial version UID for serialisation.
     */

    /**
     * The Active state.
     */
    public static final State STATE_ACTIVE = new State("Active") ;
    /**
     * The Canceling state.
     */
    public static final State STATE_CANCELING = new State("Canceling") ;
    /**
     * The Canceling-Active state.
     */
    public static final State STATE_CANCELING_ACTIVE = new State("Canceling-Active") ;
    /**
     * The Canceling-Completing state.
     */
    public static final State STATE_CANCELING_COMPLETING = new State("Canceling-Completing") ;
    /**
     * The Completing state.
     */
    public static final State STATE_COMPLETING = new State("Completing") ;
    /**
     * The Completed state.
     */
    public static final State STATE_COMPLETED = new State("Completed") ;
    /**
     * The Closing state.
     */
    public static final State STATE_CLOSING = new State("Closing") ;
    /**
     * The Compensating state.
     */
    public static final State STATE_COMPENSATING = new State("Compensating") ;
    /**
     * The Failing-Active state.
     */
    public static final State STATE_FAILING_ACTIVE = new State("Failing-Active") ;
    /**
     * The Failing-Canceling state.
     */
    public static final State STATE_FAILING_CANCELING = new State("Failing-Canceling") ;
    /**
     * The Failing-Completing state.
     */
    public static final State STATE_FAILING_COMPLETING = new State("Failing-Completing") ;
    /**
     * The Failing-Compensating state.
     */
    public static final State STATE_FAILING_COMPENSATING = new State("Failing-Compensating") ;
    /**
     * The Exiting state.
     */
    public static final State STATE_EXITING = new State("Exiting") ;
    /**
     * The NotCompleting state.
     */
    public static final State STATE_NOT_COMPLETING = new State("NotCompleting") ;

    /**
     * The Ended state.
     */
    public static final State STATE_ENDED = new State("Ended") ;

    /**
     * The map of enumerations.
     */
    private static final Map ENUM_MAP = generateMap(new Enumerated[] {
        STATE_ACTIVE, STATE_CANCELING, STATE_CANCELING_ACTIVE,
        STATE_CANCELING_COMPLETING, STATE_COMPLETING, STATE_COMPLETED,
        STATE_CLOSING, STATE_COMPENSATING, STATE_FAILING_ACTIVE, STATE_FAILING_CANCELING,
        STATE_FAILING_COMPLETING, STATE_FAILING_COMPENSATING, STATE_EXITING,
        STATE_NOT_COMPLETING, STATE_ENDED
    }) ;

    /**
     * Construct the state enumeration with the specified value.
     * @param value The localName of the state enumeration.
     */
    private State(final String value)
    {
        super(getQualifiedName(value)) ;
    }

    /**
     * Get the value of this enumeration.
     * @return the value.
     */
    public QName getValue()
    {
        return (QName)getKey() ;
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
        return toState11((QName)value) ;
    }

    /**
     * Return the enumeration for the specified value.
     * @param name The name.
     * @return The enumeration.
     * @throws com.arjuna.webservices.util.InvalidEnumerationException if the name is not valid.
     */
    public static State toState11(final String name)
        throws InvalidEnumerationException
    {
        return toState11(getQualifiedName(name)) ;
    }

    /**
     * Return the enumeration for the specified value.
     * @param qName The qName value.
     * @return The enumeration.
     * @throws com.arjuna.webservices.util.InvalidEnumerationException if the value is not valid.
     */
    public static State toState11(final QName qName)
        throws InvalidEnumerationException
    {
        final Object state = ENUM_MAP.get(qName) ;
        if (state == null)
        {
            throw new InvalidEnumerationException(WSTLogger.i18NLogger.get_webservices11_wsba_State_1(qName)) ;
        }
        return (State)state ;
    }

    /**
     * Get the qualified name.
     * @param name The name to qualify.
     * @return The qualified name.
     */
    private static QName getQualifiedName(final String name)
    {
        return new QName(BusinessActivityConstants.WSBA_NAMESPACE,
                name, BusinessActivityConstants.WSBA_PREFIX) ;
    }
}