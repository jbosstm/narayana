/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.extensions;

import org.omg.CORBA.SystemException;

import com.arjuna.ats.arjuna.coordinator.CheckedAction;

/**
 * This interface gives access to the Arjuna specific extensions
 * to Current.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Current.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 4.0.
 */

public interface Current
{

    public void setCheckedAction (CheckedAction ca) throws SystemException;

    public CheckedAction getCheckedAction () throws SystemException;
 
}