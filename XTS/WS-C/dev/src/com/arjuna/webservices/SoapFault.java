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
package com.arjuna.webservices;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.stax.TextElement;

/**
 * Class representing a SOAP Fault
 * @author kevin
 */
public class SoapFault extends Exception
{
    /**
     * Serial version UID for serialisation.
     */
    private static final long serialVersionUID = -5256882376188382002L ;
    
    /**
     * The SOAP fault type.
     */
    private SoapFaultType soapFaultType ;
    /**
     * The SOAP fault subcode.
     */
    private QName subcode ;
    /**
     * The SOAP fault sub subcode.
     */
    private QName subSubcode ;
    /**
     * The SOAP fault reason.
     */
    private String reason ;
    /**
     * The SOAP fault node ;
     */
    private String node ;
    /**
     * The SOAP fault role.
     */
    private String role ;
    /**
     * The detail element.
     */
    private NamedElement detail ;
    /**
     * The header elements.
     */
    private NamedElement[] headerElements ;
    /**
     * The header fault flag.
     */
    private boolean headerFault ;
    /**
     * The associated action.
     */
    private String action ;
    
    /**
     * Default constructor. 
     */
    public SoapFault()
    {
    }
    
    /**
     * Construct a SOAP fault based on a throwable.
     * @param th The throwable.
     */
    public SoapFault(final Throwable th)
    {
        this(SoapFaultType.FAULT_RECEIVER, null, th.toString(),
            new NamedElement(null, new TextElement(generateStackTrace(th))), null) ;
    }
    
    /**
     * Construct a SOAP header fault based on a throwable.
     * @param th The throwable.
     * @param headerElements The header elements.
     */
    public SoapFault(final Throwable th, final NamedElement[] headerElements)
    {
        this(SoapFaultType.FAULT_RECEIVER, null, th.toString(),
            null, headerElements) ;
    }
    
    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     */
    public SoapFault(final SoapFaultType soapFaultType, final QName subcode, final String reason)
    {
        this(soapFaultType, subcode, reason, null, null) ;
    }
    
    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     * @param headerElements The header elements.
     */
    public SoapFault(final SoapFaultType soapFaultType, final QName subcode, final String reason, final NamedElement[] headerElements)
    {
        this(soapFaultType, subcode, reason, null, headerElements) ;
    }
    
    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     * @param detail The fault detail.
     */
    public SoapFault(final SoapFaultType soapFaultType, final QName subcode, final String reason, final NamedElement detail)
    {
        this(soapFaultType, subcode, reason, detail, null) ;
    }
    
    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param reason The fault reason.
     */
    public SoapFault(final SoapFaultType soapFaultType, final String reason)
    {
        this(soapFaultType, null, reason, null, null) ;
    }
    
    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param reason The fault reason.
     * @param detail The fault detail.
     */
    public SoapFault(final SoapFaultType soapFaultType,
        final String reason, final NamedElement detail)
    {
        this(soapFaultType, null, reason, detail, null) ;
    }
    
    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     * @param detail The fault detail.
     * @param headerElements The fault header elements.
     */
    public SoapFault(final SoapFaultType soapFaultType, final QName subcode,
        final String reason, final NamedElement detail, final NamedElement[] headerElements)
    {
        this.soapFaultType = soapFaultType ;
        this.subcode = subcode ;
        this.reason = reason ;
        this.detail = detail ;
        this.headerElements = headerElements ;
    }
    
    /**
     * Get the SOAP fault type.
     * @return The SOAP fault type.
     */
    public SoapFaultType getSoapFaultType()
    {
        return soapFaultType ;
    }
    
    /**
     * Set the SOAP fault type.
     * @param soapFaultType The SOAP fault type.
     */
    public void setSoapFaultType(final SoapFaultType soapFaultType)
    {
        this.soapFaultType = soapFaultType ;
    }
    
    /**
     * Get the SOAP fault subcode.
     * @return The SOAP fault subcode.
     */
    public QName getSubcode()
    {
        return subcode ;
    }
    
