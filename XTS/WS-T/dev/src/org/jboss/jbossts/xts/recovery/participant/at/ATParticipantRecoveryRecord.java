package org.jboss.jbossts.xts.recovery.participant.at;

import com.arjuna.wst.PersistableParticipant;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.webservices.logging.WSTLogger;

import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * asbstract class used to implement save, recover and reactivate API for durable
 * XTS participants. this is subclassed by both a 1.0 and a 1.1 specific class because
 * the activate operation needs to create a participant engine appropriate to the
 * protocol in use when the participant was saved.
 */
public abstract class ATParticipantRecoveryRecord implements PersistableParticipant {

    /**
     * construct the protocol-independent part of a WS-AT participant recovery record
     * @param id
     * @param participant
     */
    protected ATParticipantRecoveryRecord(String id, Durable2PCParticipant participant)
    {
        this.id = id;
        this.participant = participant;
        recoveryState = null;
        recoveryStateValid = false;
    }

    /**
     * Retrieve and save the state of the particpant to the specified input object stream.
     * @param oos The output output stream.
     * @return true if persisted, false otherwise.
     *
     */
    public final boolean saveState(OutputObjectState oos) {
        if (participant == null) {
            return false;
        }

        try {
            useSerialization = ATParticipantHelper.isSerializable(participant);
            recoveryState = ATParticipantHelper.getRecoveryState(useSerialization, participant);
            recoveryStateValid = true;
        } catch (Exception exception) {
            WSTLogger.i18NLogger.warn_recovery_participant_at_ATParticipantRecoveryRecord_saveState_1(id);
            // if we continue here then we cannot recover this transaction if we crash during
            // commit processing. we should strictly fail here to play safe but . . .

            recoveryStateValid = false;
        }

        try {
            oos.packString(id);
            saveEndpointReference(oos);
            oos.packBoolean(recoveryStateValid);
            if (recoveryStateValid) {
            oos.packBoolean(useSerialization);
                if (recoveryState != null) {
                    oos.packBoolean(true);
                    oos.packBytes(recoveryState);
                } else {
                    oos.packBoolean(false);
                }
            }
        } catch (XMLStreamException xmle) {
            WSTLogger.i18NLogger.warn_recovery_participant_at_ATParticipantRecoveryRecord_saveState_2(id, xmle);
            return false;
        } catch (IOException ioe) {
            WSTLogger.i18NLogger.warn_recovery_participant_at_ATParticipantRecoveryRecord_saveState_3(id, ioe);
            return false;
        }

        return true;
    }

    /**
     * Restore the state of the particpant from the specified input object stream.
     *
     * @param ios The Input object stream.
     * @return true if restored, false otherwise.
     *
     */
    public boolean restoreState(InputObjectState ios) {
        try {
            id = ios.unpackString();
            restoreEndpointReference(ios);
            recoveryStateValid = ios.unpackBoolean();
            if (recoveryStateValid) {
                useSerialization = ios.unpackBoolean();
                if (ios.unpackBoolean()) {
                    recoveryState = ios.unpackBytes();
                } else {
                    recoveryState =  null;
                }
            }
        } catch (XMLStreamException xmle) {
            WSTLogger.i18NLogger.warn_recovery_participant_at_ATParticipantRecoveryRecord_restoreState_1(id, xmle);
            return false;
        } catch (IOException ioe) {
            WSTLogger.i18NLogger.warn_recovery_participant_at_ATParticipantRecoveryRecord_restoreState_2(id, ioe);
            return false;
        }
        
        return true;
    }

    /**
     * called during recovery processing to attempt to convert the restored application-
     * specific recovery state back into a participant
     * @param module the XTS recovery module to be used to attempt the conversion
     * @return
     */

    public boolean restoreParticipant(XTSATRecoveryModule module) throws Exception
    {
        if (participant != null) {
            // don't think this should ever happen
            return false;
        }

        if (recoveryStateValid) {
            if (useSerialization) {
                final ByteArrayInputStream bais = new ByteArrayInputStream(recoveryState) ;
                final ObjectInputStream ois = new ObjectInputStream(bais) ;

                participant = module.deserialize(getId(), ois);
            } else {
                participant = module.recreate(getId(), recoveryState);
            }

            if (participant != null) {
                return true;
            }
        } else {
            // the original participant did not provide a way to save its state so
            // throw an exception to notify this

            String mesg = WSTLogger.i18NLogger.get_recovery_participant_at_ATParticipantRecoveryRecord_restoreParticipant_1(id);

            throw new Exception(mesg);
        }
        
        return false;
    }

    public String getId()
    {
        return id;
    }

    /**
     * return the path string under which AT participant records are to be located in the TX
     * object store
     * @return
     */
    public static String type ()
    {
        return type;
    }

    /**
     * save the endpoint reference to the coordinator for this participant
     */
    protected abstract void saveEndpointReference(OutputObjectState oos) throws IOException, XMLStreamException;

    /**
     * restore the endpoint reference to the coordinator for this participant
     */
    protected abstract void restoreEndpointReference(InputObjectState ios) throws IOException, XMLStreamException;

    /**
     * create a participant engine to manage commit or rollback processing for the
     * participant and install it in the active participants table
     */
    public abstract void activate();

    /**
     * test whether a participant is currently activated with the id of this recovery record.
     *
     * @return true if a participant is currently activated with the id of this recovery record
     */
    public abstract boolean isActive();

    protected Durable2PCParticipant participant;
    protected String id;
    private boolean useSerialization;
    private byte[] recoveryState;
    private boolean recoveryStateValid;
    final private static String type = "/XTS/WSAT/ParticipantRecoveryRecord";
}

