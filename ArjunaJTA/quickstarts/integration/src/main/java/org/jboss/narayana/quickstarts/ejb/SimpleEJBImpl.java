/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.quickstarts.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

@Stateless
public class SimpleEJBImpl implements SimpleEJB {
	@PersistenceContext(name = "my_persistence_ctx")
	EntityManager em;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int createCustomer(String name) throws NamingException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		System.out.println("createCustomer transaction is identified as: "
				+ tx.toString());
		Customer c1 = new Customer();
		c1.setName(name);
		em.persist(c1);

		return c1.getId();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String listIds() throws NamingException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		System.out.println("listIds transaction is identified as: "
				+ tx.toString());
		final List<Customer> list = em.createQuery("select c from Customer c")
				.getResultList();
		StringBuffer toReturn = new StringBuffer("customers: ");
		boolean added = false;
		for (Customer customer : list) {
			toReturn.append(customer.getId());
			toReturn.append("/");
			toReturn.append(customer.getName());
			toReturn.append(", ");
			added = true;
		}
		if (added) {
			return toReturn.substring(0, toReturn.length() - 2);
		} else {
			return toReturn.toString();
		}
	}
}
