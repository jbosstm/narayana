/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.common.tests.logging;

import com.arjuna.common.internal.util.logging.LoggingEnvironmentBean;
import com.arjuna.common.internal.util.logging.basic.BasicLogEnvironmentBean;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.common.tests.simple.EnvironmentBeanTest;
import com.arjuna.common.util.exceptions.LogConfigurationException;
import com.arjuna.common.util.logging.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Exercise assorted logging configuration options, particularly the Debug, Facility and Visibility filters.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-01
 */
public class LogConfig
{
    @Test
    public void testDebugLevels() {

        DebugLevel d = new DebugLevel();
        assertEquals(DebugLevel.NO_DEBUGGING, d.getLevel(d.printString(DebugLevel.NO_DEBUGGING)));
        assertEquals(DebugLevel.CONSTRUCTORS, d.getLevel(d.printString(DebugLevel.CONSTRUCTORS)));
        assertEquals(DebugLevel.DESTRUCTORS, d.getLevel(d.printString(DebugLevel.DESTRUCTORS)));

        assertEquals(DebugLevel.FUNCTIONS, d.getLevel(d.printString(DebugLevel.FUNCTIONS)));
        assertEquals(DebugLevel.OPERATORS, d.getLevel(d.printString(DebugLevel.OPERATORS)));


        assertEquals(DebugLevel.TRIVIAL_FUNCS, d.getLevel(d.printString(DebugLevel.TRIVIAL_FUNCS)));
        assertEquals(DebugLevel.TRIVIAL_OPERATORS, d.getLevel(d.printString(DebugLevel.TRIVIAL_OPERATORS)));

        assertEquals(DebugLevel.ERROR_MESSAGES, d.getLevel(d.printString(DebugLevel.ERROR_MESSAGES)));
        assertEquals(DebugLevel.FULL_DEBUGGING, d.getLevel(d.printString(DebugLevel.FULL_DEBUGGING)));
    }

    @Test
    public void testFacilityCode() {

        FacilityCode facilityCode = new FacilityCode();
        assertEquals(FacilityCode.FAC_NONE, facilityCode.getLevel(facilityCode.printString(FacilityCode.FAC_NONE)));
        assertEquals(FacilityCode.FAC_ALL, facilityCode.getLevel(facilityCode.printString(FacilityCode.FAC_ALL)));
    }

    @Test
    public void testVisibilityLevel() {

        VisibilityLevel visibilityLevel = new VisibilityLevel();
        assertEquals(VisibilityLevel.VIS_NONE, visibilityLevel.getLevel(visibilityLevel.printString(VisibilityLevel.VIS_NONE)));

        System.out.println(""+VisibilityLevel.VIS_PRIVATE);
        System.out.println(visibilityLevel.printString(VisibilityLevel.VIS_PRIVATE));
        System.out.printf(""+visibilityLevel.getLevel("VIS_PRIVATE"));

        assertEquals(VisibilityLevel.VIS_PRIVATE, visibilityLevel.getLevel(visibilityLevel.printString(VisibilityLevel.VIS_PRIVATE)));
        assertEquals(VisibilityLevel.VIS_PROTECTED, visibilityLevel.getLevel(visibilityLevel.printString(VisibilityLevel.VIS_PROTECTED)));
        assertEquals(VisibilityLevel.VIS_PUBLIC, visibilityLevel.getLevel(visibilityLevel.printString(VisibilityLevel.VIS_PUBLIC)));
        assertEquals(VisibilityLevel.VIS_PACKAGE, visibilityLevel.getLevel(visibilityLevel.printString(VisibilityLevel.VIS_PACKAGE)));
        assertEquals(VisibilityLevel.VIS_ALL, visibilityLevel.getLevel(visibilityLevel.printString(VisibilityLevel.VIS_ALL)));
    }

    @Test
    public void testBadFactory() {

        LoggingEnvironmentBean loggingEnvironmentBean = commonPropertyManager.getLoggingEnvironmentBean();
        String originalFactory = loggingEnvironmentBean.getLoggingFactory();

        loggingEnvironmentBean.setLoggingFactory("bogusFactory");
        LogFactory.reset();

        try {
             // should throw LogConfigurationException
            LogNoi18n logNoi18n = LogFactory.getLogNoi18n("test");
            fail("should not reach here");
        } catch(RuntimeException e) {
            // expected
            assertTrue(e.getCause() instanceof LogConfigurationException);
        } finally {
            loggingEnvironmentBean.setLoggingFactory(originalFactory);
            LogFactory.reset();
        }

    }

    @Test
    public void testLoggingEnvironmentBean() throws Exception {
        EnvironmentBeanTest.testBeanByReflection(new LoggingEnvironmentBean());
    }

    @Test
    public void testBasicLogEnvironmentBean() throws Exception {
        EnvironmentBeanTest.testBeanByReflection(new BasicLogEnvironmentBean());
    }

}
