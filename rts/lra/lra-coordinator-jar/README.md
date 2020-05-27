Enable LRA JWT Security
=======================


Requirements
------------

* Always name the JWT token **Realm** as: `narayana-lra-jwt`

* Allowed Roles: `{"admin","client","system"}`

Role `system` is only for internal, nested coordinator endpoint calls. 

Before starting LRA-coordinator, we have to first set some MicroProfile Config properties, in order to enable LRA-JWT-security layer.


To enable security, set the value as "**true**":

	lra.security.enabled
	
Set JWT token Issuer:

	mp.jwt.verify.issuer

Set either publicKey location or set the publicKey value directly:
    
	mp.jwt.verify.publickey.location
	mp.jwt.verify.publickey
	
`mp.jwt.verify.publickey.location` option only works with HTTPS in Quarkus
