/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mwlabs.wst.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.util.ClassLoaderHelper;
import com.arjuna.wst.PersistableParticipant;

/**
 * Helper class for persisting participants.
 * @author kevin
 */
public class PersistableParticipantHelper
{

    /**
     * Save the resource state.
     * @param os The output object stream.
     * @param resource The resource to persist.
     * @return true if successful, false otherwise.
     */
    public static boolean save_state(final OutputObjectState os, final Object resource)
    {
        if (resource != null)
        {
            try
            {
                if (resource instanceof Serializable)
                {
                    os.packBoolean(true) ;
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
                    final ObjectOutputStream oos = new ObjectOutputStream(baos) ;
                    oos.writeObject(resource) ;
                    oos.flush() ;
                    os.packBytes(baos.toByteArray()) ;
                }
                else if (resource instanceof PersistableParticipant)
                {
                    final PersistableParticipant participant = (PersistableParticipant)resource ;
                    os.packBoolean(false) ;
                    os.packString(resource.getClass().getName()) ;
                    participant.saveState(os) ;
                }
                else
                {
                    wstxLogger.i18NLogger.error_mwlabs_wst_util_PersistableParticipantHelper_1();
                    return false ;
                }
                return true ;
            }
            catch(final Throwable th) {
                wstxLogger.i18NLogger.error_mwlabs_wst_util_PersistableParticipantHelper_2(th);
                return false;
            }
        }
        else
            return false;
    }

    /**
     * Restore the resource state.
     * @param is The input object stream.
     * @return The resource if successful, null otherwise.
     */
    public static Object restore_state(final InputObjectState ios)
    {
        try
        {
            final boolean serializable = ios.unpackBoolean() ;
            if (serializable)
            {
                final ByteArrayInputStream bais = new ByteArrayInputStream(ios.unpackBytes()) ;
                final ObjectInputStream ois = new ObjectInputStream(bais) ;
                return ois.readObject() ;
            }
            else
            {
                final String className = ios.unpackString() ;
                final Class resourceClass = ClassLoaderHelper.forName(PersistableParticipantHelper.class, className) ; // returns Class not instance
                final Object resource = resourceClass.newInstance();
                ((PersistableParticipant)resource).restoreState(ios) ;
                return resource ;
            }
        }
        catch (final Throwable th) {
            wstxLogger.i18NLogger.error_mwlabs_wst_util_PersistableParticipantHelper_3(th);
            return null;
        }
    }

}