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
import org.jboss.narayana.compensations.api.DeserializersContainer;
import org.jboss.narayana.compensations.api.EnlistException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Startup
@Singleton
@Path(ExecutionResource.PATH)
public class ExecutionResource {

    public static final String PATH = "execution-resource";

    private static final Logger LOGGER = Logger.getLogger(ExecutionResource.class);

    @Inject
    private Executor executor;

    @Inject
    private DeserializersContainer deserializersContainer;

    @PostConstruct
    public void init() {
        LOGGER.info("registering test deserializer");
        deserializersContainer.addDeserializer(new TestDeserializer());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result getResult() {
        Result result = ResultCollector.getInstance().getResult();
        LOGGER.info("returning result=" + result);
        return result;
    }

    @DELETE
    public void reset() {
        LOGGER.info("resetting");
        ResultCollector.getInstance().reset();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void execute(Options options) throws EnlistException {
        LOGGER.info("executing with options=" + options);

        if (options.isDistributed()) {
            executor.executeInDistributedTransaction(options);
        } else {
            executor.executeInLocalTransaction(options);
        }
    }

}
