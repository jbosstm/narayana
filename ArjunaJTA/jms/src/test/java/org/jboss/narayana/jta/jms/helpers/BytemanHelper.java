/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
