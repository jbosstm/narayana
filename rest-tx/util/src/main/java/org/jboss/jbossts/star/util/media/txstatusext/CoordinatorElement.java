package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "coordinator")
@XmlType(propOrder = { "status", "created", "timeout", "txnURI", "terminatorURI",
        "durableParticipantEnlistmentURI", "volatileParticipantEnlistmentURI", "twoPhaseAware", "twoPhaseUnaware",
        "volatileParticipants"})
public class CoordinatorElement {
    private TransactionStatusElement status;
    private Date created;
    private long timeout;
    private String txnURI;
    private String terminatorURI;
    private String durableParticipantEnlistmentURI;
    private String volatileParticipantEnlistmentURI;
    private List<TwoPhaseAwareParticipantElement> twoPhaseAware = new ArrayList<TwoPhaseAwareParticipantElement>();
    private List<TwoPhaseUnawareParticipantElement> twoPhaseUnaware = new ArrayList<TwoPhaseUnawareParticipantElement>();
    private List<String> volatileParticipants = new ArrayList<String>();

    @XmlElement
    public Date getCreated() {
        return created;
    }
    @XmlElement
    public long getTimeout() {
        return timeout;
    }
    @XmlElement
    public String getTxnURI() {
        return txnURI;
    }
    @XmlElement
    public TransactionStatusElement getStatus() {
        return status;
    }
    @XmlElement
    public String getTerminatorURI() {
        return terminatorURI;
    }
    @XmlElement
    public String getDurableParticipantEnlistmentURI() {
        return durableParticipantEnlistmentURI;
    }
    @XmlElement
    public String getVolatileParticipantEnlistmentURI() {
        return volatileParticipantEnlistmentURI;
    }
    @XmlElement
    public List<TwoPhaseAwareParticipantElement> getTwoPhaseAware() {
        return twoPhaseAware;
    }
    @XmlElement
    public List<TwoPhaseUnawareParticipantElement> getTwoPhaseUnaware() {
        return twoPhaseUnaware;
    }
    @XmlElement
    public List<String> getVolatileParticipants() {
        return volatileParticipants;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setTxnURI(String txnURI) {
        this.txnURI = txnURI;
    }

    public void setStatus(TransactionStatusElement status) {
        this.status = status;
    }

    public void setTerminateURI(String terminatorURI) {
        this.terminatorURI = terminatorURI;
    }

    public void setDurableParticipantEnlistmentURI(String durableParticipantEnlistmentURI) {
        this.durableParticipantEnlistmentURI = durableParticipantEnlistmentURI;
    }

    public void setVolatileParticipantEnlistmentURI(String volatileParticipantEnlistmentURI) {
        this.volatileParticipantEnlistmentURI = volatileParticipantEnlistmentURI;
    }

    public void addTwoPhaseAware(TwoPhaseAwareParticipantElement participantElement) {
        twoPhaseAware.add(participantElement);
    }

    public void addTwoPhaseUnaware(TwoPhaseUnawareParticipantElement participantElement) {
        twoPhaseUnaware.add(participantElement);
    }

    public void addVolatileParticipant(String volatileParticipant) {
        volatileParticipants.add(volatileParticipant);
    }
}
