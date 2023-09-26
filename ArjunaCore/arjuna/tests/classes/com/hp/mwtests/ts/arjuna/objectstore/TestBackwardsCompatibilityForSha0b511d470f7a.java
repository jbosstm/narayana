/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Verify that a log created by the code prior to the fix for JBTM-3262 can be read by later code
 */
public class TestBackwardsCompatibilityForSha0b511d470f7a {
    // define a unique path for the test log
    private String RECORD_TYPE = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/JBTM-3262";
    // define a system property for controlling a code path that will generate a log record
    private static final String CREATE_RECORD_SYS_PROP = "JBTM3262";

    @Before
    public void beforeMethod() {
        // this test only applies to filesystem based stores
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(ShadowNoFileLockStore.class.getName());

        assumeTrue("Skipping test since it only applies to stores of type FileSystemStore",
                StoreManager.getCommunicationStore() instanceof FileSystemStore);
    }

    @Test
    public void test() throws ObjectStoreException {
        Uid uid = new Uid("0:ffffc0a80021:8ced:5e68d2a1:0");
        String stringData = "JBTM-3262 test string"; // "0xdeedbaaf OF£$£ v%^";
        int intData = 0xdeedbaaf;

        try {
            if (Boolean.getBoolean(CREATE_RECORD_SYS_PROP)) {
                // write a log record
                OutputObjectState state = new OutputObjectState();

                state.packInt(intData);
                state.packString(stringData);
                state.packInt(intData);

                StoreManager.getCommunicationStore().write_committed(uid, RECORD_TYPE, state);
                // The log just written should be manually copied to the resources directory
                // (src/test/resources).
                // The else part of the statement will then verify that it can be read back.
                // The idea is to run the else part on a later revision of the code to ensure
                // backwards compatibility of the arjuna log read/write code
            } else {
                // copy the record from the classpath to the log store
                copyJBTMLogToLogStore(uid);

                // read the log record
                InputObjectState ios = StoreManager.getCommunicationStore().read_committed(uid, RECORD_TYPE);

                assertNotNull("could not read back JBTM-3262 record", ios);
                assertEquals("read back a different state", intData, ios.unpackInt());
                assertEquals("read back a different state", stringData, ios.unpackString());
                assertEquals("read back a different state", intData, ios.unpackInt());
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * The project resources directory contains a log created by pre JBTM-3262 code,
     * copy it into the object store to see if the current arjuna code reads it correctly
     *
     * @param uid the uid of a log on the classpath which should be copied over to the object store
     * @throws IOException if the file cannot be copied
     */
    private void copyJBTMLogToLogStore(Uid uid) throws IOException {
        // filename corresponding to the uid of the record
        String logName = uid.fileStringForm();
        // the object store API
        ObjectStore os = ((ObjectStore) StoreManager.getCommunicationStore());
        // the full file system path to the log record
        Path logContainer = Paths.get(os.storeDir(), os.storeRoot(), RECORD_TYPE); // valid on linux and windows
        // read a resource representing a log from the classpath
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(logName)) {

            assertNotNull(inputStream);

            // and then copy inputStream to the log store located at logContainer
            Path logPath = Paths.get(logContainer.toString(), logName);
            Files.createDirectories(logPath.getParent());
            Files.copy(inputStream, logPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}