package org.jboss.narayana.txframework.impl.handlers.wsat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WSATParticipantRegistry
{
    private static final Map<String, List<Class>> participantMap = new HashMap<String, List<Class>>();

    public WSATParticipantRegistry()
    {
    }

     public void register(String txid, Class participant)
    {
        synchronized (participantMap)
        {
            if (isRegistered(txid, participant))
            {
                return;
            }

            List<Class> participantList = participantMap.get(txid);

            if (participantList == null)
            {
                participantList = new ArrayList<Class>();
                participantMap.put(txid, participantList);
            }

            synchronized (participantMap.get(txid))
            {
                if (!participantList.contains(participant))
                {
                    participantList.add(participant);
                }
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
            List<Class> participantList = participantMap.get(txid);
            return participantList != null && participantList.contains(participant);
        }
    }
}