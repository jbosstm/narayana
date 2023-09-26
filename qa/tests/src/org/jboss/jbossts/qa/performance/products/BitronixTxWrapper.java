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
 *     -DproductClass=com.hp.mwtests.performance.products.BitronixTxWrapper
 *
 * Before running this wrapper remember to:
 * - uncomment the lines marked with TODO;
 * - include the product jars on the classpath;
 * - recompile (eg add them to jts.classpath qa/tests/build.xml)
 * - put jakarta.transaction jar on the classpath
 *       (http://mvnrepository.com/artifact/geronimo-spec/geronimo-spec-jta)
 * - set the location of the product properties file:
 *   -Dbitronix.tm.configuration=<path to bitronix property file>
 *  JVMARGS="-Dbitronix.tm.configuration=$RESDIR/btm.properties -Dcom.atomikos.icatch.file=$RESDIR/atomikos.properties
 */
public class BitronixTxWrapper extends BaseWrapper implements TxWrapper
{
    public BitronixTxWrapper()
    {
    }

    public TxWrapper createWrapper()
    {
        return new BitronixTxWrapper();
    }

    public Transaction getTransaction() throws SystemException
    {
        throw new RuntimeException("Not supported - please uncomment Bitronix dependencies in class BitronixTxWrapper");
//        return bitronix.tm.TransactionManagerServices.getTransactionManager().getCurrentTransaction(); //TODO
    }

    public int begin()
    {
          throw new RuntimeException("Not supported - please uncomment Bitronix dependencies in class BitronixTxWrapper");
//        setUserTransaction(bitronix.tm.TransactionManagerServices.getTransactionManager()); //TODO

//        return super.begin(); //TODO
    }

    public boolean supportsNestedTx()
    {
        return false;
    }

    public String getName()
    {
        return "Bitronix";
    }
}