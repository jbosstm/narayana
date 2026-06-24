package com.jboss.jbosstm.xts.demo.services.state;

import java.io.Serializable;

/**
 * A class which provides a simple versioning capability based on version numbers. A derived
 * child state has a version number one greater than its parent state.
 *
 * Services maintain a current service state plus an indexed set of per-transaction derived child states.
 * Conflicts can be detected at prepare time by comparing the derived state for the preparing transaction
 * with the current state. If the derived state is not a child of the current state then an intervening
 * write has occurred. If it is a child state of the current state then there can be no write conflict
 * and the prepare may proceed.
 */
public class ServiceState implements Serializable {
    protected long version;

    /**
     * construct an initial state with an initial version number of 0
     */
    protected ServiceState() {
        this.version = 0;
    }

    /**
     * construct a new state derived from this state bumping up the version number by one
      * @param parent the state from which the new state is to be derived
     */
    public ServiceState(ServiceState parent) {
        this.version = parent.version + 1;
    }

    /**
     * test whether the child state was derived from this state by checking the version numbers
     *
     * @param child the child state to be tested
     * @return true if the child state was derived from this state otherwise false
     */
    public boolean isParentOf(ServiceState child)
    {
        return (child.version == (version + 1));
    }
}
