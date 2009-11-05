package org.jboss.jbossts.xts.recovery.participant.ba;

import org.jboss.jbossts.xts.logging.XTSLogger;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;

import java.util.*;
import java.io.IOException;

/**
 * A class which manages the table of recovered participant records.
 *
 * @message org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_1 [org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_1] exception writing recovery record for WS-BA participant {0}
 * @message org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_2 [org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_2] exception removing recovery record {0} for WS-BA participant {1}
 * @message org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_3 [org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_3] exception reactivating recovered WS-BA participant {0}
 * @message org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_4 [org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_4] no XTS application recovery module found to help reactivate recovered WS-BA participant {0}
 * @message org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_5 [org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_5] Compensating orphaned subordinate WS-BA transcation {0}
 */
public class XTSBARecoveryManagerImple extends XTSBARecoveryManager {
    /**
     * constructor for use by BAParticipantRecoveryModule
     * @param objectStore
     */
    XTSBARecoveryManagerImple(ObjectStore objectStore)
    {
        this.objectStore = objectStore;
    }

    /**
     * register an application specific recovery module which acts as a helper to recreate
     * a WS-BA durable participant from the participant's recovery data saved at prepare
     *
     * @param module the module which will be used to identify and recreate participants
     *               for the application
     * @throws NullPointerException if the supplied module is null
     */
    public void registerRecoveryModule(XTSBARecoveryModule module) throws NullPointerException
    {
        // TODO other sanity checks?
        if (module == null) {
            throw new NullPointerException("XTSBARecoveryModule value must be non-null");
        }

        recoveryModules.add(module);
    }

    /**
     * unregister an application specific recovery module previously registered as
     * a helper to recretae WS-BA durable participants
     *
     * @param module the module to be unregistered
     * @throws java.util.NoSuchElementException
     *          if the module is not currently registered
     */
    public void unregisterRecoveryModule(XTSBARecoveryModule module) throws NoSuchElementException {
        if (!recoveryModules.remove(module)) {
            throw new NoSuchElementException();
        }
    }

    /**
     * save the supplied participant recovery record to persistent storage
     *
     * @param participantRecoveryRecord
     */
    public boolean writeParticipantRecoveryRecord(BAParticipantRecoveryRecord participantRecoveryRecord)
    {
        OutputObjectState oos = new OutputObjectState();
        // we need to be able to retrieve the class of the participant record so we can
        // create an instancde to load the rest of the participant specific data
        try {
            oos.packString(participantRecoveryRecord.getClass().getCanonicalName());
        } catch (IOException ioe) {
            if (XTSLogger.arjLoggerI18N.isWarnEnabled())
            {
                XTSLogger.arjLoggerI18N.warn("org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_1",
                        new Object[] {participantRecoveryRecord.getId()}, ioe);
            }
            return false;
        }

        if (participantRecoveryRecord.saveState(oos)) {
            Uid uid = new Uid();
            try {
                objectStore.write_committed(uid, type, oos);
                // we need to be able to identify the uid from the participant id
                // in order to delete it later
                uidMap.put(participantRecoveryRecord.getId(), uid);
                return true;
            } catch (ObjectStoreException ose) {
                if (XTSLogger.arjLoggerI18N.isWarnEnabled())
                {
                    XTSLogger.arjLoggerI18N.warn("org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_1",
                            new Object[] {participantRecoveryRecord.getId()}, ose);
                }
            }
        }

        return false;
    }

    /**
     * remove any participant recovery record with the supplied id from persistent storage
     * @param id the id of the participant whose recovery details are being deleted
     */
    public boolean deleteParticipantRecoveryRecord(String id)
    {
        Uid uid = uidMap.get(id);

        if (uid != null) {

            try {
                objectStore.remove_committed(uid, type);
                uidMap.remove(id);
                return true;
            } catch (ObjectStoreException ose) {
                if (XTSLogger.arjLoggerI18N.isWarnEnabled())
                {
                    XTSLogger.arjLoggerI18N.warn("org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_2",
                            new Object[] {uid, id},
                            ose);
                }
            }
        }

        return false;
    }

