package com.arjuna.wst;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/**
 * The interface for a persistable resource.
 */
public interface PersistableParticipant
{
    /**
     * Save the state of the particpant to the specified input object stream.
     * @param oos The output output stream.
     * @return true if persisted, false otherwise.
     */
    public boolean saveState(final OutputObjectState oos) ;
    
    /**
     * Restore the state of the particpant from the specified input object stream.
     * @param ios The Input object stream.
     * @return true if restored, false otherwise.
     */
    public boolean restoreState(final InputObjectState ios);
}