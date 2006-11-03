/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * TestUtil.java
 */

package com.arjuna.wst.tests;

public class TestUtil
{
    public static final String NOEXCEPTION_TRANSACTION_IDENTIFIER                    = "NE123456TI";
    public static final String TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER = "TRBE123456TI";
    public static final String UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER    = "UTE123456TI";
    public static final String SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER                = "SE123456TI";

    public static final String NONEXISTENT_TRANSACTION_IDENTIFIER                    = "NONE123456TI";

    public static final String PREPAREDVOTE_PARTICIPANT_IDENTIFIER           = "PV123456PI";
    public static final String ABORTEDVOTE_PARTICIPANT_IDENTIFIER            = "AV123456PI";
    public static final String READONLYVOTE_PARTICIPANT_IDENTIFIER           = "ROV123456PI";

    public static final String NOEXCEPTION_PARTICIPANT_IDENTIFIER                    = "NE123456PI";

    public static final String FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER               = "FE123456PI";

    public static final String TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER = "TRBE123456PI";
    public static final String WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER            = "WSE123456PI";
    public static final String SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER                = "SE123456PI";

    public static final String NONEXISTENT_PARTICIPANT_IDENTIFIER                    = "NONE123456PI";
}
