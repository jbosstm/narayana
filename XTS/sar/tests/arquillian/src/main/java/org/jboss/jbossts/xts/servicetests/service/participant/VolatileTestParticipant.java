package org.jboss.jbossts.xts.servicetests.service.participant;

import com.arjuna.wst.*;


/**
 * A scriptable non-durable participant for use by the XTSServiceTest service.
 */
@SuppressWarnings("serial")
public class VolatileTestParticipant
    extends ScriptedTestParticipant
        implements Volatile2PCParticipant
{
    public VolatileTestParticipant(String id)
    {
        super(id);
    }

    public void addCommand(String command)
    {
        commands.add(command);
    }

   public Vote prepare() throws WrongStateException, SystemException {
       for (String s : commands) {
           if (s.equals("prepare")) {
               commands.remove(s);
               return new Prepared();
           } else if (s.equals("prepareReadOnly")) {
               commands.remove(s);
               return new ReadOnly();
           } else if (s.equals("prepareAbort")) {
               commands.remove(s);
               return new Aborted();
           } else if (s.equals("prepareWrongStateException")) {
               commands.remove(s);
               throw new WrongStateException("DurableTestParticipant  prepare : " + id);
           } else if (s.equals("prepareSystemException")) {
               commands.remove(s);
               throw new SystemException("DurableTestParticipant prepare : " + id);
           }
       }

       // default behaviour is just to prepare

       return new Prepared();
    }

    public void commit() throws WrongStateException, SystemException {
        for (String s : commands) {
            if (s.equals("commit")) {
                commands.remove(s);
                return;
            } else if (s.equals("commitWrongStateException")) {
                commands.remove(s);
                throw new WrongStateException("DurableTestParticipant  commit : " + id);
            } else if (s.equals("commitSystemException")) {
                commands.remove(s);
                throw new SystemException("DurableTestParticipant  commit : " + id);
            }
        }

        // default behaviour is just to commit

        return;
    }

    public void rollback() throws WrongStateException, SystemException {
        for (String s : commands) {
            if (s.equals("rollback")) {
                commands.remove(s);
                return;
            } else if (s.equals("rollbackWrongStateException")) {
                commands.remove(s);
                throw new WrongStateException("DurableTestParticipant  rollback : " + id);
            } else if (s.equals("rollbackSystemException")) {
                commands.remove(s);
                throw new SystemException("DurableTestParticipant  rollback : " + id);
            }
        }

        // default behaviour is just to rollback

        return;
    }

    public void unknown() throws SystemException {
        // do nothing
    }

    public void error() throws SystemException {
        // do nothing
    }
}