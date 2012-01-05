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
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.ejb.CustomerManagerEJB;
import org.jboss.narayana.quickstarts.ejb.CustomerManagerEJBImpl;
import org.jboss.narayana.quickstarts.jpa.Customer;
import org.jboss.narayana.quickstarts.jsf.CustomerManager;
import org.jboss.narayana.quickstarts.jsf.CustomerManagerManagedBean;
import org.jboss.narayana.quickstarts.txoj.CustomerCreationCounter;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestManagedBeanCustomerManager {
	@Inject
	private CustomerManagerManagedBean managedBeanCustomerManager;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(CustomerManagerEJB.class,
						CustomerManagerEJBImpl.class, Customer.class)
				.addClasses(CustomerCreationCounter.class)
				.addClasses(CustomerManager.class,
						CustomerManagerManagedBean.class)
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		// Need to make sure we add the arquillian-service and msc as a
		// dependency
		final String ManifestMF = "Manifest-Version: 1.0\n"
				+ "Dependencies: org.jboss.modules,deployment.arquillian-service,org.jboss.msc,org.jboss.jts\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}

	@Test
	public void checkListCustomers() throws Exception {

		// Create a customer
		managedBeanCustomerManager.addCustomer("Test"
				+ System.currentTimeMillis());
		List<Customer> firstList = managedBeanCustomerManager.getCustomers();
		// Create a different customer
		managedBeanCustomerManager.addCustomer("Test"
				+ System.currentTimeMillis());
		List<Customer> secondList = managedBeanCustomerManager.getCustomers();

		// Check that the list size increased
		assertTrue(firstList.size() < secondList.size());
	}

	@Test
	public void checkCustomerCount() throws Exception {
		int response = -1;
		int size = managedBeanCustomerManager.getCustomerCount();

		// Create a new customer
		managedBeanCustomerManager.addCustomer("Test"
				+ System.currentTimeMillis());

		// Get the initial number of customers
		response = managedBeanCustomerManager.getCustomerCount();
		assertTrue("" + response, response == size + 1);
		size = response;

		// Create a new customer
		managedBeanCustomerManager.addCustomer("Test"
				+ System.currentTimeMillis());

		// Check that one extra customer was created
		response = managedBeanCustomerManager.getCustomerCount();
		assertTrue("" + response, response == size + 1);
		size = response;
	}

	@Test
	public void testCustomerCountInPresenceOfRollback() throws Exception {
		int response = -1;
		int size = managedBeanCustomerManager.getCustomerCount();

		String firstCustomerName = "Test" + System.currentTimeMillis();
		// Create a new customer
		managedBeanCustomerManager.addCustomer(firstCustomerName);

		// Get the initial number of customers
		response = managedBeanCustomerManager.getCustomerCount();
		assertTrue("" + response, response == size + 1);
		size = response;

		// Create a new customer
		managedBeanCustomerManager.addCustomer(firstCustomerName);

		// Check that no extra customers were created
		response = managedBeanCustomerManager.getCustomerCount();
		assertTrue("" + response, response == size);
		size = response;

		// Create a new customer
		managedBeanCustomerManager.addCustomer("Test"
				+ System.currentTimeMillis());

		// Check that one extra customer was created
		response = managedBeanCustomerManager.getCustomerCount();
		assertTrue("" + response, response == size + 1);
		size = response;
	}
}