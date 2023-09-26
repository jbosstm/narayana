/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client.extended.txstatusext;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.Date;

@XmlType(propOrder = { "created", "status", "recoveryURI", "resourceURI"})
public class ParticipantElement {
    private Date created;
    private TransactionStatusElement status;
    private String recoveryURI;
    private String resourceURI;

    @XmlElement
    public Date getCreated() {
        return new Date(created.getTime());
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
        this.created = new Date(created.getTime());
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