package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

@XmlType(propOrder = { "created", "status", "recoveryURI", "resourceURI"})
public class ParticipantElement {
    Date created;
    TransactionStatusElement status;
    String recoveryURI;
    String resourceURI;

    @XmlElement
    public Date getCreated() {
        return created;
    }

    @XmlElement
    public TransactionStatusElement getStatus() {
        return status;
    }

    @XmlElement
    public String getRecoveryURI() {
        return recoveryURI;
    }

    @XmlElement
    public String getResourceURI() {
        return resourceURI;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setStatus(TransactionStatusElement status) {
        this.status = status;
    }

    public void setRecoveryURI(String recoveryURI) {
        this.recoveryURI = recoveryURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }
}
