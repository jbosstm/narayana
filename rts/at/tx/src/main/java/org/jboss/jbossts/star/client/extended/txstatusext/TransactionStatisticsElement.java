/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.client.extended.txstatusext;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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