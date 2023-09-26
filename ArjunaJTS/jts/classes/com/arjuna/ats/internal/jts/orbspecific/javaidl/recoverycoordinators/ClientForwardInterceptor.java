/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;


import com.arjuna.ats.jts.logging.jtsLogger;
import java.nio.charset.StandardCharsets;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.RecoveryCoordinatorHelper;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;


/**
 * This interceptor redirects requests towards the RecoveryCoordinator returned on register_resource
 * to another target, a Recovery Coordinator located in the Recovery Manager, by throwing a ForwardRequest 
 * exception.
 *
 * @author Malik Saheb
 */
public class ClientForwardInterceptor
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{
    public ClientForwardInterceptor(org.omg.CORBA.ORB orb, 
				    org.omg.PortableInterceptor.Current _piCurrent,
				    int _IndicatorSlotId)
    {

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("Client Interceptor for RecoveryCoordinators created");
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
	    RCobjectId = JavaIdlRCServiceInit._poa.reference_to_id(ri.effective_target());
	    objectIdString = new String(RCobjectId);
	    	    
	    if ( JavaIdlRCServiceInit.RC_ID.equals(objectIdString) )
		{
		    Any indicator = ri.get_slot(IndicatorSlotId);
		    if (indicator.type().kind().equals(TCKind.tk_null))
			{
			    ri.add_request_service_context(RCctx, false);
			}
		}
	}
	catch(Exception ex) {
        jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_ClientForwardInterceptor_4(ex);
    }


	if (!in_loop)
	    {
		in_loop = true;
		if ( JavaIdlRCServiceInit.RC_ID.equals(objectIdString) ) {

		    if (ri.effective_target()._is_a(RecoveryCoordinatorHelper.id()))
			{
			    /*
			     * Extract the substring of the ObjectId that contains the Uid and 
			     * the Process Id and pass it to the data of the service context
			     */
			    RCobjectId = extractObjectId(objectIdString).getBytes(StandardCharsets.UTF_8);
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
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("RecoveryCoordinatorId(" + encodedRCData + ")");
    }

	String ObjectId2SvcCtx = null; 
	char delimiter = '#';
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
		catch (Exception e) {
            jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_ClientForwardInterceptor_2(e);
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