    /**
     * test whether the supplied uid identifies an active participant or a recovered but inactive
     * participant
     *
     * n.b. this method is not synchronized because of two assumptions: first, that uids are
     * never reused; and second that only recovery scanning (as a minimum, for a given recovery
     * record type) is single-threaded. Correctness of the first assumption ensures there are no
     * races with participant processor threads, the second races between recovery threads.
     *
     * @param uid
     */
    public boolean isParticipantPresent(Uid uid)
    {
        return (uidMap.get(uid) != null);
    }

    /**
     * add a recovered participant record to the table of unrecovered participants which
     * need to be recreated following recovery
     *
     * @param uid the uid under which the participant was saved in the file store
     * @param participantRecoveryRecord the in-memory representation of the recovery record
     * saved to disk
     */
    public void addParticipantRecoveryRecord(Uid uid, BAParticipantRecoveryRecord participantRecoveryRecord)
    {
        String participantId = participantRecoveryRecord.getId();
        if (recoveryMap.get(participantId) == null && !participantRecoveryRecord.isActive()) {
            // ok, we have not seen this entry before so add it to the list
            recoveryMap.put(participantId, participantRecoveryRecord);
            uidMap.put(participantId, uid);
        }
    }

    /**
     * see if a participant recovery record with a given id exists in the table of participants which
     * need to be recreated following recovery
     * @param id the identifier of the participant being sought
     * @return the participant recovery record with the supplied id or null if it is not found
     */
    public synchronized BAParticipantRecoveryRecord findParticipantRecoveryRecord(String id)
    {
        return recoveryMap.get(id);
    }

    /**
     * process all entries in the recovered participant map and attempt to recreate the
     * application participant and activate it
     */
    public void recoverParticipants()
    {
        // the first scan has been performed so allow processing of commit and rollback requests
        // for unknown ids to proceed now

        setParticipantRecoveryStarted();

        // we operate on a copy of the recovery modules to avoid the list being modified
        // by register and unregister operations while we are iterating over it
        // we should probably also make sure unregister does not proceed until
        // the current scan is complete . . .

        List<XTSBARecoveryModule> recoveryModulesCopy;
        synchronized (recoveryModules) {
            recoveryModulesCopy = new ArrayList<XTSBARecoveryModule>(recoveryModules);
        }

        // iterate through the participant recovery records and try to convert them to
        // a durable participant. if successful activate the participant and then remove the
        // recovery entry. note that since recovery is single threaded we can be sure that
        // no concurrent modifications will be made to the table while we are iterating and,
        // possibly, deleting via the iterator

        Iterator<BAParticipantRecoveryRecord> participantIterator = iterator();

        while(participantIterator.hasNext()) {
            BAParticipantRecoveryRecord participantRecoveryRecord = participantIterator.next();
            if (participantRecoveryRecord.isActive()) {
                // this participant must have already been activated by a by a previous
                // scan and been reloaded by this scan so just remove the entry

                participantIterator.remove();
            } else {
                Iterator<XTSBARecoveryModule> moduleIterator = recoveryModulesCopy.iterator();
                boolean found = false;

                while (!found && moduleIterator.hasNext()) {
                    XTSBARecoveryModule module = moduleIterator.next();
                    try {
                        if (participantRecoveryRecord.restoreParticipant(module)) {
                            // ok, this participant has recovered so tell it to
                            // activate and *then* remove it from the hashmap. this makes
                            // sure we don't open a window where an incoming
                            // commit may fail to find the object in either table

                            found = true;
                            participantRecoveryRecord.activate();

                            participantIterator.remove();
                        }
                    } catch (Exception e) {
                        // we foudn a helper but it failed to convert the participant record -- log a warning
                        // but leave the participant in the table for next time in case the helper has merely
                        // suffered a transient failure
                        found = true;
                        if (XTSLogger.arjLoggerI18N.isWarnEnabled())
                        {
                            XTSLogger.arjLoggerI18N.warn("org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_3",
                                    new Object[] {participantRecoveryRecord.getId()},
                                    e);
                        }
                    }
                }

                if (!found) {
                    // we failed to find a helper to convert a participant record so log a warning
                    // but leave it in the table for next time
                    if (XTSLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        XTSLogger.arjLoggerI18N.warn("org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_4",
                                new Object[] {participantRecoveryRecord.getId()});
                    }
                }
            }
        }

        // ok, see if we are now in a position to cull any prepared subordinate transactions

        cullOrphanedSubordinates();
    }

