/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2020,
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.io.IOException;
import java.util.Objects;

/**
 * A immutable POJO used as a unique key for records in the SlotStore.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-03
 */
public class SlotStoreKey {

    private final Uid uid;
    private final String typeName;
    private final int stateStatus;

    private final int hashcode;

    public SlotStoreKey(Uid uid, String typeName, int stateStatus) {

        if (!typeName.startsWith("/")) {
            typeName = "/" + typeName;
        }

        this.uid = (uid == null ? Uid.nullUid() : uid);
        this.typeName = typeName;
        this.stateStatus = stateStatus;

        this.hashcode = Objects.requireNonNull(uid).hashCode();

    }

    public Uid getUid() {
        return uid;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getStateStatus() {
        return stateStatus;
    }

    public void packInto(OutputBuffer outputBuffer) throws IOException {
        UidHelper.packInto(uid, outputBuffer);
        outputBuffer.packString(typeName);
        outputBuffer.packInt(stateStatus);
    }

    public static SlotStoreKey unpackFrom(InputBuffer inputBuffer) throws IOException {
        Uid uid = UidHelper.unpackFrom(inputBuffer);
        String typeName = inputBuffer.unpackString();
        int stateStatus = inputBuffer.unpackInt();
        return new SlotStoreKey(uid, typeName, stateStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotStoreKey key = (SlotStoreKey) o;
        return stateStatus == key.stateStatus &&
                Objects.equals(uid, key.uid) &&
                Objects.equals(typeName, key.typeName);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }
}
