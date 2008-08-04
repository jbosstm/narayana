package org.jboss.jbossts.qa.astests.taskdefs;

import java.util.Map;

/**
 * General mechanism for running code during the execution of an ant target
 */
public interface ClientAction
{
    /**
     * Execute code in the context of an ant target
     *
     * @param config Configuration for this execution loaded from the ant script
     * that triggered this action
     * @param params Arbitary collection of name/value pairs passed in from the ant script
     * that triggered this action
     * @return true to indicate that the action completed successfully
     * @see org.jboss.jbossts.qa.astests.taskdefs.ASClientTask
     */
    boolean execute(ASTestConfig config, Map<String, String> params);

    /**
     * Cancel any executing operation
     *
     * @return true if the opeartion was cancelled
     * @throws UnsupportedOperationException Not implemented
     */
    boolean cancel() throws UnsupportedOperationException;
}
