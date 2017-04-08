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

package org.jboss.narayana.compensations.functional.recovery.deployment;

import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.Deserializer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Optional;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestDeserializer implements Deserializer {

    private static final Logger LOGGER = Logger.getLogger(TestDeserializer.class);

    @Override
    public boolean canDeserialize(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> deserialize(ObjectInputStream objectInputStream, Class<T> clazz) {
        Object object;
        try {
            object = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            LOGGER.warnf(e, "Failed to deserialize an object");
            return Optional.empty();
        }

        if (clazz.isAssignableFrom(object.getClass())) {
            LOGGER.tracef("Deserialized an object: '%s'", object);
            return Optional.of((T) object);
        }

        LOGGER.tracef("Object was not deserialized");

        return Optional.empty();
    }

}
