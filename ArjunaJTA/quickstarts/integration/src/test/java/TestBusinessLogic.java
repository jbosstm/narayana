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

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.ejb.Customer;
import org.jboss.narayana.quickstarts.ejb.SimpleEJB;
import org.jboss.narayana.quickstarts.ejb.SimpleEJBImpl;
import org.jboss.narayana.quickstarts.servlet.SimpleServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBusinessLogic {
	@EJB(lookup = "java:module/SimpleEJBImpl")
	private SimpleEJB simpleEJB;

	@Deployment
	public static WebArchive createDeployment() {
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(SimpleEJB.class, SimpleEJBImpl.class,
						Customer.class)
				.addAsResource("META-INF/persistence.xml",
						"META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	public void checkThatDoubleCallIncreasesListSize() throws NamingException,
			NotSupportedException, SystemException, SecurityException,
			IllegalStateException, RollbackException, HeuristicMixedException,
			HeuristicRollbackException {
		UserTransaction tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		tx.begin();
		simpleEJB.createCustomer("tom");
		tx.commit();

		String firstList = simpleEJB.listIds();

		tx = (UserTransaction) new InitialContext()
				.lookup("java:comp/UserTransaction");
		tx.begin();
		simpleEJB.createCustomer("tom");
		tx.commit();

		String secondList = simpleEJB.listIds();

		System.out.println(firstList);
		System.out.println(secondList);

		assertTrue(firstList.length() < secondList.length());
	}
}
