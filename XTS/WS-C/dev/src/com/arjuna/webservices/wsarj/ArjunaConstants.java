/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices.wsarj;

import javax.xml.namespace.QName;

/**
 * Interface containing Arjuna WS constants.
 */
public interface ArjunaConstants
{
    /**
     * The Namespace.
     */
    public String WSARJ_NAMESPACE = "http://schemas.arjuna.com/ws/2005/10/wsarj" ;
    /**
     * The Attribute Namespace.
     */
    public String WSARJ_ATTRIBUTE_NAMESPACE = "" ;
    /**
     * The namespace prefix.
     */
    public String WSARJ_PREFIX = "wsarj" ;
    /**
     * The attribute namespace prefix.
     */
    public String WSARJ_ATTRIBUTE_PREFIX = "" ;

    /**
     * The InstanceIdentifier element.
     */
    public String WSARJ_ELEMENT_INSTANCE_IDENTIFIER = "InstanceIdentifier" ;
    /**
     * The InstanceIdentifier QName.
     */
    public QName WSARJ_ELEMENT_INSTANCE_IDENTIFIER_QNAME = new QName(WSARJ_NAMESPACE, WSARJ_ELEMENT_INSTANCE_IDENTIFIER, WSARJ_PREFIX) ;
}