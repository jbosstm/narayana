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
* Copyright (C) 2001,
*
* Arjuna Solutions Limited,
* Newcastle upon Tyne,
* Tyne and Wear,
* UK.
*
* $Id: ClassPathParser.java 2342 2006-03-30 13:06:17Z  $
*/

package com.arjuna.common.util;

import java.lang.StringIndexOutOfBoundsException;

/**
 * This class parses the CLASSPATH and returns the directories used
 * within it.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ClassPathParser.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class ClassPathParser
{

   public ClassPathParser ()
   {
      _classPath = System.getProperty("java.class.path");
      _start = 0;
   }

   /**
    * @return the directory path of the next entry in the CLASSPATH that
    * contains the specified string.
    */

   public final String getPath (String contains)
   {
      String toReturn = null;

      if ((_classPath != null) && (contains != null))
      {
         int indx = _classPath.indexOf(contains, _start);

         if (indx != -1)
         {
            int lastIndex = _classPath.indexOf(pathSeparator, _start);
            int sepIndex = lastIndex+1;

            if (lastIndex > indx)  // at start of path?
               sepIndex = 0;
            else
            {
               while ((lastIndex < indx) && (lastIndex != -1))
               {
                  lastIndex = _classPath.indexOf(pathSeparator, sepIndex);

                  if (lastIndex == -1)
                     lastIndex = _classPath.length();
                  else
                  {
                     if (lastIndex < indx)
                        sepIndex = lastIndex+1;
                  }
               }
            }

            try
            {
               toReturn = _classPath.substring(sepIndex, lastIndex);

               _start = indx+1;
            }
            catch (StringIndexOutOfBoundsException e)
            {
               // nothing left!!
            }
         }
      }

      return toReturn;
   }

   /**
    * Reload the classpath and reset the class ready to re-parse.
    *
    * @return <code>true</code> if a non-null CLASSPATH was loaded,
    * <code>false</code> otherwise.
    */

   public final boolean reset ()
   {
      _classPath = System.getProperty("java.class.path");

      _start = 0;

      return (boolean) (_classPath != null);
   }

   private String _classPath;
   private int    _start;

   private static final char winSeparator = ';';
   private static final char unixSeparator = ':';

   private static char pathSeparator = unixSeparator;

   static
   {
      pathSeparator = System.getProperty("path.separator").charAt(0);
   }

}
