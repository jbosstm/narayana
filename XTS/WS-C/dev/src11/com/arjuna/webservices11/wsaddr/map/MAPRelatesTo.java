package com.arjuna.webservices11.wsaddr.map;

import javax.xml.ws.addressing.Relationship;
import javax.xml.namespace.QName;

/**
 * MAPRelationship is a wrapper class which works with class MAP. This is the JBossWS Native implementation.
 */
public class MAPRelatesTo
{
    MAPRelatesTo(String relatesTo, QName type)
    {
        this.relatesTo = relatesTo;
        this.type = type;
    }

    public String getRelatesTo()
    {
        return relatesTo;
    }

    public QName getType()
    {
        return type;
    }

    public void setType(QName type)
    {
        this.type = type;
    }

    private String relatesTo;
    private QName type;
}
