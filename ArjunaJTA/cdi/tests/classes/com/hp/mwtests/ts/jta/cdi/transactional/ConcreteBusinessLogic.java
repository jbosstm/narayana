package com.hp.mwtests.ts.jta.cdi.transactional;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Boundary
public class ConcreteBusinessLogic extends AbstractBusinessLogic {

    private static final Logger LOGGER = Logger.getLogger(AbstractBusinessLogic.class);

    @Override
    protected void doSomethingInConcreteClass(final Throwable throwable) throws Throwable {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(getClass().getSimpleName() + ".doSomethingInConcreteClass");
        }

        if (Utills.getCurrentTransaction() == null) {
            throw new RuntimeException("No transaction is active");
        }

        if (throwable != null) {
            throw throwable;
        }
    }

}
