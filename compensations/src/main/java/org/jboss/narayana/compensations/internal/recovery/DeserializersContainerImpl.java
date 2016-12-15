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

package org.jboss.narayana.compensations.internal.recovery;

import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.Deserializer;
import org.jboss.narayana.compensations.api.DeserializersContainer;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DeserializersContainerImpl implements DeserializersContainer {

    private static final DeserializersContainerImpl INSTANCE = new DeserializersContainerImpl(
            CompensationContextStateManager.getInstance());

    private static final Logger LOGGER = Logger.getLogger(DeserializersContainerImpl.class);

    private final Set<Deserializer> deserializers = new HashSet<>();

    private final CompensationContextStateManager compensationContextStateManager;

    private DeserializersContainerImpl(CompensationContextStateManager compensationContextStateManager) {
        this.compensationContextStateManager = compensationContextStateManager;
    }

    public static DeserializersContainerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void addDeserializer(Deserializer deserializer) {
        LOGGER.tracef("Add deserializer deserializer='%s'", deserializer);
        deserializers.add(deserializer);
        compensationContextStateManager.restore();
    }

    public Set<Deserializer> getDeserializers() {
        LOGGER.tracef("Get deserializers='%s'", deserializers);
        return Collections.unmodifiableSet(deserializers);
    }

}
