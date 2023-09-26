/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.common.util.propertyservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * The FileLocator class provides a common method for locating files.
 * If not passed an absolute filename (starting with the string "abs://"),
 * it searches for the file in the order:
 *   in the directory specified by the system property user.dir
 *   in the directory specified by the system property user.home
 *   in the directory specified by the system property java.home
 *   using the getResource() method
 *
 * @author Julian Coleman
 * @version $Id: FileLocator.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 3.0.
 */

class FileLocator
{
    /**
     * Locate the specific file.
     * Return the file path or uri (if a resource within an archive on the classpath) or throw FileNotFoundExcpetion.
     */
    static String locateFile (String findFile, ClassLoader classLoader) throws FileNotFoundException
    {
        URL url;
        String fullPathName;

        if (findFile == null)
            throw new FileNotFoundException("locateFile: null file name");

        String testAbsolutePath = new File(findFile).getAbsolutePath();
        if(testAbsolutePath.equals(findFile)) {
            return testAbsolutePath;
        }

        if (findFile.startsWith(absolutePath))
            return findFile.substring(absolutePath.length());

        if ((fullPathName = locateByProperty(findFile)) != null)
            return fullPathName;

        if ((url = locateByResource(findFile, classLoader)) != null)
            return url.toString(); // no special decode handling any more.

        throw new FileNotFoundException("locateFile: file not found: " + findFile);
    }

    /**
     * Search for a file using the properties: user.dir, user.home, java.home
     * Returns absolute path name or null.
     */
    private static synchronized String locateByProperty(String findFile)
    {
        String fullPathName = null;
        String dir = null;
        File f = null;

        if (findFile == null)
            return null;

        try
        {
            // System.err.println("Searching in user.dir for: " + findFile);

            dir = System.getProperty("user.dir");
            if (dir != null) {
                fullPathName = dir + File.separatorChar + findFile;
                f = new File(fullPathName);
            }
            if (f != null && f.exists())
            {
                // System.err.println("Found in user.dir");
                return fullPathName;
            }

            dir = System.getProperty("user.home");
            if (dir != null) {
                fullPathName = dir + File.separatorChar + findFile;
                f = new File(fullPathName);
            }
            if (f != null && f.exists())
            {
                // System.err.println("Found in user.home");
                return fullPathName;
            }

            dir = System.getProperty("java.home");
            if (dir != null) {
                fullPathName = dir + File.separatorChar + findFile;
                f = new File(fullPathName);
            }
            if (f != null && f.exists())
            {
                // System.err.println("Found in java.home");
                return fullPathName;
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

    /**
     * Search for a file using the properties: user.dir, user.home, java.home
     * Returns URL or null.
     */
    private static URL locateByResource(String findFile, ClassLoader classLoader)
    {
        URL url = classLoader.getResource(findFile);
        if (url == null)
        {
            url = FileLocator.class.getResource("/" + findFile);
        }
        // System.err.println("Search succeeded via getResource()");
        return url;
    }

    /*
    * Check the file separator to see if we're on a Windows platform.
    *
    * @return	boolean True if the platform is Windows, false otherwise.
    */
    private static boolean platformIsWindows()
    {
        if(File.separatorChar == '\\')
        {
            return true;
        }
        return false;
    }

    private static final String absolutePath = "abs://";
    private static final String pct20 = "%20";
    private static final int pct20len = 3;
}