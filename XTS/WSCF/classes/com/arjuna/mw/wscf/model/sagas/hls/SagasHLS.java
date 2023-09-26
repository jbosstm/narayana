/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.hls;

import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wscf.model.sagas.api.*;

import com.arjuna.mw.wscf.api.UserCoordinatorService;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SagasHLS.java,v 1.2 2004/03/15 13:25:04 nmcl Exp $
 * @since 1.0.
 */

public interface SagasHLS extends HLS
{
    
    public UserCoordinatorService coordinatorService ();
    
    public UserCoordinator userCoordinator ();
    
    public CoordinatorManager coordinatorManager ();

}