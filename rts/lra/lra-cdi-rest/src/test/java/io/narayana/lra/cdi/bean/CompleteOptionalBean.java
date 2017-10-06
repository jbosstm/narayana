/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package io.narayana.lra.cdi.bean;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import io.narayana.lra.annotation.Compensate;
import io.narayana.lra.annotation.LRA;
import io.narayana.lra.annotation.Status;

/**
 * {@link LRA} bean which contains annotations - {@link Compensate} and {@link Status}
 * is enough to run the LRA 
 */
public class CompleteOptionalBean {

    @LRA
    public void process() {
        // no implementation needed
    }

    @Compensate
    @Path("compensate")
    @PUT
    public void compensate() {
        // no implementation needed
    }
    
    @Status
    @Path("status")
    @GET
    public void status() {
        // no implementation needed
    }
}
