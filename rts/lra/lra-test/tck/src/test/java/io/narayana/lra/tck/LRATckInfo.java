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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.lra.tck.LRAInfo;

import io.narayana.lra.client.NarayanaLRAInfo;

/**
 * Class delegating the functionality to {@link NarayanaLRAInfo}
 * while implementing the LRA TCK interface {@link LRAInfo}.
 */
public class LRATckInfo implements LRAInfo {
    private final NarayanaLRAInfo narayanaLraInfoInstance;

    public static LRAInfo of(NarayanaLRAInfo narayanaLraInfoRecord) {
        if (narayanaLraInfoRecord == null) {
            throw new NullPointerException("narayanaLraInfo");
        }
        return new LRATckInfo(narayanaLraInfoRecord);
    }

    public static List<LRAInfo> of(List<NarayanaLRAInfo> narayanaLraInfos) {
        if (narayanaLraInfos == null) {
            throw new NullPointerException("narayanaLraInfos");
        }
        return narayanaLraInfos.stream().map(LRATckInfo::of).collect(Collectors.toList());
    }


    private LRATckInfo(NarayanaLRAInfo narayanaLraInfo) {
        this.narayanaLraInfoInstance = narayanaLraInfo;
    }

    @Override
    public String getLraId() {
        return narayanaLraInfoInstance.getLraId();
    }

    @Override
    public String getClientId() {
        return narayanaLraInfoInstance.getClientId();
    }

    @Override
    public boolean isComplete() {
        return narayanaLraInfoInstance.isComplete();
    }

    @Override
    public boolean isCompensated() {
        return narayanaLraInfoInstance.isCompensated();
    }

    @Override
    public boolean isRecovering() {
        return narayanaLraInfoInstance.isRecovering();
    }

    @Override
    public boolean isActive() {
        return narayanaLraInfoInstance.isActive();
    }

    @Override
    public boolean isTopLevel() {
        return narayanaLraInfoInstance.isTopLevel();
    }

}
