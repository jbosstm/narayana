/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

public class CSTest extends CSControl {

    // private static CSControl control = new CSControl();

    // XsdValidator is not thread safe
    public void test_211() {
        runServer("211");
        runTest("211");
    }

    // tpcall incorrectly returns TPNOTIME whenever the TPNOBLOCK or TPNOTIME
    // flags are specified
    public void test_2120() {
        runServer("2120");
        runTest("2120");
    }

    // Similarly specifying TPNOBLOCK means that if a blocking condition does
    // exist then the caller
    // should get the error TPEBLOCK
    public void test_2121() {
        runServer("2121");
        runTest("2121");
    }

    // tpcall should return TPEINVAL if the service name is invalid
    public void test_213() {
        log.info("test_213");
        if (isSunOS) {
            log.info("skipping test_213");
        } else {
            runServer("213");
            runTest("213");
        }
    }

    // TPSIGRSTRT flag isn't supported on tpcall
    public void test_214() {
        runServer("214");
        runTest("214");
    }

    // tpcall failure with multiple threads
    public void test_215() {
        runServer("215");
        runTest("215");
    }

    // tp bufs should morph if they're the wrong type
    public void test_2160() {
        runServer("2160");
        runTest("2160");
    }

    // passing the wrong return buffer type with TPNOCHANGE
    public void test_2161() {
        runServer("2161");
        runTest("2161");
    }

    // make sure tpurcode works
    public void test_217() {
        runServer("217");
        runTest("217");
    }

    // sanity check
    public void test_0() {
        runServer("0");
        runTest("0");
    }

    // tell the server to set a flag on tpreturn (should generate TPESVCERR)
    public void test_1() {
        runServer("1");
        runTest("1");
    }

    // set flag on tpreturn should fail
    public void test_2() {
        runServer("2");
        runTest("2");
    }

    // telling the service to not tpreturn should generate an error
    public void test_3() {
        runServer("3");
        runTest("3");
    }

    // telling service to call tpreturn outside service routine should have no
    // effect
    public void test_4() {
        if (isSunOS) {
            log.info("Skipping test_4");
        } else {
            runServer("4");
            runTest("4");
        }
    }

    // tpreturn outside service routing
    public void test_5() {
        runServer("5");
        runTest("5");
    }

    // test tpcall with buffer size larger than a network buffer
    public void test_9() {
        runServer("9");
        runTest("9");
    }
}
