/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2007,
 * @author Red Hat Middleware LLC.
 */
package org.jboss.jbossts.xts.recovery.participant.at;

import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import org.jboss.jbossts.xts.recovery.logging.RecoveryLogger;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.jboss.jbossts.xts.recovery.XTSRecoveryModule;

import java.util.Vector;
import java.util.Enumeration;
import java.util.HashMap;
import java.io.IOException;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for recovering XTS AT participants.
 * (instances of org.jboss.jbossts.xts.recovery.participant.at.ATParticipantRecoveryRecord)
 *
 * $Id$
 *
 */

public class ATParticipantRecoveryModule implements XTSRecoveryModule
{
    public ATParticipantRecoveryModule()
    {
        if (RecoveryLogger.logger.isDebugEnabled()) {
            RecoveryLogger.logger.debug("ATParticipantRecoveryModule created - default");
        }

        if (_recoveryStore == null)
        {
            _recoveryStore = StoreManager.getRecoveryStore();
        }

        _participantType = ATParticipantRecoveryRecord.type();
    }

    /**
     * called by the service startup code before the recovery module is added to the recovery managers
     * module list
     */
    public void install()
    {
        // the manager is needed by both the participant or the coordinator recovery modules so whichever
        // one gets there first creates it. No synchronization is needed as modules are only ever
        // installed in a single thread
        XTSATRecoveryManager atRecoveryManager = XTSATRecoveryManager.getRecoveryManager();
        if (atRecoveryManager == null) {
            atRecoveryManager = new XTSATRecoveryManagerImple(_recoveryStore);
            XTSATRecoveryManager.setRecoveryManager(atRecoveryManager);
        }
        // Subordinate Coordinators register durable participants with their parent transaction so
        // we need to add an XTSATRecoveryModule which knows about the registered participants

        subordinateRecoveryModule = new XTSATSubordinateRecoveryModule();
        
        atRecoveryManager.registerRecoveryModule(subordinateRecoveryModule);
    }

    /**
     * a recovery module which knows hwo to recover the participants registered by Subordinate AT Coordinators
     */

    private XTSATSubordinateRecoveryModule subordinateRecoveryModule;

