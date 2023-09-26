/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package io.narayana.lra.coordinator.domain.model.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import io.narayana.lra.LRAData;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VolatileObjectStoreTest extends TestBase {

    @BeforeClass
    public static void start() {
        TestBase.start();
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "alt-jbossts-properties.xml");
    }

    /**
     * This test checks that a new LRA transaction can be created when
     * Narayana is configured to use a Volatile Object Store. This test
     * fails if the Object Store is not set to VolatileStore
     */
    @Test
    public void volatileStoreTest() {

        String objectStoreType = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).getObjectStoreType();
        // This test fails if the Object Store is not set to Volatile
        assertEquals("The Object Store type should have been set to VolatileStore", VolatileStore.class.getName(), objectStoreType);

        LRALogger.logger.infof("%s: the Object Store type is set to: %s",testName.getMethodName(), objectStoreType);

        // Starts a new LRA
        URI lraIdUri = lraClient.startLRA(testName.getMethodName() + "#newLRA");
        // Checks that the LRA transaction has been created
        assertNotNull("An LRA should have been added to the object store", lraIdUri);
        // Using NarayanaLRAClient, the following statement checks that the status of the new LRA is active
        assertEquals("Expected Active", LRAStatus.Active, getStatus(lraIdUri));

        // Extracts the id from the URI
        String lraId = convertLraUriToString(lraIdUri).replace('_', ':');

        LRAData lraData = getLastCreatedLRA();
        assertEquals("Expected that the LRA transaction just started matches the LRA transaction fetched through the Narayana LRA client",
                lraData.getLraId(),
                lraIdUri);
    }
}
