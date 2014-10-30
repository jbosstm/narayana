/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package com.arjuna.ats.jts;

import org.omg.CORBA.Any;

public interface PICurrentSlotAccessorInterface {

    /**
     * save the received data slot
     * @return the current data in the PICurrent receive slot
     */
    Any getData();

    /**
     * restore the received data slot
     * @param any the data to put into the PICurrent receive slot
     */
    void putData(Any any);

    /**
     * get the current data in a PICurrent slot
     * @param slotId the slot id
     * @return the data in a PICurrent slot
     */
    Any getData(int slotId);

    /**
     * put data into a PICurrent slot
     * @param slotId the slot id
     * @param any the data to put into a PICurrent receive slot
     */
    void putData(int slotId, Any any);
}
