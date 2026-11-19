/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package com.jboss.transaction.wstf.webservices.sc007.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.jboss.transaction.wstf.webservices.sc007.generated package. 
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

    private final static QName _Response_QNAME = new QName("http://www.wstf.org/sc007", "Response");
    private final static QName _Readonly_QNAME = new QName("http://www.wstf.org/sc007", "Readonly");
    private final static QName _VolatileAndDurable_QNAME = new QName("http://www.wstf.org/sc007", "VolatileAndDurable");
    private final static QName _ReplayCommit_QNAME = new QName("http://www.wstf.org/sc007", "ReplayCommit");
    private final static QName _EarlyReadonly_QNAME = new QName("http://www.wstf.org/sc007", "EarlyReadonly");
    private final static QName _PreparedAfterTimeout_QNAME = new QName("http://www.wstf.org/sc007", "PreparedAfterTimeout");
    private final static QName _Phase2Rollback_QNAME = new QName("http://www.wstf.org/sc007", "Phase2Rollback");
    private final static QName _EarlyAborted_QNAME = new QName("http://www.wstf.org/sc007", "EarlyAborted");
    private final static QName _Commit_QNAME = new QName("http://www.wstf.org/sc007", "Commit");
    private final static QName _Rollback_QNAME = new QName("http://www.wstf.org/sc007", "Rollback");
    private final static QName _CompletionCommit_QNAME = new QName("http://www.wstf.org/sc007", "CompletionCommit");
    private final static QName _RetryCommit_QNAME = new QName("http://www.wstf.org/sc007", "RetryCommit");
    private final static QName _RetryPreparedCommit_QNAME = new QName("http://www.wstf.org/sc007", "RetryPreparedCommit");
    private final static QName _RetryPreparedAbort_QNAME = new QName("http://www.wstf.org/sc007", "RetryPreparedAbort");
    private final static QName _LostCommitted_QNAME = new QName("http://www.wstf.org/sc007", "LostCommitted");
    private final static QName _CompletionRollback_QNAME = new QName("http://www.wstf.org/sc007", "CompletionRollback");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.jboss.transaction.wstf.webservices.sc007.generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TestMessageType }
     * 
     */
    public TestMessageType createTestMessageType() {
        return new TestMessageType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "Response")
    public JAXBElement<TestMessageType> createResponse(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_Response_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "Readonly")
    public JAXBElement<TestMessageType> createReadonly(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_Readonly_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "VolatileAndDurable")
    public JAXBElement<TestMessageType> createVolatileAndDurable(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_VolatileAndDurable_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "ReplayCommit")
    public JAXBElement<TestMessageType> createReplayCommit(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_ReplayCommit_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "EarlyReadonly")
    public JAXBElement<TestMessageType> createEarlyReadonly(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_EarlyReadonly_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "PreparedAfterTimeout")
    public JAXBElement<TestMessageType> createPreparedAfterTimeout(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_PreparedAfterTimeout_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "Phase2Rollback")
    public JAXBElement<TestMessageType> createPhase2Rollback(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_Phase2Rollback_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "EarlyAborted")
    public JAXBElement<TestMessageType> createEarlyAborted(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_EarlyAborted_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "Commit")
    public JAXBElement<TestMessageType> createCommit(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_Commit_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "Rollback")
    public JAXBElement<TestMessageType> createRollback(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_Rollback_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "CompletionCommit")
    public JAXBElement<String> createCompletionCommit(String value) {
        return new JAXBElement<String>(_CompletionCommit_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "RetryCommit")
    public JAXBElement<TestMessageType> createRetryCommit(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_RetryCommit_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "RetryPreparedCommit")
    public JAXBElement<TestMessageType> createRetryPreparedCommit(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_RetryPreparedCommit_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "RetryPreparedAbort")
    public JAXBElement<TestMessageType> createRetryPreparedAbort(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_RetryPreparedAbort_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TestMessageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "LostCommitted")
    public JAXBElement<TestMessageType> createLostCommitted(TestMessageType value) {
        return new JAXBElement<TestMessageType>(_LostCommitted_QNAME, TestMessageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wstf.org/sc007", name = "CompletionRollback")
    public JAXBElement<String> createCompletionRollback(String value) {
        return new JAXBElement<String>(_CompletionRollback_QNAME, String.class, null, value);
    }

}
