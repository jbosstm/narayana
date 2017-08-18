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
import javax.xml.bind.annotation.XmlType;
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
