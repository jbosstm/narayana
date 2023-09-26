/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client.extended.txstatusext;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"prepareURI", "commitURI", "rollbackURI"})
public class TwoPhaseUnawareParticipantElement extends ParticipantElement {
    private String prepareURI;
    private String commitURI;
    private String rollbackURI;

    @XmlElement
    public String getPrepareURI() {
        return prepareURI;
    }

    @XmlElement
    public String getCommitURI() {
        return commitURI;
    }

    @XmlElement
    public String getRollbackURI() {
        return rollbackURI;
    }

    public void setPrepareURI(String prepareURI) {
        this.prepareURI = prepareURI;
    }

    public void setCommitURI(String commitURI) {
        this.commitURI = commitURI;
    }

    public void setRollbackURI(String rollbackURI) {
        this.rollbackURI = rollbackURI;
    }
}