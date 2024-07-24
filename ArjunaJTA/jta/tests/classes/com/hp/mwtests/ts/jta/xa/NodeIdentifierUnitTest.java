/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.xa;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.xa.XidImple;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NodeIdentifierUnitTest
{
    // TxControl initialises the node id in a static initializer so use just a test
    @BeforeClass
    public static void before() throws NoSuchAlgorithmException {
        String random = UUID.randomUUID().toString(); // String form of a random UUID

        assertEquals(36, random.length()); // a UUID consists of 32 hex digits along with 4 “-” symbols

        setNodeIdentifier(random); // set nodeIdentifier in the config bean
    }

    @Test
    public void testNodeIdentifier() {
        XidImple xid = new XidImple(new Uid()); // the Xid will encode the nodeName plus other data

        byte[] nodeNameConfiguredBytes = arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifierBytes();
        byte[] xaNodeNameBytes = XAUtils.getXANodeNameBytes(xid);

        String nodeName = arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier();
        String nodeNameFromBytes = new String(nodeNameConfiguredBytes); // nodeName from the config bean
        String xaNodeName = XAUtils.getXANodeName(xid); // extract the node id from a Xid
        String tmNodeName = TxControl.getXANodeName(); // TxControl's view of the node id
        String xaNodeNameFromBytes = new String(xaNodeNameBytes); // TxControl's view of the node id bytes

        assertNotNull(nodeName); // can't be null (see the setNodeIdentifier method)
        assertTrue(nodeName.length() <= TxControl.NODE_NAME_SIZE);

        // they all should agree
        assertEquals(nodeName, nodeNameFromBytes);
        assertEquals(nodeName, xaNodeName);
        assertEquals(xaNodeName, tmNodeName);
        assertEquals(tmNodeName, xaNodeNameFromBytes);
    }

    /**
     * setNodeIdentifier uses an SHA-224 message digest ie it produces a
     * "secure one-way hash function that takes arbitrary-sized data and outputs a fixed-length hash value".
     * The fixed-length hash value is then used to produce a byte array and the resulting digest is used to configure
     * the node identifier bytes. These bytes are then used to set the string form of the node identifier using a UTF_8
     * encoding, but this encoding can still produce a string that is longer than
     * {@link com.arjuna.ats.arjuna.common.CoreEnvironmentBean#NODE_NAME_SIZE} and if this happens then the method
     * {@link com.arjuna.ats.arjuna.common.CoreEnvironmentBean#setNodeIdentifier(byte[])} will use a Base64 Encoder
     * to truncate it down to the maximum size. This test verifies the encoding works correctly by running the
     * {@link NodeIdentifierUnitTest#setNodeIdentifier(String)} method many times. Without the encoding the
     * test fails approximately 1 in 100 times.
     */
    @Test
    public void testBase64Encoding() throws NoSuchAlgorithmException {
        for (int i = 1; i < 1000; i++) {
            setNodeIdentifier(UUID.randomUUID().toString());
        }
    }

    private static void setNodeIdentifier(String nodeIdentifier) throws NoSuchAlgorithmException {
        byte[] nodeIdentifierAsBytes = nodeIdentifier.getBytes();
        MessageDigest messageDigest224 = MessageDigest.getInstance("SHA-224");
        byte[] digest = messageDigest224.digest(nodeIdentifierAsBytes);

        try {
            arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier(digest);
        } catch (CoreEnvironmentBeanException e) {
            fail("unable to set nodeIdentifier: " + e.getMessage());
        }
    }
}