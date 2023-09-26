/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wscf.model.sagas.arjunacore;

import java.nio.charset.StandardCharsets;
import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.mw.wscf.common.CoordinatorId;

/**
 * This implementation of CoordinatorId uses the ArjunaCore Uid class.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorIdImple.java,v 1.2 2004/03/15 13:25:11 nmcl Exp $
 * @since 1.0.
 */

public class CoordinatorIdImple extends Uid implements CoordinatorId
{

    public CoordinatorIdImple ()
    {
	super();

	_value = stringForm().getBytes(StandardCharsets.UTF_8);
    }
    
    public CoordinatorIdImple (String id)
    {
	super(id);

	_value = stringForm().getBytes(StandardCharsets.UTF_8);
    }

    public CoordinatorIdImple (Uid id)
    {
	super(id);

	_value = stringForm().getBytes();
    }
    
    public byte[] value ()
    {
	return _value;
    }
    
    private byte[] _value;
    
}