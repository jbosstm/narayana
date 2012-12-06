package org.jboss.narayana.txframework.impl;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.api.exception.TransactionDataUnavailableException;
import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author paul.robinson@redhat.com 01/11/2012
 */
@RunWith(Arquillian.class)
public class DataManagementTest {

    @Inject
    TXDataMap<String, Integer> txDataMap;

    @Deployment()
    public static JavaArchive createTestArchive() {

        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.jboss.narayana.txframework.api")
                .addClasses(TXDataMap.class, TXDataMapImpl.class, DataManagementTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test(expected = TransactionDataUnavailableException.class)
    public void testNoData() throws Exception {
        Assert.assertNotNull(txDataMap);
        txDataMap.put("one", 1);
    }

    @Test
    public void testSuspendResume() throws Exception {
        Map map = new HashMap();

        assertTransactionDataUnavailable(txDataMap);

        TXDataMapImpl.resume(map);
        txDataMap.put("one", 1);
        Assert.assertTrue(txDataMap.get("one") == 1);
        TXDataMapImpl.suspend();

        assertTransactionDataUnavailable(txDataMap);

        TXDataMapImpl.resume(map);
        txDataMap.put("two", 2);
        Assert.assertTrue(txDataMap.get("one") == 1);
        Assert.assertTrue(txDataMap.get("two") == 2);
        TXDataMapImpl.suspend();

        assertTransactionDataUnavailable(txDataMap);
    }


    @Test
    public void testMultiSuspendResume() throws Exception {
        Map map1 = new HashMap();
        Map map2 = new HashMap();

        assertTransactionDataUnavailable(txDataMap);

        TXDataMapImpl.resume(map1);
        txDataMap.put("one", 1);
        Assert.assertTrue(txDataMap.get("one") == 1);
        TXDataMapImpl.suspend();

        assertTransactionDataUnavailable(txDataMap);

        TXDataMapImpl.resume(map2);
        txDataMap.put("two", 2);
        Assert.assertTrue(txDataMap.get("one") == null);
        Assert.assertTrue(txDataMap.get("two") == 2);
        TXDataMapImpl.suspend();

        assertTransactionDataUnavailable(txDataMap);
    }

    private void assertTransactionDataUnavailable(TXDataMap txDataMap) {
        try {
            txDataMap.isEmpty();
            Assert.fail();
        }
        catch (TransactionDataUnavailableException e) {
            //do nothing, this is expected
        }
    }
}
