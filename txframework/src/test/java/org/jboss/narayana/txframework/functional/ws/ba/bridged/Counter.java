/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.narayana.txframework.functional.ws.ba.bridged;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;

/**
 * EJB3 Entity Bean implementation of the business app state.
 *
 * @author paul.robinson@redhat.com
 */
@Entity
public class Counter implements Serializable {

    private int id;
    private int bookingCount;
    private boolean confirmed;

    public Counter() {

    }

    public Counter(int id, int initialCounterValue) {

        this.id = id;
        this.bookingCount = initialCounterValue;
        this.confirmed = false;
    }

    @Id
    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getCounter() {

        return bookingCount;
    }

    public void setCounter(int counter) {

        this.bookingCount = counter;
    }

    public void incrementCounter(int howMany) {

        setCounter(getCounter() + howMany);
    }

    public void decrementCounter(int howMany) {

        setCounter(getCounter() - howMany);
    }

    public void setConfirmed(boolean confirmed) {

        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {

        return confirmed;
    }
}
