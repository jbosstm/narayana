package com.hp.mwtests.ts.jta.cdi.serializability;

import jakarta.enterprise.context.SessionScoped;
import jakarta.transaction.Transactional;
import java.io.Serializable;

// any scope requiring serialization will do, e.g. session/conversation from the built-in ones
@SessionScoped
public class SessionFoo implements Serializable {

    private static final long serialVersionUID = 1L;

    // the interceptor serving this has to be serializable, if not CDI will bow up during bootstrap
    @Transactional
    String ping() {
        return SessionFoo.class.getSimpleName();
    }
}
