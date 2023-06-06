/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */
package com.arjuna.webservices11.wsaddr;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceException;

import org.w3c.dom.Element;

/**
 * internal representation of endpoint reference
 */

// XmlRootElement allows this class to be marshalled on its own
@XmlRootElement(name = "EndpointReference", namespace = NativeEndpointReference.NS)
@XmlType(name = "EndpointReferenceType", namespace = NativeEndpointReference.NS)
public final class NativeEndpointReference extends EndpointReference
{
   protected static final String NS = "http://www.w3.org/2005/08/addressing";

   private final static JAXBContext jc = getJaxbContext();

   // private but necessary properties for databinding
   @XmlElement(name = "Address", namespace = NS)
   private Address address;
   @XmlElement(name = "ReferenceParameters", namespace = NS)
   private Elements referenceParameters;
   @XmlElement(name = "Metadata", namespace = NS)
   private Elements metadata;
   @XmlAnyAttribute
   Map<QName, String> attributes;
   @XmlAnyElement
   List<Element> elements;

   // not marshalled
   private QName serviceName;
   private QName endpointName;
   private URL wsdlLocation;

   public NativeEndpointReference()
   {
   }

   /**
    * Creates an EPR from infoset representation
    *
    * @param source A source object containing valid XmlInfoset
    * instance consistent with the W3C WS-Addressing Core
    * recommendation.
    *
    * @throws WebServiceException
    *   If the source does NOT contain a valid W3C WS-Addressing
    *   EndpointReference.
    * @throws NullPointerException
    *   If the <code>null</code> <code>source</code> value is given
    */
   public NativeEndpointReference(final Source source)
   {
      NativeEndpointReference epr = unmarshalNativeEndpointReference(source);
      this.address = epr.address;
      this.metadata = epr.metadata;
      this.referenceParameters = epr.referenceParameters;
   }

   @XmlTransient
   public String getAddress()
   {
      return address != null ? address.getUri() : null;
   }

   public void setAddress(String address)
   {
      this.address = new Address(address);
   }

   @XmlTransient
   public QName getServiceName()
   {
      return serviceName;
   }

   public void setServiceName(QName serviceName)
   {
      this.serviceName = serviceName;
   }

   @XmlTransient
   public QName getEndpointName()
   {
      return endpointName;
   }

   public void setEndpointName(QName endpointName)
   {
      this.endpointName = endpointName;
   }

   @XmlTransient
   public List<Element> getMetadata()
   {
      return metadata != null ? metadata.getElements() : null;
   }

   public void setMetadata(List<Element> metadata)
   {
      this.metadata = new Elements(metadata);
   }

   @XmlTransient
   public URL getWsdlLocation()
   {
      return wsdlLocation;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      try
      {
         this.wsdlLocation = wsdlLocation != null ? new URL(wsdlLocation) : null;
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException("Invalid URL: " + wsdlLocation);
      }
   }

   @XmlTransient
   public List<Element> getReferenceParameters()
   {
      return referenceParameters != null ? referenceParameters.getElements() : null;
   }

   public void setReferenceParameters(List<Element> metadata)
   {
      this.referenceParameters = new Elements(metadata);
   }

   /**
    * Directly read a NativeEndpointReference from the given source
    * instead of leveraging the Provider's readEndpointReference method.
    *
    * @param eprInfoset
    * @return the endpointreference
    */
   public static EndpointReference readFrom(Source eprInfoset)
   {
      if (eprInfoset == null)
         throw new NullPointerException("Provided eprInfoset cannot be null");
      try
      {
         return new NativeEndpointReference(eprInfoset);
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void writeTo(Result result)
   {
      try
      {
         Marshaller marshaller = jc.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
         marshaller.marshal(this, result);
      }
      catch (JAXBException e)
      {
         throw new WebServiceException("Error marshalling NativeEndpointReference. ", e);
      }
   }

   private static JAXBContext getJaxbContext()
   {
      final NativeEndpointReferenceContextAction action = NativeEndpointReferenceContextAction.getInstance();

      if (System.getSecurityManager() == null) {
         try {
            return action.run();
         } catch (final JAXBException e) {
            throw new WebServiceException("Cannot obtain JAXB context", e);
         }
      }

      try {
         return AccessController.doPrivileged(action);
      } catch (final PrivilegedActionException e) {
         throw new WebServiceException("Cannot obtain JAXB context", e.getException());
      }
   }

   private NativeEndpointReference unmarshalNativeEndpointReference(final Source source) {
      final JAXBContextUnmarshalAction<NativeEndpointReference> unmarshalAction =
              JAXBContextUnmarshalAction.getInstance(jc, source, NativeEndpointReference.class);

      if (System.getSecurityManager() == null) {
         return unmarshalAction.run();
      }

      return AccessController.doPrivileged(unmarshalAction);
   }

   private static class Address
   {
      @XmlValue
      String uri;
      @XmlAnyAttribute
      Map<QName, String> attributes;

      protected Address()
      {
      }

      public Address(String uri)
      {
         this.uri = uri;
      }

      @XmlTransient
      public String getUri()
      {
         return uri;
      }

      public void setUri(String uri)
      {
         this.uri = uri;
      }

      @XmlTransient
      public Map<QName, String> getAttributes()
      {
         return attributes;
      }

      public void setAttributes(Map<QName, String> attributes)
      {
         this.attributes = attributes;
      }
   }

   private static class Elements
   {
      @XmlAnyElement
      List<Element> elements;
      @XmlAnyAttribute
      Map<QName, String> attributes;

      protected Elements()
      {
      }

      public Elements(List<Element> elements)
      {
         this.elements = elements;
      }

      @XmlTransient
      public List<Element> getElements()
      {
         return elements;
      }

      public void setElements(List<Element> elements)
      {
         this.elements = elements;
      }

      @XmlTransient
      public Map<QName, String> getAttributes()
      {
         return attributes;
      }

      public void setAttributes(Map<QName, String> attributes)
      {
         this.attributes = attributes;
      }
   }
}
