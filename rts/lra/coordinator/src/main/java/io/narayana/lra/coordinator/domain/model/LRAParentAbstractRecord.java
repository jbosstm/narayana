/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
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
import io.narayana.lra.logging.LRALogger;

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
                LRALogger.i18nLogger.warn_saveState(e.getMessage());
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
                LRALogger.i18nLogger.warn_restoreState(e.getMessage());
                return false;
            }
        }

        return restored;
    }

    public LongRunningAction getParentLRA() {
        return LRARecoveryModule.getService().getTransaction(parentId);
    }

    public LongRunningAction getChildLRA() {
        return LRARecoveryModule.getService().getTransaction(childId);
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
        LongRunningAction parent = getParentLRA();
        LongRunningAction child = getChildLRA();

        if (parent == null || child == null) {
            return TwoPhaseOutcome.FINISH_ERROR;
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