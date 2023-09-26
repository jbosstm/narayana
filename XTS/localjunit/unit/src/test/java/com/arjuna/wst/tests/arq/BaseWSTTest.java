/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wst.tests.arq;

import com.arjuna.wst.tests.TestInitialisation;

import org.junit.After;
import org.junit.Before;

/**
 * @author paul.robinson@redhat.com 11/01/2013
 */
public class BaseWSTTest {

    private static TestInitialisation testInitialisation = new TestInitialisation();

    @Before
    public void setup() {
        testInitialisation.setupTest();
    }

    @After
    public void teardown() {
        testInitialisation.teardownTest();
    }

}