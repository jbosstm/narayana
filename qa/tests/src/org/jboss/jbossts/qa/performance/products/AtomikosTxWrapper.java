/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2008
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.performance.products;

import javax.transaction.Transaction;
import javax.transaction.SystemException;

/**
 * To include this class in the Performance Test Suite (ie class PerformanceTestWrapper) simply include
 * it as an argument the JVM:
 *     -DproductClass=com.hp.mwtests.performance.products.AtomikosTxWrapper
 * 
 * Before running this wrapper remember to:
 * - uncomment the lines marked with TODO;
 * - include the product jars on the classpath
 * - recompile (eg add them to jts.classpath qa/tests/build-jts.xml)
 * - put javax.transaction jar on the classpath
 *          (http://mvnrepository.com/artifact/geronimo-spec/geronimo-spec-jta)
 * - set the location of the product properties file:
 *   -Dcom.atomikos.icatch.file=<path to atomikos property file>
 */
public class AtomikosTxWrapper extends BaseWrapper implements TxWrapper
{

    public TxWrapper createWrapper()
    {
        return new AtomikosTxWrapper();
    }

    public int begin()
    {
        throw new RuntimeException("Not supported - please uncomment Atomikos dependencies in class AtomikosTxWrapper");
//        setUserTransaction(new com.atomikos.icatch.jta.UserTransactionImp()); // TODO uncomment this line

//        return super.begin(); // TODO uncomment this line
    }

    public Transaction getTransaction() throws SystemException
    {
        throw new RuntimeException("Not supported - please uncomment Atomikos dependencies in class AtomikosTxWrapper");
//        return com.atomikos.icatch.jta.TransactionManagerImp.getTransactionManager().getTransaction(); // TODO uncomment this line
    }

    public boolean supportsNestedTx()
    {
        return true;
    }

    public String getName()
    {
        return "Atomikos";
    }
}
