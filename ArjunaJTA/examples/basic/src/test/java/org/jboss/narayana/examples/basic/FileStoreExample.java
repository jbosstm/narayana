package org.jboss.narayana.examples.basic;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

public class FileStoreExample extends TransactionExample {
    private static String storeDir = "target/TxStoreDir";

     public static void main(String[] args) throws Exception {
        setupStore();
        TransactionExample.main(new String[0]);
        Assert.assertTrue(new File(storeDir).exists());
    }

    @BeforeClass
    public static void setupStore() throws Exception {
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreDir(storeDir);
        Util.emptyObjectStore();
    }
}
