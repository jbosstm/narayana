package org.jboss.jbossts.txframework.api.configuration.trigger;

/**
 */
public enum BALifecycleEvent
{
    /**
     * lifecycle event which happens if all service requests methods executed in the activity have read only
     * outcomes or if a service request method indicates, via a control, that the activity should exit.
     */
    EXIT,
    /**
     * lifecycle event which happens if a service request indicates that the activity cannot completed
     */
    NOT_COMPLETE,
    /**
     * lifecycle event which happens when the activity is completed. for coordinator completion activities this
     * occurs when a completed notification is received from the coordinator. for participant completion activities
     * this happens when a service request method indicates, via a control, that the activity has completed or a
     * service request annotated with an @Completes annotation finishes executing.
     */
    COMPLETE,
    /**
     * lifecycle event which happens when the middleware has made durable the completed.
     */
    CONFIRM_COMPLETE,
    /**
     * lifecycle event which happens when the activity is cancelled.
     */
    CANCEL,
    /**
     * lifecycle event which happens when the activity is closed.
     */
    CLOSE,
    /**
     * lifecycle event which happens when the activity is compensated.
     */
    COMPENSATE,
    /**
     * lifecycle event which happens when the activity fails.
     */
    FAIL,
    /*
     * lifecycle event which happens when an error in the protocol occurs.
     */
    Error,

    //todo: document when this occurs, why deprecated and what to use instead
    @Deprecated
    UNKNOWN,

    //todo: do we need this lifecycle event? 
    STATUS
}