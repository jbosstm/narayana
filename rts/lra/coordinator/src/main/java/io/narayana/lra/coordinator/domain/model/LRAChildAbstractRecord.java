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
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * record stored with a nested LRA to inform the parent about
 * the outcome of the nested LRA
 */
public class LRAChildAbstractRecord extends AbstractRecord {
    private LRAParentAbstractRecord par;

    public LRAChildAbstractRecord() {
        super();
        par = null;
    }

    public LRAChildAbstractRecord(LRAParentAbstractRecord par) {
        super(new Uid());
        this.par = par;
    }

    private static int getTypeId() {
        return RecordType.LRA_CHILD_RECORD;
    }

    @Override
    public boolean save_state(OutputObjectState os, int i) {
        boolean saved = super.save_state(os, i);
        if (saved) {
            return par.save_state(os, i);
        }
        return false;
    }

    @Override
    public boolean restore_state(InputObjectState os, int i) {
        if (super.restore_state(os, i)) {
            par = new LRAParentAbstractRecord();
            if (par.restore_state(os, i))
                return true;
        }
        par = null;
        return false;
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

    /*
     * During topLevelCommit, this instance will call par.childCommitted()
     * Otherwise the PAR we registered with the parent will always no-op the methods.
     */
    public boolean doSave() {
        return true;
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
        return TwoPhaseOutcome.PREPARE_OK;
    }

    @Override
    public int topLevelAbort() {
        return par.getChildLRA().finishLRA(true);
    }

    @Override
    public int topLevelCommit() {
        par.childCommitted();
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
