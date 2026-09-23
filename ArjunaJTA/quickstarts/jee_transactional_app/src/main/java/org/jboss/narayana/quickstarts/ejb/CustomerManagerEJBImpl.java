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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.logging.Logger;
import org.jboss.narayana.quickstarts.jpa.Customer;
import org.jboss.narayana.quickstarts.jsf.CustomerManagerManagedBean;
import org.jboss.narayana.quickstarts.txoj.CustomerCreationCounter;

@Stateless
public class CustomerManagerEJBImpl implements CustomerManagerEJB {
	private Logger logger = Logger.getLogger(CustomerManagerManagedBean.class
			.getName());

	private CustomerCreationCounter customerCreationCounter = new CustomerCreationCounter();

	@PersistenceContext(name = "my_persistence_ctx")
	EntityManager entityManager;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int createCustomer(String name) throws Exception {
		logger.debug("createCustomer transaction is identified as: "
				+ new InitialContext().lookup("java:comp/UserTransaction")
						.toString());

		// Can do this first because if there is a duplicate it will be rolled
		// back for us
		customerCreationCounter.incr(1);

		Customer c1 = new Customer();
		c1.setName(name);
		entityManager.persist(c1);

		return c1.getId();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@SuppressWarnings("unchecked")
	public List<Customer> listCustomers() throws NamingException,
			NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		logger.debug("listCustomers transaction is identified as: "
				+ new InitialContext().lookup("java:comp/UserTransaction")
						.toString());
		return entityManager.createQuery("select c from Customer c")
				.getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public int getCustomerCount() throws Exception {
		logger.debug("getCustomerCount transaction is identified as: "
				+ new InitialContext().lookup("java:comp/UserTransaction")
						.toString());
		return customerCreationCounter.get();
	}
}
