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