    /**
     * called by the service shutdown code after the recovery module is removed from the recovery managers
     * module list
     */
    public void uninstall()
    {
        XTSATRecoveryManager.getRecoveryManager().unregisterRecoveryModule(subordinateRecoveryModule);
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass()
    {
        // Transaction type
        boolean ATParticipants = false ;

        // uids per transaction type
        InputObjectState acc_uids = new InputObjectState() ;

        try
        {
            if (RecoveryLogger.logger.isDebugEnabled()) {
                RecoveryLogger.logger.debug("ATParticipantRecoveryModule: first pass");
            }

            ATParticipants = _recoveryStore.allObjUids(_participantType, acc_uids );

        }
        catch ( ObjectStoreException ex )
        {
            RecoveryLogger.i18NLogger.warn_participant_at_ATParticipantRecoveryModule_1(ex);
        }

        if ( ATParticipants )
        {
            _participantUidVector = processParticipants( acc_uids ) ;
        }
    }

    public void periodicWorkSecondPass()
    {
        if (RecoveryLogger.logger.isDebugEnabled()) {
            RecoveryLogger.logger.debug("ATParticipantRecoveryModule: Second pass");
        }

        processParticipantsStatus() ;
    }

    private void doRecoverParticipant( Uid recoverUid )
    {
        // Retrieve the participant from its original process.

        if (RecoveryLogger.logger.isDebugEnabled()) {
            RecoveryLogger.logger.debug("participant type is " + _participantType + " uid is " +
                    recoverUid.toString());
        }

        // we don't need to use a lock here because we only attempt the read
        // when the uid is inactive which means it cannto be pulled out form under our
        // feet at commit. uniqueness of uids also means we can't be foiled by a reused
        // uid.

        XTSATRecoveryManager recoveryManager = XTSATRecoveryManager.getRecoveryManager();

        if (!recoveryManager.isParticipantPresent(recoverUid)) {
            // ok, the participant can neither be active nor loaded awaiting recreation by
            // an application recovery module so we need to load it
            try {
                // retrieve the data for the participant
                InputObjectState inputState = _recoveryStore.read_committed(recoverUid, _participantType);

                if (inputState != null) {
                    try {
                        String participantRecordClazzName = inputState.unpackString();
                        try {
                            // create a participant engine instance and tell it to recover itself
                            Class participantRecordClazz = Class.forName(participantRecordClazzName);
                            ATParticipantRecoveryRecord participantRecord = (ATParticipantRecoveryRecord)participantRecordClazz.newInstance();
                            participantRecord.restoreState(inputState);
                            // ok, now insert into recovery map if needed
                            XTSATRecoveryManager.getRecoveryManager().addParticipantRecoveryRecord(recoverUid, participantRecord);
                        } catch (ClassNotFoundException cnfe) {
                            // oh boy, not supposed to happen -- n.b. either the user deployed 1.0
                            // last time and 1.1 this time or vice versa or something is rotten in
                            // the state of Danmark
                            RecoveryLogger.i18NLogger.error_participant_at_ATParticipantRecoveryModule_4(participantRecordClazzName, recoverUid, cnfe);
                        } catch (InstantiationException ie) {
                            // this is also worrying, log an error
                            RecoveryLogger.i18NLogger.error_participant_at_ATParticipantRecoveryModule_5(participantRecordClazzName, recoverUid, ie);
                        } catch (IllegalAccessException iae) {
                            // this is another configuration problem, log an error
                            RecoveryLogger.i18NLogger.error_participant_at_ATParticipantRecoveryModule_5(participantRecordClazzName, recoverUid, iae);
                        }
                    } catch (IOException ioe) {
                        // hmm, record corrupted? log this as a warning
                        RecoveryLogger.i18NLogger.error_participant_at_ATParticipantRecoveryModule_6(recoverUid, ioe);
                    }
                } else {
                    // hmm, it ought not to be able to disappear unless the recovery manager knows about it
                    // this is an error!
                    RecoveryLogger.i18NLogger.error_participant_at_ATParticipantRecoveryModule_7(recoverUid);
                }
            } catch (ObjectStoreException ose) {
                // if the object store is not working this is serious
                RecoveryLogger.i18NLogger.error_participant_at_ATParticipantRecoveryModule_8(recoverUid, ose);
            }
        }
    }

    private Vector processParticipants( InputObjectState uids )
    {
        Vector uidVector = new Vector() ;

        if (RecoveryLogger.logger.isDebugEnabled()) {
            RecoveryLogger.logger.debug("processing " + _participantType
                    + " WS-AT participants");
        }

        Uid NULL_UID = Uid.nullUid();
        Uid theUid = null;

        while (true)
        {
            try
            {
                theUid = UidHelper.unpackFrom( uids ) ;
            }
            catch ( Exception ex )
            {
                break;
            }

            if (theUid.equals( NULL_UID ))
            {
                break;
            }

            if (RecoveryLogger.logger.isDebugEnabled()) {
                RecoveryLogger.logger.debug("found WS-AT participant " + theUid);
            }

            uidVector.addElement( theUid ) ;
        }

        return uidVector ;
    }

    private void processParticipantsStatus()
    {
        if (_participantUidVector != null) {
        // Process the Vector of transaction Uids
        Enumeration participantUidEnum = _participantUidVector.elements() ;

        while ( participantUidEnum.hasMoreElements() )
        {
            Uid currentUid = (Uid) participantUidEnum.nextElement();

            try
            {
                if ( _recoveryStore.currentState( currentUid, _participantType) != StateStatus.OS_UNKNOWN )
                {
                    doRecoverParticipant( currentUid ) ;
                }
            }
            catch ( ObjectStoreException ex )
            {
                RecoveryLogger.i18NLogger.warn_participant_at_ATParticipantRecoveryModule_3(currentUid, ex);
            }
        }
        }

        // now get the AT recovery manager to try to activate recovered participants

        XTSATRecoveryManager.getRecoveryManager().recoverParticipants();
    }

    // 'type' within the Object Store for ATParticipant record.
    private String _participantType = ATParticipantRecoveryRecord.type() ;

    // Array of transactions found in the object store of the
    // ACCoordinator type.
    private Vector _participantUidVector = null ;

    // Reference to the Object Store.
    private static RecoveryStore _recoveryStore = null ;

    // This object provides information about whether or not a participant is currently active.

    private HashMap<String, ATParticipantRecoveryRecord> _recoveredParticipantMap ;
}