    /**
     * look for recovered subordinate transactions which do not have an associated proxy participant
     * rolling back any that are found. this only needs doing once after the first participant and
     * subordinate transaction recovery passes have both completed
     */
    private void cullOrphanedSubordinates()
    {
        if (culledOrphanSubordinates || !(subordinateCoordinateRecoveryStarted && participantRecoveryStarted)) {
            return;
        }
        culledOrphanSubordinates = true;

        SubordinateBACoordinator[] coordinators = SubordinateBACoordinator.listRecoveredCoordinators();
        for (SubordinateBACoordinator coordinator : coordinators) {
            if (coordinator.isOrphaned()) {
                if (XTSLogger.arjLoggerI18N.isWarnEnabled())
                {
                    XTSLogger.arjLoggerI18N.warn("org.jboss.transactions.xts.recovery.participant.ba.XTSBARecoveryModule_5",
                            new Object[] {coordinator.get_uid().stringForm()});
                }
                coordinator.cancel();
            }
        }
    }

    /**
     * return an iterator over the collection of entries in the table. n.b. this iterates
     * direct over the table so any deletions performed during iteration need to be done
     * via the iterator and need to be sure to avoid concurrent modification
     * @return
     */
    private synchronized Iterator<BAParticipantRecoveryRecord> iterator()
    {
        return recoveryMap.values().iterator();
    }

    /**
     * set a global flag indicating that the first BA participant recovery scan has
     * been performed.
     */
    private synchronized void setParticipantRecoveryStarted()
    {
        participantRecoveryStarted = true;
    }

    /**
     * test whether the first BA participant recovery scan has completed. this indicates whether
     * there may or may not still be unknown participant recovery records on disk. If the first
     * scan has not yet completed then a commit or rollback message for an unknown participant
     * must be dropped. If it has then a commit or rollback for an unknown participant must be
     * acknowledged with, respectively, a committed or aborted message.
     */
    public synchronized boolean isParticipantRecoveryStarted()
    {
        return participantRecoveryStarted;
    }

    /**
     * test whether the first BA coordinator recovery scan has completed. this indicates whether
     * there may or may not still be unknown BA transcation records on disk. If the first
     * scan has not yet completed then a prepare message for an unknown participant
     * must be dropped. If it has then a perpare for an unknown participant must be
     * acknowledged with a rollback message.
     */
    public synchronized boolean isCoordinatorRecoveryStarted() {
        return coordinatorRecoveryStarted;
    }

    public boolean isSubordinateCoordinatorRecoveryStarted() {
        return subordinateCoordinateRecoveryStarted;
    }

    /**
     * record the fact that the first BA coordinator recovery scan has completed.
     */

    public synchronized void setCoordinatorRecoveryStarted() {
        coordinatorRecoveryStarted = true;
    }

    public void setSubordinateCoordinatorRecoveryStarted() {
        subordinateCoordinateRecoveryStarted = true;

        // see if we are now in a position to cull any orphaned subordinate transactions
        cullOrphanedSubordinates();
    }

    /**
     * a global flag indicating whether the first BA participant recovery scan has
     * been performed.
     */
    private boolean participantRecoveryStarted = false;

    /**
     * a global flag indicating whether the first BA coordinator recovery scan has
     * been performed.
     */
    private boolean coordinatorRecoveryStarted = false;

    /**
     * a global flag indicating whether the first BA subordinate coordinator recovery scan has
     * been performed.
     */
    private boolean subordinateCoordinateRecoveryStarted = false;

    /**
     * a global flag indicating whether we have already reconciled the list of subordinate transactions
     * against their proxy participants looking for any orphans
     */
    private boolean culledOrphanSubordinates = false;

    /**
     * a map from participant ids to participant recovery records
     */
    private HashMap<String, BAParticipantRecoveryRecord> recoveryMap = new HashMap<String, BAParticipantRecoveryRecord>();

    /**
     * a map from participant id to the uid under which the participant has been saved in the
     * persistent store
     */
    private HashMap<String, Uid> uidMap = new HashMap<String, Uid>();

    /**
     * a map from participant ids to participant recover records
     */
    private List<XTSBARecoveryModule> recoveryModules = new ArrayList<XTSBARecoveryModule>();

    /**
     * the tx object store to be used for saving and deleting participant details
     */
    private ObjectStore objectStore;

    private final static String type = BAParticipantRecoveryRecord.type();
}