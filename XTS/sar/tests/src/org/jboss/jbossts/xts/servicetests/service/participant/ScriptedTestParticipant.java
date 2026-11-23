package org.jboss.jbossts.xts.servicetests.service.participant;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Generic participant class which enables scripting of participant behaviour via a list of commands
 */

public class ScriptedTestParticipant implements Serializable {

    protected String id;
    protected List<String> commands;

    // constructor for recovery only
    protected ScriptedTestParticipant()
    {
    }

    protected ScriptedTestParticipant(String id)
    {
        this.id = id;
        commands = new ArrayList<String>();
    }

    public void addCommand(String command)
    {
        commands.add(command);
    }

    public String getId()
    {
        return id;
    }
}
