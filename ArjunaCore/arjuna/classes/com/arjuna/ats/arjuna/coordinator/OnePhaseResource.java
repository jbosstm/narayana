/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.io.IOException;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/*
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: OnePhaseResource.java 2342 2006-03-30 13:06:17Z  $
 * @since 3.2.
 */

public interface OnePhaseResource
{

    /**
     * Return values from TwoPhaseOutcome to indicate success or failure.
     *
     * If this fails, then we will automatically attempt to rollback any
     * other participants.
     */

    public int commit ();

    /**
     * Return values from TwoPhaseOutcome to indicate success or failure.
     */
    
    public int rollback ();

    public void pack (OutputObjectState os) throws IOException;
    
    public void unpack (InputObjectState os) throws IOException;
    
}