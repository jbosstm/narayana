package org.jboss.jbossts.xts.recovery.participant.at;

/**
 * an interface which can be implemented by AT participants which wish to save recovery state at prepare
 * which they can subsequently use to restore the participant state if a crash happnes bteween prepare and
 * commit/rollback
 */
public interface PersistableATParticipant
{
    byte[] getRecoveryState() throws Exception;
}
