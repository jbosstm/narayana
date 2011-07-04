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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.ejb.Customer;
import org.jboss.narayana.quickstarts.ejb.SimpleEJB;
import org.jboss.narayana.quickstarts.ejb.SimpleEJBImpl;
import org.jboss.narayana.quickstarts.servlet.SimpleServlet;
import org.jboss.narayana.quickstarts.txoj.AtomicObject;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.Uid;

@RunWith(Arquillian.class)
public class TestSimpleServlet {
	@EJB(lookup = "java:module/SimpleEJBImpl")
	private SimpleEJB simpleEJB;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(SimpleEJB.class, SimpleEJBImpl.class,
						Customer.class)
				.addClasses(AtomicObject.class)
				.addClasses(SimpleServlet.class)
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
	public void checkThatDoubleCallIncreasesListSize()
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {

		// Declare the servlet outside the container
		SimpleServlet servlet = new SimpleServlet();
		Field field = servlet.getClass().getDeclaredField("simpleEJB");
		field.setAccessible(true);
		field.set(servlet, simpleEJB);
		field.setAccessible(false);

		servlet.createCustomer("tom");

		String firstList = servlet.listCustomers();

		servlet.createCustomer("tom");

		String secondList = servlet.listCustomers();

		System.out.println(firstList);
		System.out.println(secondList);

		assertTrue(firstList.length() < secondList.length());
	}

	@Test
	public void testTxoj() throws Exception {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");

		AtomicObject foo = new AtomicObject();
		Uid u = foo.get_uid();

		tx.begin();

		foo.set(2);

		tx.commit();

		int finalVal = foo.get();

		assertEquals(2, finalVal);

		foo = new AtomicObject(u);

		tx.begin();

		foo.set(4);

		tx.commit();

		finalVal = foo.get();

		assertEquals(4, finalVal);

		foo = new AtomicObject(u);

		finalVal = foo.get();

		assertEquals(4, finalVal);

		tx.begin();

		foo.set(10);

		tx.rollback();

		finalVal = foo.get();

		assertEquals(4, finalVal);
	}

	@Test
	public void testServlet() throws Exception {
		// Declare the servlet outside the container
		SimpleServlet servlet = new SimpleServlet();
		Field field = servlet.getClass().getDeclaredField("simpleEJB");
		field.setAccessible(true);
		field.set(servlet, simpleEJB);
		field.setAccessible(false);

		String toLookFor = "<p>Customers created this run: ";
		String response = null;
		int indexOf = -1;
		String customersCreated = null;

		// Get the initial number of customers
		response = servlet.getCustomerCount();
		indexOf = response.indexOf(toLookFor);
		customersCreated = response.substring(indexOf + toLookFor.length(),
				response.indexOf("</p>", indexOf));
		int initialSize = Integer.parseInt(customersCreated);

		// Create a new customer
		servlet.createCustomer("tom");

		// Check that one extra customer was created
		response = servlet.getCustomerCount();
		indexOf = response.indexOf(toLookFor);
		customersCreated = response.substring(indexOf + toLookFor.length(),
				response.indexOf("</p>", indexOf));
		int newSize = Integer.parseInt(customersCreated);
		assertTrue(newSize == initialSize + 1);
	}
}