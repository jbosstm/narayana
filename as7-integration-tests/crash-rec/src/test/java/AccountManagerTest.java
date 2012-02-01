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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.quickstarts.cmt.jts.controller.AccountManager;
import org.jboss.as.quickstarts.cmt.jts.ejb.AccountManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJB;
import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBHome;
import org.jboss.as.quickstarts.cmt.jts.ejb.InvoiceManagerEJBImpl;
import org.jboss.as.quickstarts.cmt.jts.model.Customer;
import org.jboss.as.quickstarts.cmt.jts.model.Invoice;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AccountManagerTest {
    @Inject
    private AccountManager accountManager;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:/queue/test")
    private Queue queue;

    @Deployment
    public static Archive<?> createCustomerManagerArchive() {
        return ShrinkWrap
                .create(WebArchive.class, "customerManager.war")
                .addClasses(InvoiceManagerEJBHome.class, InvoiceManagerEJB.class, InvoiceManagerEJBImpl.class, Invoice.class,
                        AccountManagerEJB.class, CustomerManagerEJB.class, AccountManager.class, Customer.class)
                .addAsResource("META-INF/persistence-customerManager.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void crashServer() throws Exception {

        String newCustomerName = "Test" + System.currentTimeMillis();

        accountManager.invoiceNewCustomer(newCustomerName);
        // This should crash
        fail("Should have crashed");
    }

    @Test
    public void retrieveJMSMessages() throws Exception {
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        connection.start();
        assertNotNull(messageConsumer.receive(1000));
        assertNotNull(messageConsumer.receive(1000));
    }

    @Test
    public void retrieveSavedState() throws Exception {
        assertTrue(accountManager.listCustomers().size() > 0);
        assertTrue(accountManager.listInvoices().size() > 0);
    }
}