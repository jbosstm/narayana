package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
