package org.jboss.jbossts.star.util.media.txstatusext;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "statistics")
@XmlType(propOrder = { "active", "prepared", "committed", "rolledback"})
public class TransactionStatisticsElement {
    private int active;
    private int prepared;
    private int committed;
    private int rolledback;

    public TransactionStatisticsElement() {
    }

    public TransactionStatisticsElement(int active, int prepared, int committed, int rolledback) {
        this.active = active;
        this.prepared = prepared;
        this.committed = committed;
        this.rolledback = rolledback;
    }

    @XmlElement
    public int getActive() {
        return active;
    }

    @XmlElement
    public int getPrepared() {
        return prepared;
    }

    @XmlElement
    public int getCommitted() {
        return committed;
    }

    @XmlElement
    public int getRolledback() {
        return rolledback;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public void setPrepared(int prepared) {
        this.prepared = prepared;
    }

    public void setCommitted(int committed) {
        this.committed = committed;
    }

    public void setRolledback(int rolledback) {
        this.rolledback = rolledback;
    }
}
