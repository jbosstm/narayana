
package org.oasis_open.docs.ws_tx.wsba._2006._06;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.oasis_open.docs.ws_tx.wsba._2006._06 package. 
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

    private final static QName _Canceled_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Canceled");
    private final static QName _Cancel_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Cancel");
    private final static QName _Status_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Status");
    private final static QName _Close_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Close");
    private final static QName _Exited_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Exited");
    private final static QName _Complete_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Complete");
    private final static QName _Failed_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Failed");
    private final static QName _Compensate_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Compensate");
    private final static QName _CannotComplete_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "CannotComplete");
    private final static QName _Completed_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Completed");
    private final static QName _Closed_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Closed");
    private final static QName _Compensated_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Compensated");
    private final static QName _Exit_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Exit");
    private final static QName _Fail_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "Fail");
    private final static QName _GetStatus_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "GetStatus");
    private final static QName _NotCompleted_QNAME = new QName("http://docs.oasis-open.org/ws-tx/wsba/2006/06", "NotCompleted");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.oasis_open.docs.ws_tx.wsba._2006._06
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
     * Create an instance of {@link BAMixedOutcomeAssertion }
     * 
     */
    public BAMixedOutcomeAssertion createBAMixedOutcomeAssertion() {
        return new BAMixedOutcomeAssertion();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link NotificationType }
     * 
     */
    public NotificationType createNotificationType() {
        return new NotificationType();
    }

    /**
     * Create an instance of {@link BAAtomicOutcomeAssertion }
     * 
     */
    public BAAtomicOutcomeAssertion createBAAtomicOutcomeAssertion() {
        return new BAAtomicOutcomeAssertion();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Canceled")
    public JAXBElement<NotificationType> createCanceled(NotificationType value) {
        return new JAXBElement<NotificationType>(_Canceled_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Cancel")
    public JAXBElement<NotificationType> createCancel(NotificationType value) {
        return new JAXBElement<NotificationType>(_Cancel_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Status")
    public JAXBElement<StatusType> createStatus(StatusType value) {
        return new JAXBElement<StatusType>(_Status_QNAME, StatusType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Close")
    public JAXBElement<NotificationType> createClose(NotificationType value) {
        return new JAXBElement<NotificationType>(_Close_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Exited")
    public JAXBElement<NotificationType> createExited(NotificationType value) {
        return new JAXBElement<NotificationType>(_Exited_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Complete")
    public JAXBElement<NotificationType> createComplete(NotificationType value) {
        return new JAXBElement<NotificationType>(_Complete_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Failed")
    public JAXBElement<NotificationType> createFailed(NotificationType value) {
        return new JAXBElement<NotificationType>(_Failed_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Compensate")
    public JAXBElement<NotificationType> createCompensate(NotificationType value) {
        return new JAXBElement<NotificationType>(_Compensate_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "CannotComplete")
    public JAXBElement<NotificationType> createCannotComplete(NotificationType value) {
        return new JAXBElement<NotificationType>(_CannotComplete_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Completed")
    public JAXBElement<NotificationType> createCompleted(NotificationType value) {
        return new JAXBElement<NotificationType>(_Completed_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Closed")
    public JAXBElement<NotificationType> createClosed(NotificationType value) {
        return new JAXBElement<NotificationType>(_Closed_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Compensated")
    public JAXBElement<NotificationType> createCompensated(NotificationType value) {
        return new JAXBElement<NotificationType>(_Compensated_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Exit")
    public JAXBElement<NotificationType> createExit(NotificationType value) {
        return new JAXBElement<NotificationType>(_Exit_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExceptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "Fail")
    public JAXBElement<ExceptionType> createFail(ExceptionType value) {
        return new JAXBElement<ExceptionType>(_Fail_QNAME, ExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "GetStatus")
    public JAXBElement<NotificationType> createGetStatus(NotificationType value) {
        return new JAXBElement<NotificationType>(_GetStatus_QNAME, NotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://docs.oasis-open.org/ws-tx/wsba/2006/06", name = "NotCompleted")
    public JAXBElement<NotificationType> createNotCompleted(NotificationType value) {
        return new JAXBElement<NotificationType>(_NotCompleted_QNAME, NotificationType.class, null, value);
    }

}
