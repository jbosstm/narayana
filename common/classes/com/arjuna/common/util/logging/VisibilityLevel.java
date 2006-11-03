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
package com.arjuna.common.util.logging;

/**
 * The VisibilityLevel class provides default finer visibility value to determine if finer
 * debugging is allowed or not. The various levels of common debugging that are available:
 * <ul>
 * <li><pre>VIS_NONE      = 0x00000000</pre> (no visibility).
 * <li><pre>VIS_PRIVATE   = 0x0001</pre> (only from private methods).
 * <li><pre>VIS_PROTECTED = 0x0002</pre> (only from protected methods).
 * <li><pre>VIS_PUBLIC    = 0x0004</pre>  (only from public methods).
 * <li><pre>VIS_PACKAGE   = 0x0008</pre>  (only from package methods).
 * <li><pre>VIS_ALL	  = 0xffffffff</pre>  (output all visbility levels).
 * </ul>
 * 
 * @since 1.0
 * @version $id$
 * @author Malik SAHEB - malik.saheb@arjuna.com
 */

public class VisibilityLevel //implements VisibilityLevel
{
   public static final long VIS_NONE = 0x00000000;
   public static final long VIS_PRIVATE = 0x00000001;
   public static final long VIS_PROTECTED = 0x00000002;
   public static final long VIS_PUBLIC = 0x00000004;
   public static final long VIS_PACKAGE = 0x00000008;
   public static final long VIS_ALL = 0xffffffff;

   /**
    * @return the VisibilityCode value associted with the provided string representation
    * @param level The string representation of the visibility level, e.g. "VIS_PUBLIC"
    */
   public long getLevel (String level)
   {
      if (level.equals("VIS_NONE"))
         return VIS_NONE;
      if (level.equals("VIS_PRIVATE"))
         return VIS_PRIVATE;
      if (level.equals("VIS_PROTECTED"))
         return VIS_PROTECTED;
      if (level.equals("VIS_PUBLIC"))
         return VIS_PUBLIC;
      if (level.equals("VIS_PACKAGE"))
         return VIS_PACKAGE;
      if (level.equals("VIS_ALL"))
         return VIS_ALL;

      return VIS_NONE;
   }

   /**
    * @return the string representation of the visibility level. Note, this
    * string is intended only for debugging purposes, and cannot be fed
    * back into the debug system to obtain the visibility level that it
    * represents.
    * @param level The value of the visibility level
    *
    */

   public String printString (long level)
   {
      if (level == VIS_ALL)
         return "VIS_ALL";

      if (level == VIS_NONE)
         return "VIS_NONE";

      String sLevel = null;

      if ((level & VIS_PRIVATE) != 0)
         sLevel = ((sLevel == null) ? "VIS_PRIVATE" : " & VIS_PRIVATE");
      if ((level & VIS_PROTECTED) != 0)
         sLevel = ((sLevel == null) ? "VIS_PROTECTED" : " & VIS_PROTECTED");
      if ((level & VIS_PUBLIC) != 0)
         sLevel = ((sLevel == null) ? "VIS_PUBLIC" : " & VIS_PUBLIC");
      if ((level & VIS_PACKAGE) != 0)
         sLevel = ((sLevel == null) ? "VIS_PACKAGE" : " & VIS_PACKAGE");
      if ((level & VisibilityLevel.VIS_ALL) != 0)
         sLevel = ((sLevel == null) ? "VIS_ALL" : " & VIS_ALL");

      return ((sLevel == null) ? "VIS_NONE" : sLevel);
   }

}






