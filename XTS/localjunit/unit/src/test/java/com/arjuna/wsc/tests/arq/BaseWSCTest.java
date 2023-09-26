/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wsc.tests.arq;

import org.junit.After;
import org.junit.Before;

import com.arjuna.wsc.tests.TestInitialisation;

/**
 * @author paul.robinson@redhat.com 11/01/2013
 */
public class BaseWSCTest {

    @Before
    public void setup() {
        TestInitialisation.testSetup();
    }

    @After
    public void teardown() {
        TestInitialisation.testTeardown();
    }

}