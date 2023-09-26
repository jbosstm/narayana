/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.xa;

import java.sql.SQLException;

import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.exceptions.NotImplementedException;

/**
 * Instances of this class enable us to work around problems
 * in certain databases (specifically Oracle).
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: XAModifier.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

// TODO may be worth revisiting now as things may have changed in 10 years!

public interface XAModifier
{

    /**
     * Given an Arjuna xid, create a local driver representation. Some
     * drivers expect their own implementations to be used, despite the
     * fact that the JTA does not mention this!
     */

    public Xid createXid (Xid xid) throws SQLException, NotImplementedException;

    /**
     * Return the xa_start parameters for this level.
     */

    public int xaStartParameters (int level) throws SQLException, NotImplementedException;
    
}