package org.jboss.jbossts.xts.recovery.participant.ba;

import com.arjuna.ats.arjuna.common.Uid;

import java.util.NoSuchElementException;

/**
 * Abstract class defining API for managing both participant and coordinator recovery
 * On the participant this is responsible for saving WS-BA participant recovery records
 * during prepare, deleting them at commit and recreating and reactivating them during
 * recovery.
 * On the coordinator side it currently merely records whether coordinator recovery has
 * completed its first scan
 */
public abstract class XTSBARecoveryManager {
    /*****************************************************************************************/
    /* get or set the BA recovery manager singleton                                          */
    /*****************************************************************************************/

    /**
     * obtain a reference to the WS-BA recovery manager singleton instance
     * @return the singleton instance
     */
    public static XTSBARecoveryManager getRecoveryManager()
    {
        return theRecoveryManager;
    }

    /**
     * set the WS-BA recovery manager singleton instance
     * @param recoveryManager the instance to use as the recovery manager
     * @return the singleton previously in use or null if it was not previously set
     */
    public static XTSBARecoveryManager setRecoveryManager(XTSBARecoveryManager recoveryManager)
    {
        XTSBARecoveryManager old = theRecoveryManager;
        theRecoveryManager = recoveryManager;

        return old;
    }

    /*****************************************************************************************/
    /* API used by application client code to register XTS recovery modules at application   */
    /* startup and to unregister them when the application is unloaded                       */
    /*****************************************************************************************/

    /**
     * register an application specific recovery module which acts as a helper to recreate
     * a WS-BA durable participant from the participant's recovery data saved at prepare
     * @param module the module which will be used to identify and recreate participants
     * for the application
     * @throws NullPointerException if the supplied module is null
     */
    public abstract void registerRecoveryModule(XTSBARecoveryModule module);

    /**
     * unregister an application specific recovery module previously registered as
     * a helper to recretae WS-BA durable participants
     * @param module the module to be unregistered
     * @throws java.util.NoSuchElementException if the module is not currently registered
     */
    public abstract void unregisterRecoveryModule(XTSBARecoveryModule module) throws NoSuchElementException;

    /*****************************************************************************************/
    /* API used by participant service to create and delete participant recovery records in  */
    /* persistent store during normal operation                                              */
    /*****************************************************************************************/

    /**
     * save the supplied participant recovery record to persistent storage
     * @param participantRecoveryRecord
     */
    public abstract boolean writeParticipantRecoveryRecord(BAParticipantRecoveryRecord participantRecoveryRecord);

    /**
     * remove any participant recovery record with the supplied id from persistent storage
     * @param id
     */
    public abstract boolean deleteParticipantRecoveryRecord(String id);

    /*****************************************************************************************/
    /* API used by recovery scanning thread to add entries to the BA recovery manager's      */
    /* table of recovered participant recovery records.                                      */
    /*****************************************************************************************/

    /**
     * test whether the supplied uid identifies an active participant or a recovered but inactive
     * participant
     * @param uid
     */
    public abstract boolean isParticipantPresent(Uid uid);

    /**
     * add a recovered participant record to the table of unrecovered participants which
     * need to be recreated following recovery
     *
     * @param uid the uid under which the participant was saved in the file store
     * @param participantRecoveryRecord the in-memory represenattion of the recovery record
     * saved to disk
     */
    public abstract void addParticipantRecoveryRecord(Uid uid, BAParticipantRecoveryRecord participantRecoveryRecord);

    /**
     * see if a participant recovery record with a given id exists in the table of unrecovered
     * participants
     * @param id the identifier of the participant being sought
     * @return the participant recovery record with the supplied id or null if it is not found
     */
    public abstract BAParticipantRecoveryRecord findParticipantRecoveryRecord(String id);

    /**
     * process all entries in the recovered participant map and attempt to recreate the
     * application participant and activate it
     */
    public abstract void recoverParticipants();

    /**
     * test whether the first BA participant recovery scan has completed. this indicates whether
     * there may or may not still be unknown participant recovery records on disk. If the first
     * scan has not yet completed then a commit or rollback message for an unknown participant
     * must be dropped. If it has then a commit or rollback for an unknown participant must be
     * acknowledged with, respectively, a committed or aborted message.
     */
    public abstract boolean isParticipantRecoveryStarted();

    /**
     * test whether the first BA coordinator recovery scan has completed. this indicates whether
     * there may or may not still be unknown BA transcation records on disk. If the first
     * scan has not yet completed then a prepare message for an unknown participant
     * must be dropped. If it has then a perpare for an unknown participant must be
     * acknowledged with a rollback message.
     */
    public abstract boolean isCoordinatorRecoveryStarted();

    /**
     * record the fact thatwhether the first BA coordinator recovery scan has completed.
     */

    public abstract void setCoordinatorRecoveryStarted();

    /**
     * the singleton instance of the recovery manager
     */

    private static XTSBARecoveryManager theRecoveryManager = null;
}