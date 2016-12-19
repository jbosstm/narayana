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

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ResultCollector {

    private static final ResultCollector INSTANCE = new ResultCollector();

    private static final Logger LOGGER = Logger.getLogger(ResultCollector.class);

    private final List<String> compensatedData = new LinkedList<>();

    private final List<String> confirmedData = new LinkedList<>();

    public static ResultCollector getInstance() {
        return INSTANCE;
    }

    private ResultCollector() {
    }

    public void compensate(String data) {
        LOGGER.info("compensating data=" + data);
        compensatedData.add(data);
    }

    public void confirm(String data) {
        LOGGER.info("confirming data=" + data);
        confirmedData.add(data);
    }

    public Result getResult() {
        Result result = Result.builder().compensatedDate(compensatedData).confirmedData(confirmedData).build();
        LOGGER.info("returning result=" + result);
        return result;
    }

    public void reset() {
        LOGGER.info("resetting");
        compensatedData.clear();
        confirmedData.clear();
    }

}
