package com.arjuna.webservices11.wsarj.processor;

import com.arjuna.webservices.base.processors.BaseProcessor;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;

/**
 * Utility class handling common response functionality.
 * @author kevin
 */
public abstract class BaseNotificationProcessor extends BaseProcessor
{
    /**
     * Get the callback ids.
     * @param arjunaContext The arjuna context.
     * @return The callback ids.
     */
    protected String[] getIDs(final ArjunaContext arjunaContext)
    {
        if (arjunaContext != null)
        {
            final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier() ;
            if (instanceIdentifier != null)
            {
                return new String[] {instanceIdentifier.getInstanceIdentifier()} ;
            }
        }
        return null ;
    }
}
