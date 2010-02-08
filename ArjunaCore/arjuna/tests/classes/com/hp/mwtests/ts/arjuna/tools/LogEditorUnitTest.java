/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

package com.hp.mwtests.ts.arjuna.tools;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;

import org.junit.Test;

import static org.junit.Assert.*;

public class LogEditorUnitTest
{
    @Test
    public void test () throws Exception
    {
        // dummy to set up ObjectStore

        AtomicAction A = new AtomicAction();

        A.begin();

        A.add(new CrashRecord(CrashRecord.CrashLocation.NoCrash,
                CrashRecord.CrashType.Normal));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        A.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));

        int outcome = A.commit();

        System.out.println("Transaction " + A + " committed with "
                + ActionStatus.stringForm(outcome));

        AtomicAction B = new AtomicAction();

        B.begin();

        B.add(new CrashRecord(CrashRecord.CrashLocation.NoCrash,
                CrashRecord.CrashType.Normal));
        B.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        B.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));
        B.add(new CrashRecord(CrashRecord.CrashLocation.CrashInCommit,
                CrashRecord.CrashType.HeuristicHazard));

        outcome = B.commit();

        System.out.println("Transaction " + B + " committed with "
                + ActionStatus.stringForm(outcome));
    }
}