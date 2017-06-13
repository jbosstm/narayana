/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2010
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.resource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.service.Coordinator;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxStatus;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.RecordListIterator;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.jbossts.star.util.media.txstatusext.CoordinatorElement;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatusElement;
import org.jboss.jbossts.star.util.media.txstatusext.TwoPhaseAwareParticipantElement;
import org.jboss.logging.Logger;

@XmlRootElement(name = "transaction")
public class Transaction extends AtomicAction
{
    protected final static Logger log = Logger.getLogger(Transaction.class);

    private long age = System.currentTimeMillis();
    private Coordinator coordinator = null;
    private String initiator;
    private String recoveryUrl = null;
    private Collection<String> volatileParticipants; // synchronizations

    public Transaction()
    {
        super();
        volatileParticipants = new ArrayList<String>();
    }

    public Transaction(Coordinator coordinator, String initiator)
    {
        this();
        this.coordinator = coordinator;
        this.initiator = initiator;
    }

    public Transaction(Uid uid) {
        super(uid);
        volatileParticipants = new ArrayList<String>();
    }

    public CoordinatorElement toXML() {
        CoordinatorElement coordinatorElement = new CoordinatorElement();

        coordinatorElement.setCreated(new Date(System.currentTimeMillis() - age));
        coordinatorElement.setStatus(TransactionStatusElement.valueOf(getStatus()));
        coordinatorElement.setTimeout(getTimeout());

        for (String vParticipant : volatileParticipants)
            coordinatorElement.addVolatileParticipant(vParticipant);

        return coordinatorElement;
    }

    @XmlElement
    public String getInitiator()
    {
        return initiator;
    }

    @XmlElement
    public String getAge()
    {
        return Long.toString(System.currentTimeMillis() - age);
    }

    @XmlAttribute
    public String getStatus()
    {
        return getStatus(lookupStatus());
    }

    protected int lookupStatus() {
        return status();
    }

    public String getStatus(int status)
    {
        TxStatus txStatus = TxStatus.fromActionStatus(status);

        if (txStatus.equals(TxStatus.TransactionStatusUnknown))
            return ""; //ActionStatus.stringForm(lookupStatus());

        return txStatus.name();
    }

    public TxStatus getTxStatus() {
        return TxStatus.fromActionStatus(lookupStatus());
    }

    public String getRecoveryUrl()
    {
        return recoveryUrl;
    }

    protected RESTRecord getParticipantRecord(String txId, String coordinatorUrl, String participantUrl, String terminateUrl, String recoveryUrlBase) {
        return new RESTRecord(txId, coordinatorUrl, participantUrl, terminateUrl);
    }

    public String enlistParticipant(String coordinatorUrl, String participantUrl, String recoveryUrlBase,
                                    String terminateUrl) {
        if (findParticipant(participantUrl) != null)
            return null;    // already enlisted

        String txId = get_uid().fileStringForm();
        RESTRecord p = getParticipantRecord(txId, coordinatorUrl, participantUrl, terminateUrl, recoveryUrlBase);
        String coordinatorId = p.get_uid().fileStringForm();

        recoveryUrl = recoveryUrlBase + txId + '/' + coordinatorId;
        p.setRecoveryURI(recoveryUrl);

        if (add(p) != AddOutcome.AR_REJECTED)
            return coordinatorId;

        return null;
    }

    public String enlistParticipant(String coordinatorUrl, String participantUrl, String recoveryUrlBase,
                                    String commitURI, String prepareURI, String rollbackURI, String commitOnePhaseURI) {
        if (findParticipant(participantUrl) != null)
            return null;    // already enlisted

        String txId = get_uid().fileStringForm();
        RESTRecord p = new RESTRecord(txId, coordinatorUrl, participantUrl,
                commitURI, prepareURI, rollbackURI, commitOnePhaseURI);
        String coordinatorId = p.get_uid().fileStringForm();

        recoveryUrl = recoveryUrlBase + txId + '/' + coordinatorId;
        p.setRecoveryURI(recoveryUrl);

        if (add(p) != AddOutcome.AR_REJECTED)
            return coordinatorId;

        return null;
    }

