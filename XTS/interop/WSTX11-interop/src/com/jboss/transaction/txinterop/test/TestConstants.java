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
package com.jboss.transaction.txinterop.test ;

import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;

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
     * test name for all AT tests
     */
    public static final String NAME_ALL_AT_TESTS = "allATTests" ;
    /**
     * test name for all BA tests
     */
    public static final String NAME_ALL_BA_TESTS = "allBATests" ;
    /**
     * The prefix for all AT tests.
     */
    public static final String PREFIX_AT_TESTS = "testAT" ;
    /**
     * The prefix for all BA tests.
     */
    public static final String PREFIX_BA_TESTS = "testBA" ;
    
    /**
     * test name for AT Scenario 1.1
     */
    public static final String NAME_TEST_AT_1_1 = PREFIX_AT_TESTS + "1_1" ;
    /**
     * test description for AT Scenario 1.1
     */
    public static final String DESCRIPTION_TEST_AT_1_1 = "1.1 - " + ATInteropConstants.INTEROP_ELEMENT_COMPLETION_COMMIT ;
    /**
     * test name for AT Scenario 1.2
     */
    public static final String NAME_TEST_AT_1_2 = PREFIX_AT_TESTS + "1_2" ;
    /**
     * test description for AT Scenario 1.2
     */
    public static final String DESCRIPTION_TEST_AT_1_2 = "1.2 - " + ATInteropConstants.INTEROP_ELEMENT_COMPLETION_ROLLBACK ;
    /**
     * test name for AT Scenario 2.1
     */
    public static final String NAME_TEST_AT_2_1 = PREFIX_AT_TESTS + "2_1" ;
    /**
     * test description for AT Scenario 2.1
     */
    public static final String DESCRIPTION_TEST_AT_2_1 = "2.1 - " + ATInteropConstants.INTEROP_ELEMENT_COMMIT ;
    /**
     * test name for AT Scenario 2.2
     */
    public static final String NAME_TEST_AT_2_2 = PREFIX_AT_TESTS + "2_2" ;
    /**
     * test description for AT Scenario 2.2
     */
    public static final String DESCRIPTION_TEST_AT_2_2 = "2.2 - " + ATInteropConstants.INTEROP_ELEMENT_ROLLBACK ;
    /**
     * test name for AT Scenario 3.1
     */
    public static final String NAME_TEST_AT_3_1 = PREFIX_AT_TESTS + "3_1" ;
    /**
     * test description for AT Scenario 3.1
     */
    public static final String DESCRIPTION_TEST_AT_3_1 = "3.1 - " + ATInteropConstants.INTEROP_ELEMENT_PHASE_2_ROLLBACK ;
    /**
     * test name for AT Scenario 3.2
     */
    public static final String NAME_TEST_AT_3_2 = PREFIX_AT_TESTS + "3_2" ;
    /**
     * test description for AT Scenario 3.2
     */
    public static final String DESCRIPTION_TEST_AT_3_2 = "3.2 - " + ATInteropConstants.INTEROP_ELEMENT_READONLY ;
    /**
     * test name for AT Scenario 3.3
     */
    public static final String NAME_TEST_AT_3_3 = PREFIX_AT_TESTS + "3_3" ;
    /**
     * test description for AT Scenario 3.3
     */
    public static final String DESCRIPTION_TEST_AT_3_3 = "3.3 - " + ATInteropConstants.INTEROP_ELEMENT_VOLATILE_AND_DURABLE ;
    /**
     * test name for AT Scenario 4.1
     */
    public static final String NAME_TEST_AT_4_1 = PREFIX_AT_TESTS + "4_1" ;
    /**
     * test description for AT Scenario 4.1
     */
    public static final String DESCRIPTION_TEST_AT_4_1 = "4.1 - " + ATInteropConstants.INTEROP_ELEMENT_EARLY_READONLY ;
    /**
     * test name for AT Scenario 4.2
     */
    public static final String NAME_TEST_AT_4_2 = PREFIX_AT_TESTS + "4_2" ;
    /**
     * test description for AT Scenario 4.2
     */
    public static final String DESCRIPTION_TEST_AT_4_2 = "4.2 - " + ATInteropConstants.INTEROP_ELEMENT_EARLY_ABORTED ;
    /**
     * test name for AT Scenario 5.1
     */
    public static final String NAME_TEST_AT_5_1 = PREFIX_AT_TESTS + "5_1" ;
    /**
     * test description for AT Scenario 5.1
     */
    public static final String DESCRIPTION_TEST_AT_5_1 = "5.1 - " + ATInteropConstants.INTEROP_ELEMENT_REPLAY_COMMIT ;
    /**
     * test name for AT Scenario 5.2
     */
    public static final String NAME_TEST_AT_5_2 = PREFIX_AT_TESTS + "5_2" ;
    /**
     * test description for AT Scenario 5.2
     */
    public static final String DESCRIPTION_TEST_AT_5_2 = "5.2 - " + ATInteropConstants.INTEROP_ELEMENT_RETRY_PREPARED_COMMIT ;
    /**
     * test name for AT Scenario 5.3
     */
    public static final String NAME_TEST_AT_5_3 = PREFIX_AT_TESTS + "5_3" ;
    /**
     * test description for AT Scenario 5.3
     */
    public static final String DESCRIPTION_TEST_AT_5_3 = "5.3 - " + ATInteropConstants.INTEROP_ELEMENT_RETRY_PREPARED_ABORT ;
    /**
     * test name for AT Scenario 5.4
     */
    public static final String NAME_TEST_AT_5_4 = PREFIX_AT_TESTS + "5_4" ;
    /**
     * test description for AT Scenario 5.4
     */
    public static final String DESCRIPTION_TEST_AT_5_4 = "5.4 - " + ATInteropConstants.INTEROP_ELEMENT_RETRY_COMMIT ;
    /**
     * test name for AT Scenario 5.5
     */
    public static final String NAME_TEST_AT_5_5 = PREFIX_AT_TESTS + "5_5" ;
    /**
     * test description for AT Scenario 5.5
     */
    public static final String DESCRIPTION_TEST_AT_5_5 = "5.5 - " + ATInteropConstants.INTEROP_ELEMENT_PREPARED_AFTER_TIMEOUT ;
    /**
     * test name for AT Scenario 5.6
     */
    public static final String NAME_TEST_AT_5_6 = PREFIX_AT_TESTS + "5_6" ;
    /**
     * test description for AT Scenario 5.6
     */
    public static final String DESCRIPTION_TEST_AT_5_6 = "5.6 - " + ATInteropConstants.INTEROP_ELEMENT_LOST_COMMITTED ;

    /**
     * test name for BA Scenario 1.1
     */
    public static final String NAME_TEST_BA_1_1 = PREFIX_BA_TESTS + "1_1" ;
    /**
     * test description for BA Scenario 1.1
     */
    public static final String DESCRIPTION_TEST_BA_1_1 = "1.1 - " + BAInteropConstants.INTEROP_ELEMENT_CANCEL ;
    /**
     * test name for BA Scenario 1.2
     */
    public static final String NAME_TEST_BA_1_2 = PREFIX_BA_TESTS + "1_2" ;
    /**
     * test description for BA Scenario 1.2
     */
    public static final String DESCRIPTION_TEST_BA_1_2 = "1.2 - " + BAInteropConstants.INTEROP_ELEMENT_EXIT ;
    /**
     * test name for BA Scenario 1.3
     */
    public static final String NAME_TEST_BA_1_3 = PREFIX_BA_TESTS + "1_3" ;
    /**
     * test description for BA Scenario 1.3
     */
    public static final String DESCRIPTION_TEST_BA_1_3 = "1.3 - " + BAInteropConstants.INTEROP_ELEMENT_FAIL ;
    /**
     * test name for BA Scenario 1.4
     */
    public static final String NAME_TEST_BA_1_4 = PREFIX_BA_TESTS + "1_4" ;
    /**
     * test description for BA Scenario 1.4
     */
    public static final String DESCRIPTION_TEST_BA_1_4 = "1.4 - " + BAInteropConstants.INTEROP_ELEMENT_CANNOT_COMPLETE ;
    /**
     * test name for BA Scenario 1.5
     */
    public static final String NAME_TEST_BA_1_5 = PREFIX_BA_TESTS + "1_5" ;
    /**
     * test description for BA Scenario 1.5
     */
    public static final String DESCRIPTION_TEST_BA_1_5 = "1.5 - " + BAInteropConstants.INTEROP_ELEMENT_PARTICIPANT_COMPLETE_CLOSE ;
    /**
     * test name for BA Scenario 1.6
     */
    public static final String NAME_TEST_BA_1_6 = PREFIX_BA_TESTS + "1_6" ;
    /**
     * test description for BA Scenario 1.6
     */
    public static final String DESCRIPTION_TEST_BA_1_6 = "1.6 - " + BAInteropConstants.INTEROP_ELEMENT_COORDINATOR_COMPLETE_CLOSE ;
    /**
     * test name for BA Scenario 1.7
     */
    public static final String NAME_TEST_BA_1_7 = PREFIX_BA_TESTS + "1_7" ;
    /**
     * test description for BA Scenario 1.7
     */
    public static final String DESCRIPTION_TEST_BA_1_7 = "1.7 - " + BAInteropConstants.INTEROP_ELEMENT_UNSOLICITED_COMPLETE ;
    /**
     * test name for BA Scenario 1.8
     */
    public static final String NAME_TEST_BA_1_8 = PREFIX_BA_TESTS + "1_8" ;
    /**
     * test description for BA Scenario 1.8
     */
    public static final String DESCRIPTION_TEST_BA_1_8 = "1.8 - " + BAInteropConstants.INTEROP_ELEMENT_COMPENSATE ;
    /**
     * test name for BA Scenario 1.9
     */
    public static final String NAME_TEST_BA_1_9 = PREFIX_BA_TESTS + "1_9" ;
    /**
     * test description for BA Scenario 1.9
     */
    public static final String DESCRIPTION_TEST_BA_1_9 = "1.9 - " + BAInteropConstants.INTEROP_ELEMENT_COMPENSATION_FAIL ;
    /**
     * test name for BA Scenario 1.10
     */
    public static final String NAME_TEST_BA_1_10 = PREFIX_BA_TESTS + "1_10" ;
    /**
     * test description for BA Scenario 1.10
     */
    public static final String DESCRIPTION_TEST_BA_1_10 = "1.10 - " + BAInteropConstants.INTEROP_ELEMENT_PARTICIPANT_CANCEL_COMPLETED_RACE ;
    /**
     * test name for BA Scenario 1.11
     */
    public static final String NAME_TEST_BA_1_11 = PREFIX_BA_TESTS + "1_11" ;
    /**
     * test description for BA Scenario 1.11
     */
    public static final String DESCRIPTION_TEST_BA_1_11 = "1.11 - " + BAInteropConstants.INTEROP_ELEMENT_MESSAGE_LOSS_AND_RECOVERY ;
    /**
     * test name for BA Scenario 1.12
     */
    public static final String NAME_TEST_BA_1_12 = PREFIX_BA_TESTS + "1_12" ;
    /**
     * test description for BA Scenario 1.12
     */
    public static final String DESCRIPTION_TEST_BA_1_12 = "1.12 - " + BAInteropConstants.INTEROP_ELEMENT_MIXED_OUTCOME ;

    /**
     * The name to description map.
     */
    public static final Map DESCRIPTIONS ;
    
    static
    {
        final TreeMap descriptions = new TreeMap() ;
        
        descriptions.put(NAME_TEST_AT_1_1, DESCRIPTION_TEST_AT_1_1) ;
        descriptions.put(NAME_TEST_AT_1_2, DESCRIPTION_TEST_AT_1_2) ;
        descriptions.put(NAME_TEST_AT_2_1, DESCRIPTION_TEST_AT_2_1) ;
        descriptions.put(NAME_TEST_AT_2_2, DESCRIPTION_TEST_AT_2_2) ;
        descriptions.put(NAME_TEST_AT_3_1, DESCRIPTION_TEST_AT_3_1) ;
        descriptions.put(NAME_TEST_AT_3_2, DESCRIPTION_TEST_AT_3_2) ;
        descriptions.put(NAME_TEST_AT_3_3, DESCRIPTION_TEST_AT_3_3) ;
        descriptions.put(NAME_TEST_AT_4_1, DESCRIPTION_TEST_AT_4_1) ;
        descriptions.put(NAME_TEST_AT_4_2, DESCRIPTION_TEST_AT_4_2) ;
        descriptions.put(NAME_TEST_AT_5_1, DESCRIPTION_TEST_AT_5_1) ;
        descriptions.put(NAME_TEST_AT_5_2, DESCRIPTION_TEST_AT_5_2) ;
        descriptions.put(NAME_TEST_AT_5_3, DESCRIPTION_TEST_AT_5_3) ;
        descriptions.put(NAME_TEST_AT_5_4, DESCRIPTION_TEST_AT_5_4) ;
        descriptions.put(NAME_TEST_AT_5_5, DESCRIPTION_TEST_AT_5_5) ;
        descriptions.put(NAME_TEST_AT_5_6, DESCRIPTION_TEST_AT_5_6) ;
        descriptions.put(NAME_TEST_BA_1_1, DESCRIPTION_TEST_BA_1_1) ;
        descriptions.put(NAME_TEST_BA_1_2, DESCRIPTION_TEST_BA_1_2) ;
        descriptions.put(NAME_TEST_BA_1_3, DESCRIPTION_TEST_BA_1_3) ;
        descriptions.put(NAME_TEST_BA_1_4, DESCRIPTION_TEST_BA_1_4) ;
        descriptions.put(NAME_TEST_BA_1_5, DESCRIPTION_TEST_BA_1_5) ;
        descriptions.put(NAME_TEST_BA_1_6, DESCRIPTION_TEST_BA_1_6) ;
        // decommissioned
        // descriptions.put(NAME_TEST_BA_1_7, DESCRIPTION_TEST_BA_1_7) ;
        descriptions.put(NAME_TEST_BA_1_8, DESCRIPTION_TEST_BA_1_8) ;
        descriptions.put(NAME_TEST_BA_1_9, DESCRIPTION_TEST_BA_1_9) ;
        descriptions.put(NAME_TEST_BA_1_10, DESCRIPTION_TEST_BA_1_10) ;
        descriptions.put(NAME_TEST_BA_1_11, DESCRIPTION_TEST_BA_1_11) ;
//        descriptions.put(NAME_TEST_BA_1_12, DESCRIPTION_TEST_BA_1_12) ;
       
        DESCRIPTIONS = descriptions ;
    }
}