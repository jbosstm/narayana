/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.common;

/**
 * A qualifier for the core coordination engine to use. Qualifiers
 * can be used to augment the protocol. For example, when enlisting a
 * participant with a transaction, it is possible to specify a caveat on
 * the enroll via a suitable qualifier, such that the coordinator knows
 * (in this example) that the participant will cancel if it does not hear
 * from the coordinator within 24 hours.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Qualifier.java,v 1.1 2002/11/25 10:56:31 nmcl Exp $
 * @since 1.0.
 */

public interface Qualifier
{
    /**
     * @return the unique name for this qualifier.
     */

    public String qualifierName ();

    /**
     * @return the context of this qualifier.
     */

    public String content ();
}