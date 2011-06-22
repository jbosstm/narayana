package org.jboss.narayana.examples.basic;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.io.File;

public class Util {
    public static void emptyObjectStore() {
        String objectStoreDirName = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreDir();

        if (objectStoreDirName != null)
            Util.removeContents(new File(objectStoreDirName));
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
