/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.base.processors;

/**
 * A specialization of ActivatedObjectProcessor which allows for ghost entries to
 * be left in the table after deletion. A ghost entry cannot be retrieved by a normal
 * getObject(id) which will return null, indicating that no object with the supplied id
 * exists. However, the ghost's presence can be detected using getGhost(id).</p>
 *
 * Ghost entries are used to identify objects which have failed to be terminated due to an
 * unavailable participant or coordinator and so are still present in an unprocessed log record.
 * When recovery processing recreates a participant the recovered instance replaces the
 * ghost entry, ensuring that sbsequent messages update the participant whose recovery is
 * being driven by the coordinator.
 */

public class ReactivatedObjectProcessor extends ActivatedObjectProcessor {

    /**
     * a private object used to identify a ghost entry
     */

    static final private Object tombstone = new Object();

    /**
     * Activate the object.
     *
     * @param object     The object.
     * @param identifier The identifier.
     */
    public synchronized void activateObject(Object object, String identifier) {
        super.activateObject(object, identifier);
    }

    /**
     * Deactivate the object.
     *
     * @param object The object.
     */
    public synchronized void deactivateObject(Object object) {
        deactivateObject(object, false);
    }

    /**
     * Deactivate the object.
     *
     * @param object The object.
     */
    public synchronized void deactivateObject(Object object, boolean leaveGhost) {
        if (leaveGhost) {
            final String identifier = (String)identifierMap.get(object);
            super.deactivateObject(object);
            objectMap.put(identifier, tombstone);
        } else {
            super.deactivateObject(object);
        }
    }

    /**
     * Get the object with the specified identifier.
     *
     * @param identifier The identifier.
     * @return The participant or null if not known.
     */
    public synchronized Object getObject(String identifier) {
        final Object object = super.getObject(identifier);

        if (object == tombstone) {
            return  null;
        }

        return object;
    }

    /**
     * check if there is a ghost entry for this object
     *
     * @param identifier
     * @return true iff there is a ghost entry for this object
     */
    public synchronized boolean getGhost(String identifier)
    {
        if (reactivationProcessingStarted) {
            final Object object = super.getObject(identifier);
            return (object == tombstone);
        } else {
            // until we have been notified of at least one complete recovery scan pass we have
            // to assume that any identifier may have an entry in the log so we return true
            return true;
        }
    }

    /**
     * a global flag which is false at boot and is set to true once a recovery log scan for XTS
     * data has completed
     */
    static boolean reactivationProcessingStarted = false;

    /**
     * notify completion of a recovery log scan for XTS data
     */

    static public void setReactivationProcessingStarted()
    {
        reactivationProcessingStarted = true;
    }
}
