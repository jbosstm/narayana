/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wsas.common.arjunacore;

import java.nio.charset.StandardCharsets;
import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.mw.wsas.common.GlobalId;

/**
 * This implementation of GlobalId uses the ArjunaCore Uid class.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: GlobalIdImple.java,v 1.3 2004/03/15 13:25:01 nmcl Exp $
 * @since 1.0.
 */

public class GlobalIdImple extends Uid implements GlobalId
{

    public GlobalIdImple ()
    {
	super();

	_value = stringForm().getBytes(StandardCharsets.UTF_8);
    }
    
    public GlobalIdImple (String id)
    {
	super(id);

	_value = stringForm().getBytes(StandardCharsets.UTF_8);
    }
    
    public byte[] value ()
    {
	return _value;
    }
    
    private byte[] _value;
    
}