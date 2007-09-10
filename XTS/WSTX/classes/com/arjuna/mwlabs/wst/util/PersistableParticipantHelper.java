/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
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
     * @message com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_1 [com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_1] - Participant not persistable.
     * @message com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_2 [com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_2] - Error persisting participant.
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
                    wstxLogger.arjLoggerI18N.error("com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_1") ;
                    return false ;
                }
                return true ;
            }
            catch(final Throwable th)
            {
                wstxLogger.arjLoggerI18N.error("com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_2", th) ;
                return false ;
            }
        }
        else
            return false;
    }

    /**
     * Restore the resource state.
     * @param is The input object stream.
     * @return The resource if successful, null otherwise.
     * @message com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_3 [com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_3] - Error restoring participant.
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
        catch (final Throwable th)
        {
            wstxLogger.arjLoggerI18N.error("com.arjuna.mwlabs.wst.util.PersistableParticipantHelper_3", th) ;
            return null ;
        }
    }

}
