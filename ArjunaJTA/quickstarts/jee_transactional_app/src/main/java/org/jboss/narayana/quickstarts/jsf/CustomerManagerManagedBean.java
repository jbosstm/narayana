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
package org.jboss.narayana.quickstarts.jsf;

import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.narayana.quickstarts.ejb.CustomerManagerEJB;
import org.jboss.narayana.quickstarts.jpa.Customer;

@Named("customerManager")
@RequestScoped
public class CustomerManagerManagedBean implements CustomerManager {
	private Logger logger = Logger.getLogger(CustomerManagerManagedBean.class
			.getName());

	@Inject
	private CustomerManagerEJB customerManagerEJB;

	@Inject
	private UserTransaction userTransaction;

	public List<Customer> getCustomers() throws SecurityException,
			IllegalStateException, NamingException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		logger.debug("Getting customers");
		return customerManagerEJB.listCustomers();
	}

	public String addCustomer(String name) {
		logger.debug("Adding customer: " + name);
		try {
			userTransaction.begin();
			logger.debug("Creating customer");
			customerManagerEJB.createCustomer(name);
			userTransaction.commit();
			logger.debug("Created customer");
			return "customerAdded";
		} catch (Exception e) {
			logger.debug("Caught a duplicate", e);
			// Transaction will be marked rollback only anyway utx.rollback();
			return "customerDuplicate";
		}
	}

	public int getCustomerCount() throws Exception {
		logger.debug("Getting count");
		int count = customerManagerEJB.getCustomerCount();
		return count;
	}
}
