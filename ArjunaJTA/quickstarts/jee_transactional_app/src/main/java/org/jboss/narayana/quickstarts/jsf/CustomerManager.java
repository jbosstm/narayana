package org.jboss.narayana.quickstarts.jsf;

import java.util.List;

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.narayana.quickstarts.ejb.Customer;

public interface CustomerManager {

	public List<Customer> getCustomers() throws SecurityException, IllegalStateException, NamingException, NotSupportedException, SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException;

	public String addCustomer(String name);

	public int getCustomerCount() throws Exception;

}