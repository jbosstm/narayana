package com.arjuna.ats.arjuna.tools.osb.mbean;

/**
 * Enumeration of the commit status of a participant in an action/transaction
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public enum ParticipantStatus {PREPARED, PENDING, FAILED, READONLY, HEURISTIC}
