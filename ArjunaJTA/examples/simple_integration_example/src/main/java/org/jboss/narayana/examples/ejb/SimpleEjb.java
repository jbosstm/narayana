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
package org.jboss.narayana.examples.ejb;

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
public class SimpleEjb implements SimpleEjbLocal {
	@PersistenceContext(name = "my_persistence_ctx")
	EntityManager em;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String getStatus() throws NamingException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		return "tom transactionally hacked this up: " + tx.toString();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String getStatus2() throws NamingException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		return "tom transactionally hacked this off: " + tx.toString();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public String createCustomerAndListIds() throws NamingException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		Customer c1 = new Customer();
		c1.setName("XYZ");
		em.persist(c1);

		final List<Customer> list = em.createQuery("select c from Customer c")
				.getResultList();
		StringBuffer toReturn = new StringBuffer("customers (in tx "
				+ tx.toString() + ": ");
		boolean added = false;
		for (Customer customer : list) {
			int id = customer.getId();
			toReturn.append(id);
			toReturn.append(", ");
			added = true;
		}
		if (added) {
			toReturn.delete(toReturn.length() - 3, toReturn.length() - 1);
		}
		return toReturn.toString();
	}
}
