/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package com.jboss.transaction.wstf.test ;

import com.jboss.transaction.wstf.webservices.sc007.InteropConstants;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class contains constants used to map the front end onto
 * the WS-TX interop tests.
 * @author kevin
 */
public class TestConstants
{
    /**
     * The name of the Service URI parameter.
     */
    public static final String PARAM_SERVICE_URI = "serviceuri" ;
    /**
     * The name of the test parameter.
     */
    public static final String PARAM_TEST = "test" ;
    /**
     * The name of the test timeout parameter.
     */
    public static final String PARAM_TEST_TIMEOUT = "testTimeout" ;
    /**
     * The name of the asynchronous test flag parameter.
     */
    public static final String PARAM_ASYNC_TEST = "asyncTest" ;
    /**
     * The name of the result page parameter.
     */
    public static final String PARAM_RESULT_PAGE = "resultPage";
    /**
     * Default address of page with results.
     */
    public static final String DEFAULT_RESULT_PAGE_ADDRESS = "/results.jsp";
    
    /**
     * The name of the test results attribute.
     */
    public static final String ATTRIBUTE_TEST_RESULT = "testResult" ;
    /**
     * The name of the test validation attribute.
     */
    public static final String ATTRIBUTE_TEST_VALIDATION = "testValidation" ;
    /**
     * The name of the log count attribute.
     */
    public static final String ATTRIBUTE_LOG_COUNT = "logCount" ;
    /**
     * The name of the log name attribute.
     */
    public static final String ATTRIBUTE_LOG_NAME = "logName" ;
    
    /**
     * test name for all tests
     */
    public static final String NAME_ALL_TESTS = "allTests" ;
    /**
     * The prefix for all AT tests.
     */
    public static final String PREFIX_TESTS = "test" ;
    /**
     * test name for AT Scenario 1.1
     */
    public static final String NAME_TEST_AT_1_1 = PREFIX_TESTS + "1_1" ;
    /**
     * test description for AT Scenario 1.1
     */
    public static final String DESCRIPTION_TEST_1_1 = "1.1 - " + InteropConstants.INTEROP_ELEMENT_COMPLETION_COMMIT ;
    /**
     * test name for AT Scenario 1.2
     */
    public static final String NAME_TEST_1_2 = PREFIX_TESTS + "1_2" ;
    /**
     * test description for AT Scenario 1.2
     */
    public static final String DESCRIPTION_TEST_1_2 = "1.2 - " + InteropConstants.INTEROP_ELEMENT_COMPLETION_ROLLBACK ;
    /**
     * test name for AT Scenario 2.1
     */
    public static final String NAME_TEST_2_1 = PREFIX_TESTS + "2_1" ;
    /**
     * test description for AT Scenario 2.1
     */
    public static final String DESCRIPTION_TEST_2_1 = "2.1 - " + InteropConstants.INTEROP_ELEMENT_COMMIT ;
    /**
     * test name for AT Scenario 2.2
     */
    public static final String NAME_TEST_2_2 = PREFIX_TESTS + "2_2" ;
    /**
     * test description for AT Scenario 2.2
     */
    public static final String DESCRIPTION_TEST_2_2 = "2.2 - " + InteropConstants.INTEROP_ELEMENT_ROLLBACK ;
    /**
     * test name for AT Scenario 3.1
     */
    public static final String NAME_TEST_3_1 = PREFIX_TESTS + "3_1" ;
    /**
     * test description for AT Scenario 3.1
     */
    public static final String DESCRIPTION_TEST_3_1 = "3.1 - " + InteropConstants.INTEROP_ELEMENT_PHASE_2_ROLLBACK ;
    /**
     * test name for AT Scenario 3.2
     */
    public static final String NAME_TEST_3_2 = PREFIX_TESTS + "3_2" ;
    /**
     * test description for AT Scenario 3.2
     */
    public static final String DESCRIPTION_TEST_3_2 = "3.2 - " + InteropConstants.INTEROP_ELEMENT_READONLY ;
    /**
     * test name for AT Scenario 3.3
     */
    public static final String NAME_TEST_3_3 = PREFIX_TESTS + "3_3" ;
    /**
     * test description for AT Scenario 3.3
     */
    public static final String DESCRIPTION_TEST_3_3 = "3.3 - " + InteropConstants.INTEROP_ELEMENT_VOLATILE_AND_DURABLE ;
    /**
     * test name for AT Scenario 4.1
     */
    public static final String NAME_TEST_3_4 = PREFIX_TESTS + "3_4" ;
    /**
     * test description for AT Scenario 3.4
     */
    public static final String DESCRIPTION_TEST_3_4 = "3.4 - " + InteropConstants.INTEROP_ELEMENT_EARLY_READONLY ;
    /**
     * test name for AT Scenario 3.5
     */
    public static final String NAME_TEST_3_5 = PREFIX_TESTS + "3_5" ;
    /**
     * test description for AT Scenario 3.5
     */
    public static final String DESCRIPTION_TEST_3_5 = "3.5 - " + InteropConstants.INTEROP_ELEMENT_EARLY_ABORTED ;
    /**
     * test name for AT Scenario 3.6
     */
    public static final String NAME_TEST_3_6 = PREFIX_TESTS + "3_6" ;
    /**
     * test description for AT Scenario 3.6
     */
    public static final String DESCRIPTION_TEST_3_6 = "3.6 - " + InteropConstants.INTEROP_ELEMENT_REPLAY_COMMIT ;
    /**
     * test name for AT Scenario 3.7
     */
    public static final String NAME_TEST_3_7 = PREFIX_TESTS + "3_7" ;
    /**
     * test description for AT Scenario 3.7
     */
    public static final String DESCRIPTION_TEST_3_7 = "3.7 - " + InteropConstants.INTEROP_ELEMENT_RETRY_PREPARED_COMMIT ;
    /**
     * test name for AT Scenario 3.8
     */
    public static final String NAME_TEST_3_8 = PREFIX_TESTS + "3_8" ;
    /**
     * test description for AT Scenario 3.8
     */
    public static final String DESCRIPTION_TEST_3_8 = "3.8 - " + InteropConstants.INTEROP_ELEMENT_RETRY_PREPARED_ABORT ;
    /**
     * test name for AT Scenario 3.9
     */
    public static final String NAME_TEST_3_9 = PREFIX_TESTS + "3_9" ;
    /**
     * test description for AT Scenario 3.9
     */
    public static final String DESCRIPTION_TEST_3_9 = "3.9 - " + InteropConstants.INTEROP_ELEMENT_RETRY_COMMIT ;
    /**
     * test name for AT Scenario 3.10
     */
    public static final String NAME_TEST_3_10 = PREFIX_TESTS + "3_10" ;
    /**
     * test description for AT Scenario 3.10
     */
    public static final String DESCRIPTION_TEST_3_10 = "3.10 - " + InteropConstants.INTEROP_ELEMENT_PREPARED_AFTER_TIMEOUT ;
    /**
     * test name for AT Scenario 3.11
     */
    public static final String NAME_TEST_3_11 = PREFIX_TESTS + "3_11" ;
    /**
     * test description for AT Scenario 3.11
     */
    public static final String DESCRIPTION_TEST_3_11 = "3.11 - " + InteropConstants.INTEROP_ELEMENT_LOST_COMMITTED ;

