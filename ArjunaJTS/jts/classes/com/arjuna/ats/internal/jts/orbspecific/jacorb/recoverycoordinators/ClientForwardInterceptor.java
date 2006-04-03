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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ClientForwardInterceptor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;


import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.RecoveryCoordinatorHelper;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

/**
 * This interceptor redirects requests towards the RecoveryCoordinator returned on register_resource
 * to another target, a Recovery Coordinator located in the Recovery Manager, by throwing a ForwardRequest 
 * exception.
 *
 * @author Malik Saheb
 */

/**
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_1 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_1] -  Client Interceptor for RecoveryCoordinators created
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_2 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_2] -  Failed to retreive the Object reference of the default RecoverCoordinator Object.
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_3 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_3] -  Failed to obtain the ObjectId string of the RecveryCoordinator target. 
 * @message com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_4 [com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_4] -  Failed to build service context with the ObjectId 
 */

public class ClientForwardInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{
    public ClientForwardInterceptor(org.omg.CORBA.ORB orb, 
				    org.omg.PortableInterceptor.Current _piCurrent,
				    int _IndicatorSlotId)
    {

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_1");
	      }

	piCurrent = _piCurrent;
	IndicatorSlotId = _IndicatorSlotId;
	org.omg.CORBA.Object obj = null ;
	_ourOrb = orb;
	
    }

    public String name() 
    {
        return "arjuna.ClientForwardInterceptor";
    }

    public void destroy()
    {

    }

    /**
     * Throws a ForwardRequest
     */
    public void send_request(ClientRequestInfo ri) 
        throws ForwardRequest
    {

	String objectIdString = null;

	try  {
	    RCobjectId = JacOrbRCServiceInit._poa.reference_to_id(ri.effective_target());
	    objectIdString = new String(RCobjectId);
	    	    
	    if ( objectIdString.compareTo("RecoveryManager")== 0 )
		{
		    Any indicator = ri.get_slot(IndicatorSlotId);
		    if (indicator.type().kind().equals(TCKind.tk_null))
			{
			    ri.add_request_service_context(RCctx, false);
			}
		}
	}
	catch(Exception ex)
	  {
	      jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_4", ex);
	  }


	if (!in_loop)
	    {
		in_loop = true;
		if ( objectIdString.compareTo("RecoveryManager")!= 0 ) {

		    if (ri.effective_target()._is_a(RecoveryCoordinatorHelper.id()))
			{
			    /*
			     * Extract the substring of the ObjectId that contains the Uid and 
			     * the Process Id and pass it to the data of the service context
			     */
			    RCobjectId = extractObjectId(objectIdString).getBytes();
			    RCctx = new ServiceContext(RecoveryContextId, RCobjectId);
			    in_loop = false;
			    throw new ForwardRequest( reco );
			}
		    else
			{
			    in_loop = false;
			}
		}
		in_loop = false;
	    }
	
    }

    public void send_poll(ClientRequestInfo ri){
    }

    public void receive_reply(ClientRequestInfo ri){
    }

    public void receive_exception(ClientRequestInfo ri) 
        throws ForwardRequest{
    }

    public void receive_other(ClientRequestInfo ri) 
        throws ForwardRequest{
    }




    /*
     * Extract from the ObjectID of the recoveryCorrdinator Object the substring containing the transaction Id
     * and the process Id that should be propagated in the service context.
     * The Object reference of the default recoveryCoordinator that receives effectively replay_completion
     * is also extract and passed to the global private variable reco, defined below, is also extracted.
     * null is returned if not found appropriate String.
     */

    private String extractObjectId(String encodedRCData)
    {
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.FUNCTIONS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "RecoveryCoordinatorId(" + encodedRCData + ")");
	    }

	String ObjectId2SvcCtx = null; 
	char delimiter = '#'; //JacOrbRCManager.rcKeyDelimiter;

	boolean ok = (encodedRCData != null);

	if (ok)
	{
	    int index1 = encodedRCData.indexOf(delimiter);
	    int index2 = 0;
	    
	    if (index1 != -1)
	    {
		ObjectId2SvcCtx = encodedRCData.substring(0, index1);
	    }
	    else
		ok = false;

	    if (ok)
	    {
		try 
		    {
			String  RCDefaultObjectReference = encodedRCData.substring(index1 +1);
			org.omg.CORBA.Object obj = _ourOrb.string_to_object(RCDefaultObjectReference) ;
			reco = RecoveryCoordinatorHelper.narrow(obj);
		    }
		catch (Exception e)
		    {
			jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.ClientForwardInterceptor_2", e);
		    }
		
	    }
	    
	}

	return ObjectId2SvcCtx;
	
    }



    private RecoveryCoordinator reco = null;
    
    private boolean first_loop = false;

    private boolean in_loop = false;
    private org.omg.CORBA.ORB _ourOrb = null;
    
    ServiceContext RCctx = null;

    // The following tag should be placed somewhere else and advertise it should not be used by applications
    int RecoveryContextId = 100001; 

    byte[] RCobjectId;

    private org.omg.PortableInterceptor.Current piCurrent;
    private int IndicatorSlotId = -1;


} // ClientForwardInterceptor
