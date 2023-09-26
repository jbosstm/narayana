/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.products;

import jakarta.transaction.Transaction;
import jakarta.transaction.SystemException;

/**
 * To include this class in the Performance Test Suite (ie class PerformanceTestWrapper) simply include
 * it as an argument the JVM:
 *     -DproductClass=com.hp.mwtests.performance.products.AtomikosTxWrapper
 * 
 * Before running this wrapper remember to:
 * - uncomment the lines marked with TODO;
 * - include the product jars on the classpath
 * - recompile (eg add them to jts.classpath qa/tests/build.xml)
 * - put jakarta.transaction jar on the classpath
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