/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.util.concurrent.atomic.AtomicInteger;

public class BytemanControlledRecord extends AbstractRecord {

    /* This is a counter used internally in BytemanControlledRecord to count how many
     * times the "topLevelCommit()" method was invoked.
     *
     * NOTE: AtomicInteger here is an overkill as BytemanControlledRecord is only used
     * during tests and, as a consequence, there won't be two threads calling commit in
     * parallel.
     * Also, the recovery thread won't invoke topLevelCommit() in parallel to the test thread,
     * so there isn't any risk in declaring this field as int.
     * Nevertheless, it's good practice to show that this field should be declared as an
     * AtomicInteger in a production environment as multiple threads could handle this field
     * concurrently.
     */
    private static final AtomicInteger _commitCallCounter = new AtomicInteger(0);

    public static int getCommitCallCounter() {
        return BytemanControlledRecord._commitCallCounter.get();
    }

    public static void resetCommitCallCounter() {
        BytemanControlledRecord._commitCallCounter.set(0);
    }

    /* This is a counter used internally in BytemanControlledRecord to count how many
     * times the "topLevelPrepare()" method was invoked.
     */
    private static final AtomicInteger _prepareCallCounter = new AtomicInteger(0);

    public static int getPrepareCallCounter() {
        return BytemanControlledRecord._prepareCallCounter.get();
    }

    public static void resetPrepareCallCounter() {
        BytemanControlledRecord._prepareCallCounter.set(0);
    }
    
    // Executed in the byteman script recoverySuspend.btm
    public static void resetGreenFlag() {
        
    }

    // Executed in the byteman script recoverySuspend.btm
    public static void setGreenFlag() {
        // Artificially set greenFlag to skip failures
    }

    public BytemanControlledRecord() {
    }

    public BytemanControlledRecord(boolean newRecord) {
        super(new Uid());
    }

    public int typeIs() {
        return RecordType.USER_DEF_FIRST0;
    }

    public boolean doSave() {
        return true;
    }

    public String type() {
        return "/StateManager/AbstractRecord/BytemanControlledRecord";
    }

    public boolean save_state(OutputObjectState os, int ot) {
        return super.save_state(os, ot);
    }

    public boolean restore_state(InputObjectState os, int ot) {
        return super.restore_state(os, ot);
    }

    public Object value() {
        return null;
    }

    public void setValue(Object object) {
    }

    public int nestedAbort() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int nestedCommit() {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedPrepare() {
        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public int topLevelAbort() {
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit() {
        BytemanControlledRecord._commitCallCounter.getAndIncrement();
        return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare() {
        BytemanControlledRecord._prepareCallCounter.getAndIncrement();
        return TwoPhaseOutcome.PREPARE_OK;
    }

    public void alter(AbstractRecord abstractRecord) {
    }

    public void merge(AbstractRecord abstractRecord) {
    }

    public boolean shouldAdd(AbstractRecord abstractRecord) {
        return false;
    }

    public boolean shouldAlter(AbstractRecord abstractRecord) {
        return false;
    }

    public boolean shouldMerge(AbstractRecord abstractRecord) {
        return false;
    }

    public boolean shouldReplace(AbstractRecord abstractRecord) {
        return false;
    }

}