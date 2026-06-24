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

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.narayana.quickstarts.jpa.Customer;

/**
 * This is the customer manager as seen by the JSF pages. It defines the basic
 * operations required to add and list customers.
 */
public interface CustomerManager {

	/**
	 * Get the list of current customers.
	 * 
	 * @return
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws NamingException
	 * @throws NotSupportedException
	 * @throws SystemException
	 * @throws RollbackException
	 * @throws HeuristicMixedException
	 * @throws HeuristicRollbackException
	 */
	public List<Customer> getCustomers() throws SecurityException,
			IllegalStateException, NamingException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException;

	/**
	 * Add a customer to the database.
	 * 
	 * @param name
	 * @return
	 */
	public String addCustomer(String name);

	/**
	 * Get the total count of customers.
	 * 
	 * @return
	 * @throws Exception
	 */
	public int getCustomerCount() throws Exception;
}