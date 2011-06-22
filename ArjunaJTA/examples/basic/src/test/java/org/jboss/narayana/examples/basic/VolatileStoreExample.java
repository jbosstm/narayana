package org.jboss.narayana.examples.basic;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

public class VolatileStoreExample extends TransactionExample {
    private static final String storeClassName = com.arjuna.ats.internal.arjuna.objectstore.VolatileStore.class.getName();
    private static String defaultStoreDir;

    public static void main(String[] args) throws Exception {
        setupStore();
        TransactionExample.main(new String[0]);
        checkSuccess();
    }

    @BeforeClass
    public static void setupStore() throws Exception {
        Util.emptyObjectStore();
        defaultStoreDir = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "default").setObjectStoreType(storeClassName);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeClassName);
    }

    @AfterClass
    public static void checkSuccess() throws Exception {
        File f = new File(defaultStoreDir);
        Assert.assertTrue(!f.exists());
    }
}