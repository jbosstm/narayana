
package com.arjuna.schemas.ws._2005._10.wsarjtx;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.arjuna.schemas.ws._2005._10.wsarjtx package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Close_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Close");
    private final static QName _Closed_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Closed");
    private final static QName _Cancel_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Cancel");
    private final static QName _Completed_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Completed");
    private final static QName _Faulted_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Faulted");
    private final static QName _Cancelled_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Cancelled");
    private final static QName _Complete_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Complete");
    private final static QName _Fault_QNAME = new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "Fault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.arjuna.schemas.ws._2005._10.wsarjtx
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExceptionType }
     * 
     */
    public ExceptionType createExceptionType() {
        return new ExceptionType();
    }

    /**
     * Create an instance of {@link NotificationType }
     * 
     */
    public NotificationType createNotificationType() {
        return new NotificationType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Close")
    public JAXBElement<NotificationType> createClose(NotificationType value) {
        return new JAXBElement<NotificationType>(_Close_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Closed")
    public JAXBElement<NotificationType> createClosed(NotificationType value) {
        return new JAXBElement<NotificationType>(_Closed_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Cancel")
    public JAXBElement<NotificationType> createCancel(NotificationType value) {
        return new JAXBElement<NotificationType>(_Cancel_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Completed")
    public JAXBElement<NotificationType> createCompleted(NotificationType value) {
        return new JAXBElement<NotificationType>(_Completed_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Faulted")
    public JAXBElement<NotificationType> createFaulted(NotificationType value) {
        return new JAXBElement<NotificationType>(_Faulted_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Cancelled")
    public JAXBElement<NotificationType> createCancelled(NotificationType value) {
        return new JAXBElement<NotificationType>(_Cancelled_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Complete")
    public JAXBElement<NotificationType> createComplete(NotificationType value) {
        return new JAXBElement<NotificationType>(_Complete_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.arjuna.com/ws/2005/10/wsarjtx", name = "Fault")
    public JAXBElement<ExceptionType> createFault(ExceptionType value) {
        return new JAXBElement<ExceptionType>(_Fault_QNAME, ExceptionType.class, null, value);
    }

}