    /**
     * The name to description map.
     */
    public static final Map DESCRIPTIONS ;
    
    static
    {
        final TreeMap descriptions = new TreeMap() ;
        
        descriptions.put(NAME_TEST_AT_1_1, DESCRIPTION_TEST_1_1) ;
        descriptions.put(NAME_TEST_1_2, DESCRIPTION_TEST_1_2) ;
        descriptions.put(NAME_TEST_2_1, DESCRIPTION_TEST_2_1) ;
        descriptions.put(NAME_TEST_2_2, DESCRIPTION_TEST_2_2) ;
        descriptions.put(NAME_TEST_3_1, DESCRIPTION_TEST_3_1) ;
        descriptions.put(NAME_TEST_3_2, DESCRIPTION_TEST_3_2) ;
        descriptions.put(NAME_TEST_3_3, DESCRIPTION_TEST_3_3) ;
        descriptions.put(NAME_TEST_3_4, DESCRIPTION_TEST_3_4) ;
        descriptions.put(NAME_TEST_3_5, DESCRIPTION_TEST_3_5) ;
        descriptions.put(NAME_TEST_3_6, DESCRIPTION_TEST_3_6) ;
        descriptions.put(NAME_TEST_3_7, DESCRIPTION_TEST_3_7) ;
        descriptions.put(NAME_TEST_3_8, DESCRIPTION_TEST_3_8) ;
        descriptions.put(NAME_TEST_3_9, DESCRIPTION_TEST_3_9) ;
        descriptions.put(NAME_TEST_3_10, DESCRIPTION_TEST_3_10) ;
        descriptions.put(NAME_TEST_3_11, DESCRIPTION_TEST_3_11) ;

        DESCRIPTIONS = descriptions ;
    }
}