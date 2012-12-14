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

package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst11.BAParticipantManager;

import java.util.HashMap;
import java.util.Map;


public class WSBAParticipantRegistry {

    protected static final Map<String, Map<Class, BAParticipantManager>> participantMap = new HashMap<String, Map<Class, BAParticipantManager>>();

    public WSBAParticipantRegistry() {

    }

    public void register(String txid, Class participant, BAParticipantManager baParticipantManager) {

        synchronized (participantMap) {
            if (isRegistered(txid, participant)) {
                return;
            }

            Map<Class, BAParticipantManager> baParticipantManagerMap = participantMap.get(txid);

            if (baParticipantManagerMap == null) {
                baParticipantManagerMap = new HashMap<Class, BAParticipantManager>();
                participantMap.put(txid, baParticipantManagerMap);
            }

            if (baParticipantManagerMap.get(participant) == null) {
                baParticipantManagerMap.put(participant, baParticipantManager);
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
            Map<Class, BAParticipantManager> baParticipantManagerMap = participantMap.get(txid);
            return baParticipantManagerMap != null && baParticipantManagerMap.containsKey(participant);
        }
    }

    public BAParticipantManager lookupBAParticipantManager(String txid, Class participantClass) {

        synchronized (participantMap) {
            Map<Class, BAParticipantManager> baParticipantManagerMap = participantMap.get(txid);
            if (baParticipantManagerMap != null && baParticipantManagerMap.containsKey(participantClass)) {
                return baParticipantManagerMap.get(participantClass);
            }
            return null;
        }
    }
}
