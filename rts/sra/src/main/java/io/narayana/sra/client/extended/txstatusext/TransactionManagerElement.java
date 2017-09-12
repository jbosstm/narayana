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
