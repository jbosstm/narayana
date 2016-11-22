/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.narayana.compensations.api;

import java.io.ObjectInputStream;
import java.util.Optional;

/**
 * Application should implement this interface in order to provide us a way to recreate its handlers and compensation scoped
 * beans during the recovery.
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public interface Deserializer {

    /**
     * Method used to check if the object of specified class can be deserialized by this deserializer.
     *
     * @param className class name of the object which needs to be deserialized.
     * @return {@code true} if object can be deserialized and {@code false} otherwise.
     */
    boolean canDeserialize(String className);

    /**
     * Deserialize an object of the specified type.
     *
     * @param objectInputStream input stream containing serialized object.
     * @param clazz type of the serialized object.
     * @return {@link Optional} containing a deserialized object or empty if something went wrong.
     */
    <T> Optional<T> deserialize(ObjectInputStream objectInputStream, Class<T> clazz);

}
