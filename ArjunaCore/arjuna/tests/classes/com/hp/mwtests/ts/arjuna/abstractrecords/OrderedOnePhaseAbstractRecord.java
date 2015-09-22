/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.abstractrecords;

import java.io.IOException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

public class OrderedOnePhaseAbstractRecord extends AbstractRecord {

    Uid order;
    private boolean fail;

    /**
     * Used for recovery
     */
    public OrderedOnePhaseAbstractRecord()
    {
        super();
    }

    public OrderedOnePhaseAbstractRecord(Uid uid)
    {
        super(uid);
        order = uid;
    }

    public static int typeId() {
        return RecordType.USER_DEF_FIRST0;
    }

    public int typeIs()
    {
        return typeId();
    }

    public int topLevelPrepare()
    {
        // Maybe do a Hibernate flush or something to check constraints if you were wrapping hibernate
        return TwoPhaseOutcome.PREPARE_OK;
    }

    public void causeTransientFailure() {
        fail = true;
    }

    public int topLevelCommit()
    {
        System.out.println("topLevelCommit: " + order);
        if (!fail)
            return TwoPhaseOutcome.FINISH_OK;
        else
            return TwoPhaseOutcome.FINISH_ERROR;
    }

    /**
     * You would commit the 1PC connection here
     */
    public int topLevelOnePhaseCommit()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * You would rollback the 1PC connection here
     */
    public int topLevelAbort()
    {
        return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * Save inline in the BasicAction
     */
    public boolean doSave()
    {
        return true;
    }

    /**
     * Make sure we save the Uid so we can do ordering during recovery
     */
    public boolean save_state(OutputObjectState os, int ot)
    {
        try {
            os.packBytes(order.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("save: " + order);

        return true;
    }

    /**
     * Make sure we restore the Uid so we can do ordering during recovery
     */
    public boolean restore_state(InputObjectState os, int ot)
    {

        try {
            order = new Uid(os.unpackBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("restored: " + order);

        return true;
    }

    /**
     * Use the Uid to enforce the order
     */
    public Uid order()
    {
        return order;
    }

    public String type()
    {
        return "/StateManager/AbstractRecord/" + getClass().getName();
    }

    // Methods below this are NO-OP/defaults/disable 1PC

    public int nestedOnePhaseCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedAbort()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedCommit()
    {
        return TwoPhaseOutcome.FINISH_ERROR;
    }

    public int nestedPrepare()
    {
        return TwoPhaseOutcome.PREPARE_NOTOK;
    }

    public boolean shouldAdd(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldMerge(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldReplace(AbstractRecord a)
    {
        return false;
    }

    public boolean shouldAlter(AbstractRecord a)
    {
        return false;
    }

    public void merge(AbstractRecord a)
    {
    }

    public void alter(AbstractRecord a)
    {
    }

    public Object value()
    {
        return null;
    }

    public void setValue(Object o)
    {
    }
}
