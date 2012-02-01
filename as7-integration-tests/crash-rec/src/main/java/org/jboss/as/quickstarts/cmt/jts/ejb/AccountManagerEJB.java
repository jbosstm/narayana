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
package org.jboss.as.quickstarts.cmt.jts.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;

import org.jboss.as.quickstarts.cmt.jts.model.Customer;
import org.jboss.as.quickstarts.cmt.jts.model.Invoice;

@Stateless
public class AccountManagerEJB {
    @EJB
    private CustomerManagerEJB customerManager;

//    @EJB(lookup = "corbaname:iiop:localhost:3628#invoiceManager/InvoiceManagerEJBImpl")
    @EJB(lookup = "corbaname:iiop:localhost:3528#customerManager/InvoiceManagerEJBImpl")
    private InvoiceManagerEJBHome invoiceManagerHome;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void createCustomerAndInvoice(String name) throws RemoteException, JMSException {
        customerManager.createCustomer(name);

        final InvoiceManagerEJB invoiceManager = invoiceManagerHome.create();
        invoiceManager.createInvoice(name);
    }

    public List<Customer> listCustomers() {
        return customerManager.listCustomers();
    }

    public List<Invoice> listInvoices() throws RemoteException {
        final InvoiceManagerEJB invoiceManager = invoiceManagerHome.create();
        return invoiceManager.listInvoices();
    }

}
