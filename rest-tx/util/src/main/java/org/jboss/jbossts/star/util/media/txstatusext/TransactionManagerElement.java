package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//@XmlSchema(xmlns = {}, namespace = "org.reststart...")
@XmlRootElement(name = "transaction-manager")
@XmlType(propOrder = {"created", "statistics", "coordinatorURIs", "coordinators"})
public class TransactionManagerElement {
    private Date created;
    private TransactionStatisticsElement statistics;
    private List <String> coordinatorURIs = new ArrayList<String>();
    private List<CoordinatorElement> coordinators = new ArrayList<CoordinatorElement>();

    @XmlElement
    public Date getCreated() {
        return created;
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
        this.created = created;
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
