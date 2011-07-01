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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.ejb.Customer;
import org.jboss.narayana.quickstarts.ejb.SimpleEJB;
import org.jboss.narayana.quickstarts.ejb.SimpleEJBImpl;
import org.jboss.narayana.quickstarts.servlet.SimpleServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBusinessLogic {
	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap
				.create(JavaArchive.class, "test.jar")
				.addClasses(SimpleEJB.class, SimpleEJBImpl.class,
						Customer.class)
				.addClasses(SimpleServlet.class)
				.addAsManifestResource("META-INF/persistence.xml",
						"META-INF/persistence.xml");
	}

	@Test
	public void checkThatDoubleCallIncreasesListSize() {
		SimpleServlet simpleServlet = new SimpleServlet();
		simpleServlet.createCustomer("tom");
		simpleServlet.createCustomer("tom");
	}

}
