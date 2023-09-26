/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.internal.jts.ControlWrapper;

/**
 * Used to process thread associations.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ThreadAssociationControl.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class ThreadAssociationControl
{

    public final static void updateAssociation (ControlWrapper tx, int reason)
    {
	ThreadAssociations.updateAssociation(tx, reason);
    }

}