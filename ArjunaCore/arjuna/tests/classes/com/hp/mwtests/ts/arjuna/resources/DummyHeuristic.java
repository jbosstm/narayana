/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.coordinator.HeuristicNotification;

public class DummyHeuristic extends HeuristicNotification
{
    public int getStatus ()
    {
        return _status;
    }

    @Override
    public void heuristicOutcome (int actionStatus)
    {
        _status = actionStatus;
    }

    @Override
    public int compareTo (Object o)
    {
        DummyHeuristic sr = (DummyHeuristic) o;
        if (get_uid().equals(sr.get_uid())) {
            return 0;
        } else {
            return get_uid().lessThan(sr.get_uid()) ? -1 : 1;
        }
    }

    private int _status = -1;

    @Override
    public boolean isInterposed() {
        return false;
    }
}