/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;



/**
 * An implementation of this interface should be returned by
 * an AbstractRecord if it has caused a heuristic decision.
 *
 * @version $Id: HeuristicInformation.java 2342 2006-03-30 13:06:17Z  $
 * @author Richard A. Begg (richard.begg@arjuna.com)
 */
/**
 * @deprecated as of 5.2.2.Final In a subsequent release we will be providing this functionality via JMX MBeans
 */
@Deprecated
public interface HeuristicInformation
{
    /**
     * The type of heuristic.
     * @return the heuristic type
     */
    public int getHeuristicType();

    /**
     * A reference to the entity that caused the heuristic.
     * @return the entity reference
     */
    public Object getEntityReference();
}