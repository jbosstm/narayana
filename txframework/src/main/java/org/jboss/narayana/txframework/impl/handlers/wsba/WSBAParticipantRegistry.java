package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst11.BAParticipantManager;

import java.util.HashMap;
import java.util.Map;


public class WSBAParticipantRegistry
{
    protected static final Map<String, Map<Class, BAParticipantManager>> participantMap = new HashMap<String, Map<Class, BAParticipantManager>>();

    public WSBAParticipantRegistry()
    {
    }

    public void register(String txid, Class participant, BAParticipantManager baParticipantManager)
    {
        synchronized (participantMap)
        {
            if (isRegistered(txid, participant))
            {
                return;
            }

            Map<Class, BAParticipantManager> baParticipantManagerMap = participantMap.get(txid);

            if (baParticipantManagerMap == null)
            {
                baParticipantManagerMap = new HashMap<Class, BAParticipantManager>();
                participantMap.put(txid, baParticipantManagerMap);
            }

            if (baParticipantManagerMap.get(participant) == null)
            {
                baParticipantManagerMap.put(participant, baParticipantManager);
            }
        }
    }

    public void forget(String txid)
    {
        synchronized (participantMap)
        {
            participantMap.remove(txid);
        }
    }

    public boolean isRegistered(String txid, Class participant)
    {
        synchronized (participantMap)
        {
            Map<Class, BAParticipantManager> baParticipantManagerMap = participantMap.get(txid);
            return baParticipantManagerMap != null && baParticipantManagerMap.containsKey(participant);
        }
    }

    public BAParticipantManager lookupBAParticipantManager(String txid, Class participantClass)
    {
        synchronized (participantMap)
        {
            Map<Class, BAParticipantManager> baParticipantManagerMap = participantMap.get(txid);
            if(baParticipantManagerMap != null && baParticipantManagerMap.containsKey(participantClass))
            {
                return baParticipantManagerMap.get(participantClass);
            }
            return null;
        }
    }
}
