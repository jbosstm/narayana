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

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.RecordListIterator;
import org.jboss.jbossts.star.util.TxSupport;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;

@XmlRootElement(name = "transaction")
public class Transaction extends AtomicAction
{
    private long age = System.currentTimeMillis();
    private String initiator;
    private String recoveryUrl = null;

    public Transaction()
    {
        super();
    }

    public Transaction(String initiator)
    {
        this();
        this.initiator = initiator;
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
        return getStatus(status());
    }

    public String getStatus(int status)
    {
        switch (status) {
        case ActionStatus.ABORT_ONLY:
            return TxSupport.ABORT_ONLY;
        case ActionStatus.ABORTING:
            return TxSupport.ABORTING;
        case ActionStatus.ABORTED:
            return TxSupport.ABORTED;
        case ActionStatus.COMMITTING:
            return TxSupport.COMMITTING;
        case ActionStatus.COMMITTED:
            return TxSupport.COMMITTED;
        case ActionStatus.H_ROLLBACK:
            return TxSupport.H_ROLLBACK;
        case ActionStatus.H_COMMIT:
            return TxSupport.H_COMMIT;
        case ActionStatus.H_HAZARD:
            return TxSupport.H_HAZARD;
        case ActionStatus.H_MIXED:
            return TxSupport.H_MIXED;
        case ActionStatus.PREPARING:
            return TxSupport.PREPARING;
        case ActionStatus.PREPARED:
            return TxSupport.PREPARED;
        case ActionStatus.RUNNING:
            return TxSupport.RUNNING;
        default:
            return ""; //ActionStatus.stringForm(super.status());
        }

    }

    public String getRecoveryUrl()
    {
        return recoveryUrl;
    }

    public String enlistParticipant(String coordinatorUrl, String participantUrl, String terminateUrl, String recoveryUrlBase) {
        if (findParticipant(terminateUrl) != null)
            return null;    // already enlisted

        RESTRecord p = new RESTRecord(coordinatorUrl, participantUrl, terminateUrl, get_uid().fileStringForm());
        String coordinatorId = p.get_uid().fileStringForm();
        recoveryUrl = recoveryUrlBase + coordinatorId;
        p.setRecoveryUrl(recoveryUrl);

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
        RESTRecord rr = findParticipant(participantUrl);

        if (rr != null)
            return pendingList.remove(rr);

        return false;
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
    public boolean isGone()
    {
         switch ( status() )
        {
            case ActionStatus.COMMITTED  :
            case ActionStatus.ABORTED    :
                  return true;
            default:
                return false;
        }
    }

    public boolean isFinished()
    {
        switch ( status() )
        {
            case ActionStatus.COMMITTED  :
            case ActionStatus.H_COMMIT   :
            case ActionStatus.H_MIXED    :
            case ActionStatus.H_HAZARD   :
            case ActionStatus.ABORTED    :
            case ActionStatus.H_ROLLBACK :
                return true;

                //case ActionStatus.INVALID: throw ...
            default:
                return false;
        }
    }

    public boolean isFinishing()
    {
        switch ( status() )
        {
            case ActionStatus.PREPARING  :
            case ActionStatus.COMMITTING   :
            case ActionStatus.ABORTING    :
                return true;
            default:
                return false;
        }
    }
    public boolean isAlive()
    {
        switch ( status() )
        {
            case ActionStatus.RUNNING    :
            case ActionStatus.ABORT_ONLY :
            case ActionStatus.PREPARING  :
            case ActionStatus.COMMITTING :
            case ActionStatus.ABORTING   :
            case ActionStatus.PREPARED   :
                return true;

                //case ActionStatus.INVALID:
            default:
                return false;
        }
    }

    public boolean isRunning()
    {
        switch ( status() )
        {
            case ActionStatus.RUNNING    :
                return true;
            default:
                return false;
        }
    }

    public boolean hasHeuristic()
    {
        switch ( status() )
        {
            case ActionStatus.H_COMMIT   :
            case ActionStatus.H_MIXED    :
            case ActionStatus.H_HAZARD   :
            case ActionStatus.H_ROLLBACK :
                return true;

            default:
                return false;
        }
    }

    public boolean isAborted() {
        return status() == ActionStatus.ABORTED;
    }


    private RESTRecord findParticipant(String participantUrl) {
        if (pendingList != null) {
            RecordListIterator i = new RecordListIterator(pendingList);
            AbstractRecord r;

            while ((r = i.iterate()) != null) {
                if (r instanceof RESTRecord) {
                    RESTRecord rr = (RESTRecord) r;

                    if (rr.getParticipant().equals(participantUrl))
                        return rr;
                }
            }
        }

        return null;
    }

}
