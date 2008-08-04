package org.jboss.jbossts.qa.astests.taskdefs;

import java.io.File;
import java.io.IOException;

public class Utils
{
    public static File toFile(String fileName)
    {
        try
        {
            return new File(fileName).getCanonicalFile();
        }
        catch (IOException e)
        {
            return new File(fileName).getAbsoluteFile();
        }
    }

}
