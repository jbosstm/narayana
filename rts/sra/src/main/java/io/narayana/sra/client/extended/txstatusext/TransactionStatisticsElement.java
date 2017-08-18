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
