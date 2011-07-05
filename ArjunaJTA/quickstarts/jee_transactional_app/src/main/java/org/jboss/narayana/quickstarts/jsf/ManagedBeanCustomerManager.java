package org.jboss.narayana.quickstarts.jsf;

import java.util.List;

import javax.ejb.EJB;
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

import org.jboss.narayana.quickstarts.ejb.Customer;
import org.jboss.narayana.quickstarts.ejb.SimpleEJB;

@Named("customerManager")
@RequestScoped
public class ManagedBeanCustomerManager implements CustomerManager {

	@EJB
	private SimpleEJB simpleEJB;

	@Inject
	private UserTransaction utx;

	public List<Customer> getCustomers() throws SecurityException,
			IllegalStateException, NamingException, NotSupportedException,
			SystemException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		System.out.println("Getting customers");
		return simpleEJB.listCustomers();
	}

	public String addCustomer(String name) {
		System.out.println("Adding customer: " + name);
		try {
			utx.begin();
			System.out.println("Creating customer");
			simpleEJB.createCustomer(name);
			utx.commit();
			System.out.println("Created customer");
			return "customerAdded";
		} catch (Exception e) {
			e.printStackTrace();
			// Transaction will be marked rollback only anyway utx.rollback();
			return "customerDuplicate";
		}
	}

	public int getCustomerCount() throws Exception {
		System.out.println("Getting count");
		int count = simpleEJB.getCustomerCount();
		return count;
	}
}
