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

import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Compensate;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Completes;
import org.jboss.narayana.txframework.api.annotation.service.ServiceRequest;
import org.jboss.narayana.txframework.api.annotation.transaction.Compensatable;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 * @author Paul Robinson (paul.robinson@redhat.com)
 */
@Stateless
@WebService(serviceName = "BABridgedService", portName = "BABridgedService",
        name = "BABridged", targetNamespace = "http://www.jboss.com/functional/ba/bridged/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Compensatable(completionType = CompletionType.PARTICIPANT)
public class BABridgedService implements BABridged {

    private static final int ENTITY_ID = 1;

    @PersistenceContext
    protected EntityManager em;

    @Inject
    private TXDataMap<String, Integer> txDataMap;

    @WebMethod
    @ServiceRequest
    @Completes
    public void incrementCounter(Integer how_many) throws SomeApplicationException {

        txDataMap.put("how_many", how_many);
        Counter counter = getSimpleEntity();
        counter.incrementCounter(how_many);
        em.merge(counter);
    }

    @WebMethod
    public int getCounter() {

        Counter counter = getSimpleEntity();
        if (counter == null) {
            return -1;
        }
        return counter.getCounter();
    }

    @WebMethod
    public boolean isConfirmed() {

        Counter counter = getSimpleEntity();
        if (counter == null) {
            return false;
        }
        return counter.isConfirmed();
    }

    @WebMethod
    public void reset() {

        Counter counter = getSimpleEntity();
        counter.setCounter(0);
        counter.setConfirmed(false);
        em.merge(counter);
    }


    @Compensate
    @WebMethod(exclude = true)
    public void compensate() {

        Integer how_many = txDataMap.get("how_many");
        Counter counter = getSimpleEntity();
        counter.decrementCounter(how_many);
        em.merge(counter);
    }

    @Close
    @WebMethod(exclude = true)
    public void close() {
        Counter counter = getSimpleEntity();
        counter.setConfirmed(true);
        em.merge(counter);
    }

    private Counter getSimpleEntity() {

        Counter counter = em.find(Counter.class, ENTITY_ID);
        if (counter == null) {
            counter = new Counter(ENTITY_ID, 0);
            em.persist(counter);
        }

        return counter;
    }
}
