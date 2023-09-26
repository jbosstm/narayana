/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client.extended.txstatusext;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "transaction-manager")
@XmlType(propOrder = {"created", "statistics", "coordinatorURIs", "coordinators"})
public class TransactionManagerElement {
    private Date created;
    private TransactionStatisticsElement statistics;
    private List <String> coordinatorURIs = new ArrayList<String>();
    private List<CoordinatorElement> coordinators = new ArrayList<CoordinatorElement>();

    @XmlElement
    public Date getCreated() {
        return new Date(created.getTime());
    }

    @XmlElement
    public TransactionStatisticsElement getStatistics() {
        return statistics;
    }

    @XmlElement
    public List<String> getCoordinatorURIs() {
        return coordinatorURIs;
    }

    @XmlElement
    public List<CoordinatorElement> getCoordinators() {
        return coordinators;
    }

    public void setCreated(Date created) {
        this.created = new Date(created.getTime());
    }

    public void setStatistics(TransactionStatisticsElement statistics) {
        this.statistics = statistics;
    }

    public void addCoordinator(CoordinatorElement coordinatorElement) {
        coordinators.add(coordinatorElement);
    }

    public void addCoordinator(String coordinatorURI) {
        coordinatorURIs.add(coordinatorURI);
    }
}