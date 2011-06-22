package org.jboss.narayana.examples.ejb;

import javax.naming.NamingException;

public interface SimpleEjbLocal {
	public String getStatus() throws NamingException;

	public String getStatus2() throws NamingException;

	public String createCustomerAndListIds() throws NamingException;
}
