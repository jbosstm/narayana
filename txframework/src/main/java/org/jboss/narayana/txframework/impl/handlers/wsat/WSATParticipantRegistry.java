/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.impl.handlers.wsat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WSATParticipantRegistry {

    private static final Map<String, List<Class>> participantMap = new HashMap<String, List<Class>>();

    public WSATParticipantRegistry() {

    }

    public void register(String txid, Class participant) {

        synchronized (participantMap) {
            if (isRegistered(txid, participant)) {
                return;
            }

            List<Class> participantList = participantMap.get(txid);

            if (participantList == null) {
                participantList = new ArrayList<Class>();
                participantMap.put(txid, participantList);
            }

            if (!participantList.contains(participant)) {
                participantList.add(participant);
            }
        }
    }

    public void forget(String txid) {

        synchronized (participantMap) {
            participantMap.remove(txid);
        }
    }

    public boolean isRegistered(String txid, Class participant) {

        synchronized (participantMap) {
            List<Class> participantList = participantMap.get(txid);
            return participantList != null && participantList.contains(participant);
        }
    }
}