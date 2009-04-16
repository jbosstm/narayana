/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 *
 * (C) 2007, 2009 @author JBoss Inc
 */
package org.jboss.jbossts.txbridge.demo.bistro;

import org.apache.log4j.Logger;

import javax.ejb.*;
import javax.persistence.*;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.HandlerChain;
import javax.jws.soap.SOAPBinding;

/**
 * A Bistro implementation that is exposed as a Web Service using JSR-181 annotations
 * and uses an EJB3 Entity Bean (i.e. JPA) backend.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
@Stateless
@Remote(Bistro.class)
@WebService()
@SOAPBinding(style = SOAPBinding.Style.RPC)
@HandlerChain(file = "jaxws-handlers-server.xml") // relative path from the class file
@TransactionAttribute(TransactionAttributeType.MANDATORY) // default is REQUIRED
public class BistroImpl implements Bistro
{
	private static Logger log = Logger.getLogger(BistroImpl.class);

	private static final int BISTRO_ID = 1;

	@PersistenceContext
	protected EntityManager em;

	@WebMethod
	public void bookSeats(int how_many)
    {
		log.debug("bookSeats(how_many="+how_many+")");

		BistroEntityImpl entity = getBistroEntity();

		entity.increaseBookingCount(how_many);
	}

	@WebMethod
	public int getBookingCount()
    {
		log.debug("getBookedSeatCount()");

		return getBistroEntity().getBookingCount();
	}

	private BistroEntityImpl getBistroEntity()
    {
		BistroEntityImpl entity = em.find(BistroEntityImpl.class, BISTRO_ID);
		if(entity == null) {
			entity = new BistroEntityImpl();
			em.persist(entity);
		}

		return entity;
	}
}
