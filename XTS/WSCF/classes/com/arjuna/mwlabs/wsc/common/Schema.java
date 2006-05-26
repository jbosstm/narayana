/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Schema.java,v 1.5 2004/12/29 16:23:21 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.common;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Schema.java,v 1.5 2004/12/29 16:23:21 kconner Exp $
 * @since 1.0.
 */

public class Schema
{

    public static final String REGISTRATION_SERVICE = "wscoor:RegistrationService";
    public static final String ADDRESS = "wsu:Address";
    public static final String IDENTIFIER = "wsu:Identifier";
    public static final String EXPIRES = "wsu:Expires";
    public static final String COORDINATION_TYPE = "wscoor:CoordinationType";
    public static final String COORDINATION_CONTEXT = "wscoor:CoordinationContext";
    
    public static final String WSCOOR_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/10/wscoor";
    public static final String WSU_NAMESPACE = "http://schemas.xmlsoap.org/ws/2002/07/utility";
    public static final String ARJUNA_NAMESPACE = "http://arjuna.com/schemas/wsc/2003/01/extension";

}
