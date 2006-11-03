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
 * The DebugLevel class provides default finer debugging value to determine if finer
 * debugging is allowed or not. The various levels of common debugging that are available:
 * <ul>
 * <li><b>NO_DEBUGGING</b>	   = 0x0 (no debugging).
 * <li><b>CONSTRUCTORS</b>	   = 0x0001 (only output from constructors).
 * <li><b>DESTRUCTORS</b>	    = 0x0002 (only output from finalizers).
 * <li><b>CONSTRUCT_AND_DESTRUCT</b> = CONSTRUCTORS | DESTRUCTORS.
 * <li><b>FUNCTIONS</b>	      = 0x0010 (only output from methods).
 * <li><b>OPERATORS</b>	      = 0x0020 (only output from methods such as <code>equals, notEquals</code>).
 * <li><b>FUNCS_AND_OPS</b>	  = FUNCTIONS | OPERATORS.
 * <li><b>ALL_NON_TRIVIAL</b>	= CONSTRUCT_AND_DESTRUCT | FUNCTIONS | OPERATORS.
 * <li><b>TRIVIAL_FUNCS</b>	  = 0x0100 (only output from trivial methods).
 * <li><b>TRIVIAL_OPERATORS</b>      = 0x0200 (only output from trivial operators).
 * <li><b>ALL_TRIVIAL</b>	    = TRIVIAL_FUNCS | TRIVIAL_OPERATORS.
 * <li><b>ERROR_MESSAGES</b>	 = 0x0400 (only output from debugging error/warning messages).
 * <li><b>FULL_DEBUGGING</b>	 = 0xffff (output all debugging messages).
 * </ul>
 *
 * @since 1.0
 * @version $id$
 * @author Malik SAHEB - malik.saheb@arjuna.com
 */

public class DebugLevel //implements DebugLevel
{
   public static final long NO_DEBUGGING = 0;
   public static final long CONSTRUCTORS = 0x00000001;
   public static final long DESTRUCTORS = 0x00000002;
   public static final long CONSTRUCT_AND_DESTRUCT = CONSTRUCTORS | DESTRUCTORS;
   public static final long FUNCTIONS = 0x00000010;
   public static final long OPERATORS = 0x00000020;
   public static final long FUNCS_AND_OPS = FUNCTIONS | OPERATORS;
   public static final long ALL_NON_TRIVIAL = CONSTRUCT_AND_DESTRUCT | FUNCTIONS | OPERATORS;
   public static final long TRIVIAL_FUNCS = 0x00000100;
   public static final long TRIVIAL_OPERATORS = 0x00000200;
   public static final long ALL_TRIVIAL = TRIVIAL_FUNCS | TRIVIAL_OPERATORS;
   public static final long ERROR_MESSAGES = 0x00000400;
   public static final long FULL_DEBUGGING = 0xffffffff;

   /**
    * @param level is the string representation of the Level
    * @return the Finer Debugging Value associated with string representation
    */
   public long getLevel (String level)
   {
      if (level.equals("NO_DEBUGGING"))
         return NO_DEBUGGING;
      if (level.equals("CONSTRUCTORS"))
         return CONSTRUCTORS;
      if (level.equals("DESTRUCTORS"))
         return DESTRUCTORS;
      if (level.equals("CONSTRUCT_AND_DESTRUCT"))
         return CONSTRUCT_AND_DESTRUCT;
      if (level.equals("FUNCTIONS"))
         return FUNCTIONS;
      if (level.equals("OPERATORS"))
         return OPERATORS;
      if (level.equals("FUNCS_AND_OPS"))
         return FUNCS_AND_OPS;
      if (level.equals("ALL_NON_TRIVIAL"))
         return ALL_NON_TRIVIAL;
      if (level.equals("TRIVIAL_FUNCS"))
         return TRIVIAL_FUNCS;
      if (level.equals("TRIVIAL_OPERATORS"))
         return TRIVIAL_OPERATORS;
      if (level.equals("ALL_TRIVIAL"))
         return ALL_TRIVIAL;
      if (level.equals("ERROR_MESSAGES"))
         return ERROR_MESSAGES;
      if (level.equals("FULL_DEBUGGING"))
         return FULL_DEBUGGING;

      return NO_DEBUGGING;
   }

   /**
    * @param level is the value of the provided Level
    * @return the string representation of the finer debugging level.
    */

   public String printString (long level)
   {
      if (level == NO_DEBUGGING)
         return "NO_DEBUGGING";

      if (level == FULL_DEBUGGING)
         return "FULL_DEBUGGING";

      String sLevel = null;

      if ((level & CONSTRUCTORS) != 0)
         sLevel = ((sLevel == null) ? "CONSTRUCTORS" : " & CONSTRUCTORS");
      if ((level & DESTRUCTORS) != 0)
         sLevel = ((sLevel == null) ? "DESTRUCTORS" : " & DESTRUCTORS");

      if ((level & FUNCTIONS) != 0)
         sLevel = ((sLevel == null) ? "FUNCTIONS" : " & FUNCTIONS");

      if ((level & OPERATORS) != 0)
         sLevel = ((sLevel == null) ? "OPERATORS" : " & OPERATORS");

      if ((level & TRIVIAL_FUNCS) != 0)
         sLevel = ((sLevel == null) ? "TRIVIAL_FUNCS" : " & TRIVIAL_FUNCS");
      if ((level & TRIVIAL_OPERATORS) != 0)
         sLevel = ((sLevel == null) ? "TRIVIAL_OPERATORS" : " & TRIVIAL_OPERATORS");
      if ((level & ERROR_MESSAGES) != 0)
         sLevel = ((sLevel == null) ? "ERROR_MESSAGES" : " & ERROR_MESSAGES");

      return ((sLevel == null) ? "NO_DEBUGGING" : sLevel);
   }


}





