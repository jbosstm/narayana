/*
 * Copyright (C) 2005
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */

package com.arjuna.ats.internal.jta.resources.arjunacore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.coordinator.OnePhaseResource;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jta.logging.FacilityCode;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

/**
 * One Phase resource wrapper for XAResources. 
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 * @since ATS 4.1
  */
public class XAOnePhaseResource implements OnePhaseResource
{
    /**
     * The one phase XA resource.
     */
    private XAResource xaResource ;
    /**
     * The recoverable XA connection.
     */
    private RecoverableXAConnection recoverableXAConnection ;
    /**
     * The transaction identified.
     */
    private Xid xid ;
    
    /**
     * Default constructor for deserialising resource.
     */
    public XAOnePhaseResource()
    {
    }
    
    /**
     * Construct the one phase wrapper for the specified resource.
     * @param xaResource The XA resource being wrapped.
     * @param xid The transaction identifier.
     * @param params additional params to pass through.
     */
    public XAOnePhaseResource(final XAResource xaResource, final Xid xid, final Object[] params)
    {
        this.xaResource = xaResource ;
        this.xid = xid ;
        if ((params != null) && (params.length >= XAResourceRecord.XACONNECTION))
        {
            final Object param = params[XAResourceRecord.XACONNECTION] ;
            if (param instanceof RecoverableXAConnection)
                recoverableXAConnection = (RecoverableXAConnection)param ;
        }
    }

    /**
     * Commit the one phase resource.
     * @return TwoPhaseOutcome.FINISH_OK or TwoPhaseOutcome.FINISH_ERROR
     */
    public int commit()
    {
        try
        {
            xaResource.commit(xid, true) ;
            return TwoPhaseOutcome.FINISH_OK ;
        }
        catch (final XAException xae)
        {
            if (jtaLogger.logger.isDebugEnabled())
            {
                jtaLogger.logger.debug(DebugLevel.ERROR_MESSAGES,
                    VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_JTA,
                    "XAOnePhaseResource.commit(" + xid + ") " + xae.getMessage());
            }
			if ((xae.errorCode >= XAException.XA_RBBASE)
					&& (xae.errorCode <= XAException.XA_RBEND))
			{
				return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
			}
            return TwoPhaseOutcome.FINISH_ERROR ;
        }
    }

    /**
     * Commit the one phase resource.
     * @return TwoPhaseOutcome.FINISH_OK or TwoPhaseOutcome.FINISH_ERROR
     */
    public int rollback()
    {
        try
        {
            xaResource.rollback(xid) ;
            return TwoPhaseOutcome.FINISH_OK ;
        }
        catch (final XAException xae)
        {
            if (jtaLogger.logger.isWarnEnabled())
            {
                jtaLogger.logger.warn("XAOnePhaseResource.rollback(" + xid + ") " + xae.getMessage());
            }
            return TwoPhaseOutcome.FINISH_ERROR ;
        }
    }

    /**
     * Pack the state of the resource.
     * @param os The object output state.
     * 
     * @message com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource.pack
     * failed to serialise resource {0}
     */
    public void pack(final OutputObjectState os)
        throws IOException
    {
        XidImple.pack(os, xid);
        if (recoverableXAConnection != null)
        {
            os.packInt(RecoverableXAConnection.AUTO_RECOVERY);
            os.packString(recoverableXAConnection.getClass().getName());

            recoverableXAConnection.packInto(os);
        }
        else
        {
            os.packInt(RecoverableXAConnection.OBJECT_RECOVERY) ;
            final byte[] data ;
            try
            {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
                final ObjectOutputStream oos = new ObjectOutputStream(baos) ;
                oos.writeObject(xaResource) ;
                oos.flush() ;
                oos.close() ;
                data = baos.toByteArray() ;
            }
            catch (final IOException ioe)
            {
                final String message = "XAOnePhaseResource.pack() " +
                    jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource.pack", new Object[] {ioe}) ;
                if (jtaLogger.logger.isWarnEnabled())
                {
                    jtaLogger.logger.warn(message);
                }
                throw new IOException(message) ;
            }
            os.packBytes(data) ;
        }
    }

    /**
     * Unpack the state of the resource.
     * @param is The object input state.
     * 
     * @message com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource.unpack
     * failed to deserialise resource {0}
     *      
     * @message com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource.unpackType
     * Unknown recovery type {0}
     */
    public void unpack(final InputObjectState is)
        throws IOException
    {
        XidImple.unpack(is) ;
        final int recoveryType = is.unpackInt() ;
        switch(recoveryType)
        {
            case RecoverableXAConnection.AUTO_RECOVERY:
                final String recoverableXAConnectionClassName = is.unpackString() ;
                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader() ;
                final Class recoverableXAConnectionClass ;
                try
                {
                    recoverableXAConnectionClass = contextClassLoader.loadClass(recoverableXAConnectionClassName) ;
                }
                catch (final ClassNotFoundException cnfe)
                {
                    throw generateUnpackError(cnfe) ;
                }
    
                try
                {
                    recoverableXAConnection = (RecoverableXAConnection)recoverableXAConnectionClass.newInstance() ;
                }
                catch (final InstantiationException ie)
                {
                    throw generateUnpackError(ie) ;
                }
                catch (final IllegalAccessException iae)
                {
                    throw generateUnpackError(iae) ;
                }
                
                recoverableXAConnection.unpackFrom(is) ;
                try
                {
                    xaResource = recoverableXAConnection.getResource() ;
                }
                catch (final SQLException sqle)
                {
                    throw generateUnpackError(sqle) ;
                }
                break ;
            case RecoverableXAConnection.OBJECT_RECOVERY:
                final byte[] data = is.unpackBytes() ;
                try
                {
                    final ByteArrayInputStream bais = new ByteArrayInputStream(data) ;
                    final ObjectInputStream ois = new ObjectInputStream(bais) ;
                    xaResource = (XAResource)ois.readObject() ;
                }
                catch (final ClassNotFoundException cnfe)
                {
                    throw generateUnpackError(cnfe) ;
                }
                catch (final IOException ioe)
                {
                    throw generateUnpackError(ioe) ;
                }
                catch (final ClassCastException cce)
                {
                    throw generateUnpackError(cce) ;
                }
                break ;
            default:
                final String message = "XAOnePhaseResource.unpack() " +
                jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource.unpackType",
                    new Object[] {new Integer(recoveryType)}) ;
                if (jtaLogger.logger.isWarnEnabled())
                {
                    jtaLogger.logger.warn(message);
                }
                throw new IOException(message) ;
        }
    }
    
    /**
     * Generate the IOException for the corresponding unpack exception.
     * @param ex The exception caught in unpack.
     * @return The corresponding IOException to be thrown.
     */
    private static IOException generateUnpackError(final Exception ex)
    {
        final String message = "XAOnePhaseResource.unpack() " +
            jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.resources.arjunacore.XAOnePhaseResource.unpack", new Object[] {ex}) ;
        if (jtaLogger.logger.isWarnEnabled())
        {
            jtaLogger.logger.warn(message);
        }
        return new IOException(message) ;
    }
}
