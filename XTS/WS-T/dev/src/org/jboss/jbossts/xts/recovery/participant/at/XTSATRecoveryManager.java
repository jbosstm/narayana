package org.jboss.jbossts.xts.recovery.participant.at;

import com.arjuna.ats.arjuna.common.Uid;

import java.util.NoSuchElementException;

/**
 * Abstract class defining API for managing both paritcipant and coordinator recovery
 * On hteparticipant this is responsible for saving WS-AT participant recovery records
 * during prepare, deleting them at commit and recreating and reactivating them during
 * recovery.
 * On the coordinator side it currently merely records whether coordinator recovery has
 * copleted its first scan
 */
public abstract class XTSATRecoveryManager {
    /*****************************************************************************************/
    /* get or set the AT recovery manager singleton                                          */
    /*****************************************************************************************/

    /**
     * obtain a reference to the WS-AT recovery manager singleton instance
     * @return the singleton instance
     */
    public static XTSATRecoveryManager getRecoveryManager()
    {
        return theRecoveryManager;
    }

    /**
     * set the WS-AT recovery manager singleton instance
     * @param recoveryManager the instance to use as the recovery manager
     * @return the singleton previously in use or null if it was not previously set
     */
    public static XTSATRecoveryManager setRecoveryManager(XTSATRecoveryManager recoveryManager)
    {
        XTSATRecoveryManager old = theRecoveryManager;
        theRecoveryManager = recoveryManager;

        return old;
    }

    /*****************************************************************************************/
    /* API used by application client code to register XTS recovery modules at application   */
    /* startup and to unregister them when the application is unloaded                       */
    /*****************************************************************************************/

    /**
     * register an application specific recovery module which acts as a helper to recreate
     * a WS-AT durable participant from the participant's recovery data saved at prepare
     * @param module the module which will be used to identify and recreate participants
     * for the application
     * @throws NullPointerException if the supplied module is null
     */
    public abstract void registerRecoveryModule(XTSATRecoveryModule module);

    /**
     * unregister an application specific recovery module previously registered as
     * a helper to recretae WS-AT durable participants
     * @param module the module to be unregistered
     * @throws NoSuchElementException if the module is not currently registered
     */
    public abstract void unregisterRecoveryModule(XTSATRecoveryModule module) throws NoSuchElementException;

    /*****************************************************************************************/
    /* API used by participant service to create and delete participant recovery records in  */
    /* persistent store during normal operation                                              */
    /*****************************************************************************************/

    /**
     * save the supplied participant recovery record to persistent storage
     * @param participantRecoveryRecord
     */
    public abstract boolean writeParticipantRecoveryRecord(ATParticipantRecoveryRecord participantRecoveryRecord);

    /**
     * remove any participant recovery record with the supplied id from persistent storage
     * @param id
     */
    public abstract boolean deleteParticipantRecoveryRecord(String id);

    /*****************************************************************************************/
    /* API used by recovery scanning thread to add entries to the AT recovery manager's      */
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
    public abstract void addParticipantRecoveryRecord(Uid uid, ATParticipantRecoveryRecord participantRecoveryRecord);

    /**
     * see if a participant recovery record with a given id exists in the table of unrecovered
     * participants
     * @param id the identifier of the participant being sought
     * @return the participant recovery record with the supplied id or null if it is not found
     */
    public abstract ATParticipantRecoveryRecord findParticipantRecoveryRecord(String id);

    /**
     * process all entries in the recovered participant map and attempt to recreate the
     * application participant and activate it
     */
    public abstract void recoverParticipants();

    /**
     * test whether the first AT participant recovery scan has completed. this indicates whether
     * there may or may not still be unknown participant recovery records on disk. If the first
     * scan has not yet completed then a commit or rollback message for an unknown participant
     * must be dropped. If it has then a commit or rollback for an unknown participant must be
     * acknowledged with, respectively, a committed or aborted message.
     */
    public abstract boolean isParticipantRecoveryStarted();

    /**
     * test whether the first AT coordinator recovery scan has completed. this indicates whether
     * there may or may not still be unknown AT transcation records on disk. If the first
     * scan has not yet completed then a prepare message for an unknown participant
     * must be dropped. If it has then a perpare for an unknown participant must be
     * acknowledged with a rollback message.
     */
    public abstract boolean isCoordinatorRecoveryStarted();

    /**
     * test whether the first AT subordinate coordinator recovery scan has completed. this indicates
     * whether there may or may not still be unknown AT subtransaction records on disk. If the first
     * scan has not yet completed then a commit for an unknown subtransaction must raise an exception
     * delaying commit of the parent transaction.
     */
    public abstract boolean isSubordinateCoordinatorRecoveryStarted();

    /**
     * record the fact thatwhether the first AT coordinator recovery scan has completed.
     */

    public abstract void setCoordinatorRecoveryStarted();

    /**
     * record the fact thatwhether the first AT coordinator recovery scan has completed.
     */

    public abstract void setSubordinateCoordinatorRecoveryStarted();

    /**
     * the singleton instance of the recovery manager
     */

    private static XTSATRecoveryManager theRecoveryManager = null;
}
