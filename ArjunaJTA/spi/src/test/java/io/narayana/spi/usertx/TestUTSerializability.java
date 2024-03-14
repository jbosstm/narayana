/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package io.narayana.spi.usertx;

import org.jboss.tm.usertx.UserTransactionRegistry;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.junit.Test;

import jakarta.transaction.*;
import java.io.*;

import static org.junit.Assert.*;

/**
 * Note that the majority of the spi tests are in the narayana repo (https://github.com/jbosstm/narayana.git)
 */
public class TestUTSerializability {
    @Test
    public void serializeUTTest() throws IOException, ClassNotFoundException, SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        ServerVMClientUserTransaction ut;
        final boolean[] utCallback = {false};

        assertNotNull(tm);
        ut = new ServerVMClientUserTransaction(tm); //ServerVMClientUserTransaction.getSingleton();

        UserTransactionRegistry utr = new UserTransactionRegistry();
        ServerVMClientUserTransaction.UserTransactionStartedListener utl = new ServerVMClientUserTransaction.UserTransactionStartedListener() {
            public void userTransactionStarted ()throws SystemException {
                utCallback[0] = true;
            }
        };

        ut.setTransactionRegistry(utr);

        ut.registerTxStartedListener(utl);

        ut.begin();
        assertTrue(utCallback[0]);
        ut.commit();

        // validate that ut can be serialized
        byte[] serializedForm = serialize(ut);

        // validate that ut can be deserialized
        ServerVMClientUserTransaction ut2 = (ServerVMClientUserTransaction) deserialize(serializedForm);

        // validate that the deserialized form is the same as the original
        assertEquals(ut, ut2);
    }

    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = null; // no try with resources for the supported lang level (1.6)
        ObjectOutput out = null;

        try {
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);

            out.writeObject(object);

            return bos.toByteArray();
        } finally {
            try {
                if (bos != null)
                    bos.close();
            } catch (IOException ignore) {
            }

            try {
                if (out != null)
                    out.close();
            } catch (IOException ignore) {
            }
        }
    }

    private Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = null; // no try with resources for the supported lang level (1.6)
        ObjectInput in = null;

        try {
            bis = new ByteArrayInputStream(bytes);
            in = new ObjectInputStream(bis);

            return in.readObject();
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException ignore) {
            }

            try {
                if (in != null)
                    in.close();
            } catch (IOException ignore) {
            }
        }
    }
}
