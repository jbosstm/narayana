/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.coordinator.domain.model;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import io.narayana.lra.coordinator.internal.LRARecoveryModule;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * record stored with a parent LRA which a child LRA will use to notify
 * the parent that the child was committed.
 */
public class LRAParentAbstractRecord extends AbstractRecord {
    private boolean committed;
    private URI parentId;
    private URI childId;

    public LRAParentAbstractRecord() {
        super();
    }

    public LRAParentAbstractRecord(BasicAction parent, LongRunningAction child) {
        super(new Uid());
        // use theChild to drive B but only if B committed, so ...

        if (parent instanceof LongRunningAction) {
            parentId = ((LongRunningAction) parent).getId();
        }

        childId = child.getId();
        committed = false; // assume default as it's the safest route to take if something goes wrong
    }

    public void childCommitted() {
        committed = true;
    }

    @Override
    public boolean save_state(OutputObjectState os, int i) {
        boolean saved = super.save_state(os, i);
        if (saved) {
            try {
                os.packString(parentId.toASCIIString());
                os.packString(childId.toASCIIString());
                os.packBoolean(committed);
            } catch (IOException e) {
                return false;
            }
        }

        return saved;
    }

    @Override
    public boolean restore_state(InputObjectState os, int i) {
        boolean restored = super.restore_state(os, i);
        if (restored) {
            try {
                parentId = new URI(Objects.requireNonNull(os.unpackString()));
                childId = new URI(Objects.requireNonNull(os.unpackString()));
                committed = os.unpackBoolean();
            } catch (IOException | URISyntaxException e) {
                return false;
            }
        }

        return restored;
    }

    /*
     * So various methods, like topLevelCommit, would need to check committed and only
     * do work if the child (B) committed.
     */

    public LongRunningAction getParentLRA() {
        return LRARecoveryModule.getRM().getServiceInstance().getTransaction(parentId);
    }

    public LongRunningAction getChildLRA() {
        return LRARecoveryModule.getRM().getServiceInstance().getTransaction(childId);
    }

    private static int getTypeId() {
        return RecordType.LRA_PARENT_RECORD;
    }

    @Override
    public int typeIs() {
        return getTypeId();
    }

    @Override
    public Object value() {
        return null;
    }

    @Override
    public void setValue(Object o) {
    }

    @Override
    public int nestedAbort() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int nestedCommit() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int nestedPrepare() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int topLevelAbort() {
        return abort(getChildLRA());
    }

    private int abort(LongRunningAction lra) {
        if (lra != null && lra.finishLRA(true) != TwoPhaseOutcome.FINISH_OK) {
            return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int topLevelCommit() {
        // TODO check committed and only do work if the child (B) committed.
        LongRunningAction parent = getParentLRA();
        LongRunningAction child = getChildLRA();

        if (parent == null || child == null) {
            return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }

        if (parent.isCancel()) {
            return topLevelAbort();
        }

        if (committed) {
            // the child LRA has committed and the parent is committing so tell
            // the child it's okay to inform the participants to forget
            child.forget();
        }

        if (child.finishLRA(false) != TwoPhaseOutcome.FINISH_OK) {
            return TwoPhaseOutcome.HEURISTIC_HAZARD;
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    @Override
    public int topLevelPrepare() {
        return TwoPhaseOutcome.PREPARE_OK;
    }

    @Override
    public void merge(AbstractRecord a) {
    }

    @Override
    public void alter(AbstractRecord a) {
    }

    @Override
    public boolean shouldAdd(AbstractRecord a) {
        return false;
    }

    @Override
    public boolean shouldAlter(AbstractRecord a) {
        return false;
    }

    @Override
    public boolean shouldMerge(AbstractRecord a) {
        return false;
    }

    @Override
    public boolean shouldReplace(AbstractRecord a) {
        return false;
    }
}
