/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.txframework.functional.ws.at.bridged;


import org.jboss.narayana.txframework.api.annotation.transaction.Transactional;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author paul.robinson@redhat.com
 */
@Stateless
@Remote(ATBridge.class)
@Transactional
@WebService(serviceName = "ATBridgeService", portName = "ATBridge",
        name = "ATBridge", targetNamespace = "http://www.jboss.com/functional/at/bridge")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class ATBridgeImpl implements ATBridge {

    private static final int ENTITY_ID = 1;

    @PersistenceContext
    protected EntityManager em;

    @WebMethod
    public void incrementCounter(int how_many) {

        SimpleEntity entity = getSimpleEntity();
        entity.incrementCounter(how_many);
        em.merge(entity);
    }

    @WebMethod
    public int getCounter() {

        SimpleEntity simpleEntity = getSimpleEntity();
        if (simpleEntity == null) {
            return -1;
        }
        return simpleEntity.getCounter();
    }

    @WebMethod
    public void reset() {

        SimpleEntity entity = getSimpleEntity();
        entity.setCounter(0);
        em.merge(entity);
    }

    private SimpleEntity getSimpleEntity() {

        SimpleEntity entity = em.find(SimpleEntity.class, ENTITY_ID);
        if (entity == null) {
            entity = new SimpleEntity(ENTITY_ID, 0);
            em.persist(entity);
        }

        return entity;
    }
}

