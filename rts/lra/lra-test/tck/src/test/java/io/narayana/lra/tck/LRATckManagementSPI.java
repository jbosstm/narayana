/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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

package io.narayana.lra.tck;

import java.net.URL;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.eclipse.microprofile.lra.tck.LRAInfo;
import org.eclipse.microprofile.lra.tck.spi.ManagementSPI;

import io.narayana.lra.client.NarayanaLRAClient;

@Dependent
public class LRATckManagementSPI implements ManagementSPI {

    @Inject
    NarayanaLRAClient narayanaLraClient;

    @Override
    public LRAInfo getStatus(URL lraId) throws NotFoundException {
        return LRATckInfo.of(narayanaLraClient.getLRAInfo(lraId));
    }

    @Override
    public List<LRAInfo> getLRAs(LRAStatus status) {
        return LRATckInfo.of(narayanaLraClient.getAllLRAs());
    }

}
