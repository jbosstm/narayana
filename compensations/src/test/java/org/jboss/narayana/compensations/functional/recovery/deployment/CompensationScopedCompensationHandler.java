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
import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationScopedCompensationHandler implements CompensationHandler, Serializable {

    private static final Logger LOGGER = Logger.getLogger(CompensationScopedCompensationHandler.class);

    @Inject
    private CompensationScopedData data;

    @Override
    public void compensate() {
        Objects.requireNonNull(data, "Compensation scoped data cannot be null");
        Objects.requireNonNull(data.getData(), "Compensation scoped data cannot be null");
        LOGGER.info("compensating data=" + data);
        ResultCollector.getInstance().compensate(data.getData());
    }

}
