package org.jboss.jbossts.qa.astests.ASCrashRecovery01;

import org.jboss.jbossts.qa.astests.taskdefs.ClientAction;
import org.jboss.jbossts.qa.astests.taskdefs.ASTestConfig;


import java.util.Map;

public class Test02 implements ClientAction
{

    public boolean execute(ASTestConfig config, Map<String, String> params)
    {
        return true;
    }

    public boolean cancel() throws UnsupportedOperationException
    {
        return true;
    }
}
