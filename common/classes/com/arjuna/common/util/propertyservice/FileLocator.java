/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
* Copyright (C) 1998, 1999, 2000, 2001,
*
* Arjuna Solutions Limited,
* Newcastle upon Tyne,
* Tyne and Wear,
* UK.
*
* $Id: FileLocator.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.common.util.propertyservice;

import java.io.File;
import java.net.URL;

import java.io.FileNotFoundException;

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
     * Return the (URL decoded) absolute pathname to the file or null.
     */
    static String locateFile (String findFile, ClassLoader classLoader) throws FileNotFoundException
    {
        URL url;
        String fullPathName;
        StringBuffer decodedPathName;
        int pos, len, start;

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
        {
            /*
        * The URL that we receive from getResource /might/ have ' '
        * (space) characters converted to "%20" strings.  However,
        * it doesn't have other URL encoding (e.g '+' characters are
        * kept intact), so we'll just convert all "%20" strings to
        * ' ' characters and hope for the best.
        */
            fullPathName = url.getFile();
            pos = 0;
            len = fullPathName.length();
            start = 0;
            decodedPathName = new StringBuffer();

            while ((pos = fullPathName.indexOf(pct20, start)) != -1) {
                decodedPathName.append(fullPathName.substring(start, pos));
                decodedPathName.append(' ');
                start = pos + pct20len;
            }

            if (start < len)
                decodedPathName.append(fullPathName.substring(start, len));

            fullPathName=decodedPathName.toString();

            if (platformIsWindows())
                fullPathName = fullPathName.substring(1, fullPathName.length());

            return fullPathName;
        }

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

