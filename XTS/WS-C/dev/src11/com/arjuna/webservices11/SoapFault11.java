package com.arjuna.webservices11;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import org.w3c.dom.Element;
import org.jboss.jbossts.xts.soapfault.Fault;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Feb 1, 2008
 * Time: 4:57:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SoapFault11 extends SoapFault {
    /**
      * The detailElement element.
      */
     private Element detailElement;
     /**
      * The header elements.
      */
     private Element[] headerElements ;
     /**
      * Default constructor.
      */
     public SoapFault11()
     {
         super();
     }

     /**
      * Construct a SOAP fault based on a throwable.
      * @param th The throwable.
      */
     public SoapFault11(final Throwable th)
     {
         super(th);
     }

     /**
      * Construct a SOAP header fault based on a throwable.
      * @param th The throwable.
      * @param headerElements The header elements.
      */
     public SoapFault11(final Throwable th, final Element[] headerElements)
     {
         super(th);
         this.headerElements = headerElements;
     }

     /**
      * Create a custom SOAP fault.
      * @param soapFaultType the soap fault type.
      * @param subcode The fault subcode.
      * @param reason The fault reason.
      */
     public SoapFault11(final SoapFaultType soapFaultType, final QName subcode, final String reason)
     {
         super(soapFaultType, subcode, reason) ;
     }

     /**
      * Create a custom SOAP fault.
      * @param soapFaultType the soap fault type.
      * @param subcode The fault subcode.
      * @param reason The fault reason.
      * @param headerElements The header elements.
      */
     public SoapFault11(final SoapFaultType soapFaultType, final QName subcode, final String reason, final Element[] headerElements)
     {
         super(soapFaultType, subcode, reason, null);
         this.headerElements = headerElements ;
     }

     /**
      * Create a custom SOAP fault.
      * @param soapFaultType the soap fault type.
      * @param subcode The fault subcode.
      * @param reason The fault reason.
      * @param detailElement The fault detailElement.
      */
     public SoapFault11(final SoapFaultType soapFaultType, final QName subcode, final String reason, final Element detailElement)
     {
         super(soapFaultType, subcode, reason, getDetailString(detailElement)) ;
         this.detailElement = detailElement;
     }

     /**
      * Create a custom SOAP fault.
      * @param soapFaultType the soap fault type.
      * @param reason The fault reason.
      */
     public SoapFault11(final SoapFaultType soapFaultType, final String reason)
     {
         this(soapFaultType, null, reason, null, null) ;
     }

     /**
      * Create a custom SOAP fault.
      * @param soapFaultType the soap fault type.
      * @param reason The fault reason.
      * @param detailElement The fault detailElement.
      */
     public SoapFault11(final SoapFaultType soapFaultType,
         final String reason, final Element detailElement)
     {
         this(soapFaultType, null, reason, detailElement, null) ;
     }

     /**
      * Create a custom SOAP fault.
      * @param soapFaultType the soap fault type.
      * @param subcode The fault subcode.
      * @param reason The fault reason.
      * @param detailElement The fault detailElement.
      * @param headerElements The fault header elements.
      */
     public SoapFault11(final SoapFaultType soapFaultType, final QName subcode,
         final String reason, final Element detailElement, final Element[] headerElements)
     {
         this.soapFaultType = soapFaultType ;
         this.subcode = subcode ;
         this.reason = reason ;
         this.detailElement = detailElement;
         this.headerElements = headerElements ;
     }

    // convert to/from fault we can send via the SoapFaultService

    public Fault toFault()
    {
        Fault fault = new Fault();
        QName faultcode = subcode;
        String faultstring = getReason();
        fault.setFaultcode(faultcode);
        fault.setFaultstring(faultstring);
        return fault;
    }

    public static SoapFault11 fromFault(Fault fault)
    {
        try {
            QName subcode = fault.getFaultcode();
            String reason = fault.getFaultstring();
            return new SoapFault11(SoapFaultType.FAULT_SENDER, subcode, reason);
        } catch (Throwable th) {
            return new SoapFault11(th);
        }
    }

     /**
      * Get the SOAP fault detailElement.
      * @return The SOAP fault detailElement.
      */
     public Element getDetailElement()
     {
         return detailElement;
     }

     /**
      * Set the SOAP fault detailElement.
      * @param detailElement The SOAP fault detailElement.
      */
     public void setDetailElement(final Element detailElement)
     {
         this.detailElement = detailElement;
     }

     /**
      * Get the SOAP fault header elements.
      * @return The SOAP fault header elements.
      */
     public Element[] getHeaderElements()
     {
         return headerElements ;
     }

     /**
      * Set the SOAP fault header elements.
      * @param headerElements The SOAP fault header elements.
      */
     public void setHeaderElements(final Element[] headerElements)
     {
         this.headerElements = headerElements ;
     }

     /**
      * return a String representing the supplied detail element
      * @param detailElement
      * @return
      */

     private static String getDetailString(Element detailElement)
     {
         return detailElement.getTextContent();
     }
}
