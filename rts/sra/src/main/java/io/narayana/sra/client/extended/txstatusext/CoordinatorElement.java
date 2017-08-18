/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
 */
package io.narayana.sra.client.extended.txstatusext;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        return new Date(created.getTime());
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
        this.created = new Date(created.getTime());
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
