/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsaddr.processor;

import com.arjuna.webservices.base.processors.BaseProcessor;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPRelatesTo;

/**
 * Utility class handling common response functionality.
 * @author kevin
 */
public abstract class BaseWSAddrResponseProcessor extends BaseProcessor
{
    /**
     * Get the callback ids.
     * @param map The addressing context.
     * @return The callback ids.
     */
    protected String[] getIDs(final MAP map)
    {
        final MAPRelatesTo relatesTo = map.getRelatesTo() ;
        if (relatesTo != null)
        {
            final String[] ids = new String[1] ;
            ids[0] = relatesTo.getRelatesTo();
            return ids ;
        }
        return null ;
    }
}