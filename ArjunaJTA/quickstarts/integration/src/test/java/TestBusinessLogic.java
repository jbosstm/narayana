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
public class TestBusinessLogic {
	@EJB(lookup = "java:module/SimpleEJBImpl")
	private SimpleEJB simpleEJB;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addClasses(SimpleEJB.class, SimpleEJBImpl.class,
						Customer.class)
				.addClasses(AtomicObject.class)
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

}