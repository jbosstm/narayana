/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.tools.log;

/**
 * Only allows the movement of heuristic participants to the prepared list.
 * Maybe allow general editing of both lists, including bidirectional movement (point?)
 * and deletion.
 */

public interface EditableTransaction
{   
    public void moveHeuristicToPrepared (int index) throws IndexOutOfBoundsException;
    
    public void deleteHeuristicParticipant (int index) throws IndexOutOfBoundsException;
    
    public String toString ();
}