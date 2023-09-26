/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client.extended.txstatusext;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

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