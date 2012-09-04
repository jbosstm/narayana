package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"terminatorURI"})
public class TwoPhaseAwareParticipantElement extends ParticipantElement {
    private String terminatorURI;

    @XmlElement
    public String getTerminatorURI() {
        return terminatorURI;
    }

    public void setTerminatorURI(String terminatorURI) {
        this.terminatorURI = terminatorURI;
    }
}
