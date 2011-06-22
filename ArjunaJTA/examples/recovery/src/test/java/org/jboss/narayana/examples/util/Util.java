package org.jboss.narayana.examples.util;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.File;

public class Util {
    public static final String dataDir = "target/data";
    public static final String recoveryStoreDir = dataDir + "/recoveryTestStore";
    public static final String hornetqStoreDir = dataDir + "/hornetq";

    public static void emptyObjectStore() {
        String objectStoreDirName = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        if (objectStoreDirName != null)
            Util.removeContents(new File(objectStoreDirName));
    }

    public static int countLogRecords() {
        String objectStoreDirName = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        if (objectStoreDirName != null) {
            File osDir = new File(objectStoreDirName);

            if (osDir.exists())
                return Util.countLogRecords(osDir, 0);
        }

        return 0;
    }

    public static int countLogRecords(File directory, int count)
    {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("..")))
        {
            File[] contents = directory.listFiles();

            for (File f : contents) {
                if (f.isDirectory()) {
                    count += countLogRecords(f, count);
                } else {
                    count += 1;
                }
            }
        }

        return count;
    }


    public static void removeContents(File directory)
    {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("..")))
        {
            File[] contents = directory.listFiles();

            for (File f : contents) {
                if (f.isDirectory()) {
                    removeContents(f);

                    f.delete();
                } else {
                    f.delete();
                }
            }
        }

        if (directory != null)
            directory.delete();
    }
}