    /**
     * Set the SOAP fault subcode.
     * @param subcode The SOAP fault subcode.
     */
    public void setSubcode(final QName subcode)
    {
        this.subcode = subcode ;
    }
    
    /**
     * Get the SOAP fault sub subcode.
     * @return The SOAP fault sub subcode.
     */
    public QName getSubSubcode()
    {
        return subSubcode ;
    }
    
    /**
     * Set the SOAP fault sub subcode.
     * @param subSubcode The SOAP fault sub subcode.
     */
    public void setSubSubcode(final QName subSubcode)
    {
        this.subSubcode = subSubcode ;
    }
    
    /**
     * Get the SOAP fault reason.
     * @return The SOAP fault reason.
     */
    public String getReason()
    {
        return reason ;
    }
    
    /**
     * Set the SOAP fault reason.
     * @param reason The SOAP fault reason.
     */
    public void setReason(final String reason)
    {
        this.reason = reason ;
    }
    
    /**
     * Get the SOAP fault node.
     * @return The SOAP fault node.
     */
    public String getNode()
    {
        return node ;
    }
    
    /**
     * Set the SOAP fault node.
     * @param node The SOAP fault node.
     */
    public void setNode(final String node)
    {
        this.node = node ;
    }
    
    /**
     * Get the SOAP fault role.
     * @return The SOAP fault role.
     */
    public String getRole()
    {
        return role ;
    }
    
    /**
     * Set the SOAP fault role.
     * @param role The SOAP fault role.
     */
    public void setRole(final String role)
    {
        this.role = role ;
    }
    
    /**
     * Get the SOAP fault detail.
     * @return The SOAP fault detail.
     */
    public NamedElement getDetail()
    {
        return detail ;
    }
    
    /**
     * Set the SOAP fault detail.
     * @param detail The SOAP fault detail.
     */
    public void setDetail(final NamedElement detail)
    {
        this.detail = detail ;
    }
    
    /**
     * Get the SOAP fault header elements.
     * @return The SOAP fault header elements.
     */
    public NamedElement[] getHeaderElements()
    {
        return headerElements ;
    }
    
    /**
     * Set the SOAP fault header elements.
     * @param headerElements The SOAP fault header elements.
     */
    public void setHeaderElements(final NamedElement[] headerElements)
    {
        this.headerElements = headerElements ;
    }
    
    /**
     * Is this a fault from a SOAP header?
     * @return true if generated by a SOAP header, false otherwise.
     */
    public boolean isHeaderFault()
    {
        return headerFault ;
    }

    /**
     * Set the header fault flag.
     * @param headerFault true if generated by a SOAP header, false otherwise.
     */
    public void setHeaderFault(final boolean headerFault)
    {
        this.headerFault = headerFault ;
    }
    
    /**
     * Get the fault action.
     * @return The fault action.
     */
    public String getAction()
    {
        return action ;
    }
    
    /**
     * Set the fault action.
     * @param action The fault action.
     */
    public void setAction(final String action)
    {
        this.action = action ;
    }
    
    /**
     * Get a message describing this fault.
     * @return The fault message.
     */
    public String getMessage()
    {
        final StringBuffer buffer = new StringBuffer(soapFaultType.getValue()) ;
        if (subcode != null)
        {
            buffer.append('[') ;
            buffer.append(subcode) ;
            if (subSubcode != null)
            {
                buffer.append(':') ;
                buffer.append(subSubcode) ;
            }
            buffer.append(']') ;
        }
        if (reason != null)
        {
            buffer.append('[') ;
            buffer.append(reason) ;
            buffer.append(']') ;
        }
        return buffer.toString() ;
    }
    
    /**
     * Generate a stack trace from a throwable.
     * @param th The throwable.
     * @return The stack trace.
     */
    private static String generateStackTrace(final Throwable th)
    {
        final StringWriter writer = new StringWriter() ;
        final PrintWriter pw = new PrintWriter(writer) ;
        th.printStackTrace(pw) ;
        if (!pw.checkError())
        {
            pw.close() ;
            return writer.toString() ;
        }
        else
        {
            return null ;
        }
    }
}
