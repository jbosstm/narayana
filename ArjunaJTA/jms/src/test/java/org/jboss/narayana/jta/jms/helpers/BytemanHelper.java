/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms.helpers;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import org.jboss.narayana.jta.jms.integration.IntegrationTestRuntimeException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class BytemanHelper {

    private static int commitsCounter;

    public static void reset() {
        commitsCounter = 0;
    }

    public void failFirstCommit(Uid uid) {
        // Increment is called first, so counter should be 1
        if (commitsCounter == 1) {
            System.out.println(BytemanHelper.class.getName() + " fail first commit");
            ActionManager.manager().remove(uid);
            ThreadActionData.popAction();
            throw new IntegrationTestRuntimeException("Failing first commit");
        }
    }

    public void incrementCommitsCounter() {
        commitsCounter++;
        System.out.println(BytemanHelper.class.getName() + " increment commits counter: " + commitsCounter);
    }

}