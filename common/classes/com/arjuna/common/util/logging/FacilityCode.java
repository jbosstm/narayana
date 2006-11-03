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
 * The FacilityCode class provides default finer facilitycode value to determine if finer
 * debugging is allowed or not. The various levels of common debugging that are available:
 * <ul>
 * <li><pre>FAC_NONE   = 0x00000000</pre> (no facility).
 * <li><pre>FAC_ALL	 = 0xffffffff</pre> (output all facility codes).
 * </ul>
 *
 * @since 1.0
 * @version $id$
 * @author Malik SAHEB - malik.saheb@arjuna.com
 */

public class FacilityCode
{
   public static final long FAC_NONE = 0x00000000;
   public static final long FAC_ALL = 0xFFFFFFFF;

   /**
    * @return the FacilityCode value associted with the provided string representation
    * @param level is the string representation of the Level
    */
   public long getLevel (String level)
   {
      if (level.equals("FAC_ALL"))
      {
         return FAC_ALL;
      }
      else
      {
         return FAC_NONE;
      }
   }

   /**
    * @return the string representation of the facility level. Note, this
    * string is intended only for debugging purposes.
    * @param level is the value of the provided Level
    */
   public String printString (long level)
   {

      if (level == FAC_ALL)
      {
         return "FAC_ALL";
      }
      else
      {
         return "FAC_NONE";
      }
   }
}
