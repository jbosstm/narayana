/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.common.tests.propertyservice;

// copied into the com.arjuna namespace from org.jboss.util
// as we don't want JBossTS to depend on JBoss common-core just for this one class.
// original version is jboss-common-core 2.2.8.GA
// svn.jboss.org/repos/common/common-core/tags/2.2.8.GA/src/test/java/org/jboss/test/util/test/StringPropertyReplacerUnitTestCase.java

import static com.arjuna.common.util.propertyservice.StringPropertyReplacer.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.util.Properties;

/**
 * A StringPropertyReplacerUnitTestCase.
 *
 * @author Brian Stansberry
 * @author Jonathan Halliday - forked to create the com.arjuna version and updated for junit4
 * @version $Revision$
 *
 */
public class StringPropertyReplacerUnitTestCase
{
   private static final String PROP_A = "string.prop.replace.test.a";
   private static final String PROP_B = "string.prop.replace.test.b";
   private static final String PROP_C = "string.prop.replace.test.c";
   private static final String PROP_D = "string.prop.replace.test.d";
   private static final String DEFAULT = "DEFAULT";
   private static final String VALUE = "VALUE";
   private static final String WRAPPER = "wrapper";

   @After
   public void tearDown() throws Exception
   {
      System.clearProperty(PROP_A);
      System.clearProperty(PROP_B);
      System.clearProperty(PROP_C);
      System.clearProperty(PROP_D);
   }

   private static Properties setupProperties()
   {
      Properties props = new Properties();
      props.put(PROP_A, VALUE);
      props.put(PROP_C, VALUE);
      return props;
   }

   private static void setupSystemProperties()
   {
      System.setProperty(PROP_A, VALUE);
      System.setProperty(PROP_C, VALUE);
   }

    @Test
   public void testNullInput()
   {
      try
      {
         assertNull(replaceProperties(null));
         fail("NPE expected with null input");
      }
      catch (NullPointerException good) {}

      try
      {
         assertNull(replaceProperties(null, setupProperties()));
         fail("NPE expected with null input");
      }
      catch (NullPointerException good) {}
   }

    @Test
   public void testBasicReplacement()
   {
      basicReplacementTest(false);
   }

    @Test
   public void testBasicReplacementFromSystemProps()
   {
      basicReplacementTest(true);
   }

   private void basicReplacementTest(boolean useSysProps)
   {
      String input = "${"+PROP_A+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(VALUE, output);
   }

    @Test
   public void testWrappedReplacement()
   {
      wrappedReplacementTest(false);
   }

    @Test
   public void testWrappedReplacementFromSystemProps()
   {
      wrappedReplacementTest(true);
   }

   private void wrappedReplacementTest(boolean useSysProps)
   {
      String input = WRAPPER+"${"+PROP_A+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(WRAPPER+VALUE, output);

      input = "${"+PROP_A+"}"+WRAPPER;
      output = null;
      if (useSysProps)
      {
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(VALUE+WRAPPER, output);

      input = WRAPPER+"${"+PROP_A+"}"+WRAPPER;
      output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(WRAPPER+VALUE+WRAPPER, output);
   }

    @Test
   public void testMissingProperty()
   {
      missingPropertyTest(false);
   }

    @Test
   public void testMissingPropertyFromSystemProps()
   {
      missingPropertyTest(true);
   }

   private void missingPropertyTest(boolean useSysProps)
   {
      String input = WRAPPER+"${"+PROP_B+"}"+WRAPPER;
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(input, output);
   }

    @Test
   public void testWrappedMissingProperty()
   {
      wrappedMissingPropertyTest(false);
   }

    @Test
   public void testWrappedMissingPropertyFromSystemProps()
   {
      wrappedMissingPropertyTest(true);
   }

   private void wrappedMissingPropertyTest(boolean useSysProps)
   {
      String input = WRAPPER+"${"+PROP_B+"}"+WRAPPER;
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(input, output);
   }

    @Test
   public void testDefaultValue()
   {
      defaultValueTest(false);
   }

    @Test
   public void testDefaultValueFromSystemProps()
   {
      defaultValueTest(true);
   }

   private void defaultValueTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+":"+DEFAULT+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(DEFAULT, output);
   }

    @Test
   public void testSecondaryProperty()
   {
      secondaryPropertyTest(false);
   }

    @Test
   public void testSecondaryPropertyFromSystemProps()
   {
      secondaryPropertyTest(true);
   }

   private void secondaryPropertyTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+","+PROP_C+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(VALUE, output);
   }

    @Test
   public void testSecondaryPropertyAndDefault()
   {
      secondaryPropertyAndDefaultTest(false);
   }

    @Test
   public void testSecondaryPropertyAndDefaultFromSystemProps()
   {
      secondaryPropertyAndDefaultTest(true);
   }

   private void secondaryPropertyAndDefaultTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+","+PROP_D+":"+DEFAULT+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(DEFAULT, output);
   }

    @Test
   public void testSecondaryPropertyAndMissing()
   {
      secondaryPropertyAndMissingTest(false);
   }

    @Test
   public void testSecondaryPropertyAndMissingFromSystemProps()
   {
      secondaryPropertyAndMissingTest(true);
   }

   private void secondaryPropertyAndMissingTest(boolean useSysProps)
   {
      String input = "${"+PROP_B+","+PROP_D+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(input, output);
   }

    @Test
   public void testMultipleReplacements()
   {
      multipleReplacementTest(false);
   }

    @Test
   public void testMultipleReplacementsFromSystemProps()
   {
      multipleReplacementTest(true);
   }

   private void multipleReplacementTest(boolean useSysProps)
   {
      String input = "${"+PROP_A+"}${"+PROP_C+"}";
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(VALUE+VALUE, output);
   }

    @Test
   public void testPartialMissing()
   {
      partialMissingTest(false);
   }

    @Test
   public void testPartialMissingFromSystemProps()
   {
      partialMissingTest(true);
   }

   private void partialMissingTest(boolean useSysProps)
   {
      String badinput = "${"+PROP_B+"}";
      String input = WRAPPER+"${"+PROP_A+"}"+badinput+"${"+PROP_C+"}"+WRAPPER;
      String output = null;
      if (useSysProps)
      {
         setupSystemProperties();
         output = replaceProperties(input);
      }
      else
      {
         output = replaceProperties(input, setupProperties());
      }

      assertEquals(WRAPPER+VALUE+badinput+VALUE+WRAPPER, output);
   }

}
