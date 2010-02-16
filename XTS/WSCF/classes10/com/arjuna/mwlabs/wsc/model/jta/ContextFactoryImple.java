/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
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
 * $Id: ContextFactoryImple.java,v 1.12.4.1 2005/11/22 10:34:10 kconner Exp $
 */

package com.arjuna.mwlabs.wsc.model.jta;

import javax.transaction.TransactionManager;

import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mw.wscf.model.xa.TransactionManagerFactory;
import com.arjuna.mwlabs.wsc.model.jta.context.JTAContextImple;
import com.arjuna.mwlabs.wsc.model.jta.participants.CleanupSynchronization;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;

public class ContextFactoryImple implements ContextFactory
{
    public ContextFactoryImple ()
    {
    	try
    	{
    	    _coordManager = TransactionManagerFactory.transactionManager();
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	}
    	_theRegistrar = new RegistrarImple();
    }

    /**
     * Called when a context factory is added to a context factory mapper. This method will be called multiple times
     * if the context factory is added to multiple context factory mappers or to the same context mapper with different
     * protocol identifiers.
     *
     * @param coordinationTypeURI the coordination type uri
     */

    public void install(final String coordinationTypeURI)
    {
    }

    // TODO interposition

    /*
     * If there is a context passed through to create then this newly created
     * coordinator should be interposed.
     */

    /**
     * Creates a coordination context.
     *
     * @param coordinationTypeURI the coordination type uri
     * @param expires the expire date/time for the returned context, can be null
     * @param currentContext the current context, can be null
     *
     * @return the created coordination context
     *
     * @throws InvalidCreateParametersException if a parameter passed is invalid
     *         this activity identifier
     *
     * @message com.arjuna.mwlabs.wsc.model.jta.ContextFactoryImple_1 [com.arjuna.mwlabs.wsc.model.jta.ContextFactoryImple_1] - Invalid type URI: < {0} , {1} >
     * @message com.arjuna.mwlabs.wsc.model.jta.ContextFactoryImple_11 [com.arjuna.mwlabs.wsc.model.jta.ContextFactoryImple_11] - Invalid type URI:
     */

    public CoordinationContextType create(final String coordinationTypeURI, final Long expires, final CoordinationContextType currentContext)
        throws InvalidCreateParametersException
    {
    	if (coordinationTypeURI.equals(_coordinationTypeURI))
    	{
    	    // TODO remove the suspend
    
    	    try
    	    {
        		_coordManager.suspend();
        	    
        		_coordManager.begin();
        
        		/*
        		DeploymentContext manager = DeploymentContextFactory.deploymentContext();
        		com.arjuna.mw.wsas.context.Context theContext = manager.context();
        		*/
        
                final JTAContextImple jtaContext = JTAContextImple.getContext() ;
                final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
                final String registrationCoordinatorURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
                
                final CoordinationContextType coordinationContext = new CoordinationContextType() ;
                coordinationContext.setCoordinationType(new URI(coordinationTypeURI)) ;
                coordinationContext.setIdentifier(new AttributedURIType(jtaContext.getTransactionIdentifier())) ;
                final int transactionExpires = jtaContext.getTransactionExpires() ;
                if (transactionExpires > 0)
                {
                    coordinationContext.setExpires(new AttributedUnsignedIntType(transactionExpires)) ;
                }
                final EndpointReferenceType registrationCoordinator = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorURI)) ;
                InstanceIdentifier.setEndpointInstanceIdentifier(registrationCoordinator, jtaContext.getTransactionIdentifier()) ;
                coordinationContext.setRegistrationService(registrationCoordinator) ;

        		/*
        		 * Now add the registrar for this specific coordinator to
        		 * the mapper.
        		 */
        
        		_coordManager.getTransaction().registerSynchronization(new CleanupSynchronization(_coordManager.getTransaction().toString(), _theRegistrar));
        
        		_theRegistrar.associate() ;
        
        		return coordinationContext ;
    	    }
    	    catch (javax.transaction.RollbackException ex)
    	    {
        		// TODO handle properly
        
        		ex.printStackTrace();
    	    }
    	    catch (javax.transaction.SystemException ex)
    	    {
        		// TODO handle properly
        
        		ex.printStackTrace();
    	    }
    	    catch (javax.transaction.NotSupportedException ex)
    	    {
        		// TODO handle properly
        
        		ex.printStackTrace();
    	    }
    	    catch (Exception ex)
    	    {
    		// TODO handle properly
    
    		ex.printStackTrace();
    	    }		
    	}
    	else
    	{
    	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wsc.model.jta.ContextFactoryImple_1",
    					  new Object[]{_coordinationTypeURI, coordinationTypeURI});
    
    	    throw new InvalidCreateParametersException(wscfLogger.arjLoggerI18N.getString("com.arjuna.mwlabs.wsc.model.jta.ContextFactoryImple_11")+" < "+_coordinationTypeURI+", "+coordinationTypeURI+" >");
    	}
    
    	return null;
    }

    /**
     * Called when a context factory is removed from a context factory mapper. This method will be called multiple
     * times if the context factory is removed from multiple context factory mappers or from the same context factory
     * mapper with different coordination type uris.
     *
     * @param coordinationTypeURI the coordination type uri
     */

    public void uninstall (final String coordinationTypeURI)
    {
    }

    public final RegistrarImple registrar ()
    {
        return _theRegistrar;
    }
    
    private TransactionManager _coordManager;
    private RegistrarImple     _theRegistrar;

    public static final String _coordinationTypeURI = "urn:arjuna:jta";
    
}