    /**
     * Determine whether a participant is enlisted in this transaction and the commitment
     * is not running.
     *
     * @param participantUrl the participant url to search for
     * @return false if the participant is not enlisted or if the commitment protocol is
     * already running
     */
    public boolean isEnlisted(String participantUrl) {
        return findParticipant(participantUrl) != null;
    }

    public boolean forgetParticipant(String participantUrl) {
        return pendingList.remove(findParticipant(participantUrl));
    }

    public void getParticipants(Collection<String> enlistmentIds)
    {
        if (pendingList == null)
            return;

        // only add faults for pending records
        AbstractRecord r = pendingList.peekFront();

        while (r != null)
        {
            if (r instanceof RESTRecord)
                enlistmentIds.add(r.get_uid().fileStringForm());

            r = pendingList.peekNext(r);
        }
    }

    public void setFault(String fault)
    {
        if (pendingList == null)
            return;

        // only add faults for pending records
        AbstractRecord r = pendingList.peekFront();

        while (r != null)
        {
            if (r instanceof RESTRecord)
                ((RESTRecord) r).setFault(fault);

            r = pendingList.peekNext(r);
        }
    }

    public boolean isFinishing()
    {
        return getTxStatus().isFinishing();
    }
    public boolean isAlive()
    {
        return getTxStatus().isActive();
    }

    public boolean isRunning()
    {
        return getTxStatus().isRunning();
    }


    protected RESTRecord findParticipant(String participantUrl) {
        if (pendingList != null) {
            RecordListIterator i = new RecordListIterator(pendingList);
            AbstractRecord r;

            while ((r = i.iterate()) != null) {
                if (r instanceof RESTRecord) {
                    RESTRecord rr = (RESTRecord) r;

                    if (rr.getParticipantURI().equals(participantUrl))
                        return rr;
                }
            }
        }

        return null;
    }

    public boolean getStatus(TwoPhaseAwareParticipantElement participantElement, String participantUrl) {
        RESTRecord rr = findParticipant(participantUrl);

        if (rr == null)
            return false;

        try {
            participantElement.setStatus(TransactionStatusElement.valueOf(rr.getStatus()));
        } catch (IllegalArgumentException e) {
            participantElement.setStatus(TransactionStatusElement.TransactionStatusNone);
        }
        participantElement.setCreated(new Date(rr.getAge()));

        return true;
    }

    public void addVolatileParticipant(String vparticipantURI) {
        volatileParticipants.add(vparticipantURI);
    }

    private Collection<String> enlistmentIds;
    @Override
    protected boolean beforeCompletion() {
        boolean commit = true;

        // NB volatileParticipants cannot change once 2PC has commenced

        for (String uri : volatileParticipants) {
            try {
                new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK}, uri, "PUT", null);
            } catch (HttpResponseException e) {
                commit = false;

                if (log.isDebugEnabled())
                    log.debugf(e, "volatile participant %s error %s during beforeCompletion", uri, e.getMessage());
            }
        }

        if (!super.beforeCompletion())
            commit = false;

        // Get a list of the participants before they check-out of the transaction
        enlistmentIds = new ArrayList<String>();
        getParticipants(enlistmentIds);

        return commit;
    }

    @Override
    protected boolean afterCompletion(int arjunaStatus) {
        return afterCompletion(arjunaStatus, false);
    }

    @Override
    protected boolean afterCompletion(int arjunaStatus, boolean report_heuristics) {
        // NB volatileParticipants collection cannot change once 2PC has commenced

        if (volatileParticipants.size() != 0) {
            String status = TxSupport.toStatusContent(TxStatus.fromActionStatus(arjunaStatus).name());

            for (String uri : volatileParticipants) {
                try {
                    new TxSupport().httpRequest(new int[] {HttpURLConnection.HTTP_OK},
                            uri, "PUT", TxMediaType.TX_STATUS_MEDIA_TYPE, status);
                } catch (HttpResponseException e) {
                    if (log.isDebugEnabled())
                        log.debugf(e, "volatile participant %s error %s during afterCompletion", uri, e.getMessage());
                }
            }
        }

        // for REST-AT tell the coordinator to clean up
        if (coordinator != null && (failedList == null || failedList.size() == 0)) {
            coordinator.removeTxState(arjunaStatus, this, enlistmentIds);
        }

        return super.afterCompletion(arjunaStatus, report_heuristics);
    }
}
