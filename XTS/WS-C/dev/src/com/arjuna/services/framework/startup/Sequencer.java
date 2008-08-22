package com.arjuna.services.framework.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A manager which allows initializaton callabcks for a suite of related web apps to be registered and subsequently
 * run in a fixed order, irrespective of the order in which the application server initializes the web apps.
 *
 * Sequencer maintains two lists of independently managed callbacks, one for the WSCOOR 1.0 web app suite and one
 * for the WSCOOR 1.1 web app suite. When registering a callback listeners or servlets for a given web app supply a
 * sequence id and a web app id to identify i) the required sequence and ii) the entry in the sequence at which the
 * callback should be stored. If a web app registers multiple callbacks they are stored in the sequence entry as a
 * list in registration order. Once a web app has registered all its callbacks it closes the relevant entry. When
 * all entries in a sequence have been closed the entries are traversed in order and the callbacks in each entry are
 * triggered, thus ensuring that callbacks for web apps with lower sequence indices complete before callbacks with
 * higher indices are called.
 *
 * User: adinn
 * Date: Nov 30, 2007
 * Time: 4:05:13 PM
 */
public class Sequencer {
    // public API

    /*
     * indices for the independent startup sequences which we manage
     */

    /**
     *  index of startup sequence used for serialization of WSCOOR 1.0 initialization callbacks
     */
    public static final int SEQUENCE_WSCOOR10 = 0;

    /**
     *  index of startup sequence used for serialization of WSCOOR 1.1 initialization callbacks
     */
    public static final int SEQUENCE_WSCOOR11 = SEQUENCE_WSCOOR10 + 1;

    /**
     *  count of independent startup sequences.
     */
    public static final int SEQUENCE_MAX = SEQUENCE_WSCOOR11 + 1;

    /*
     * indices for sequence of web apps we need to ensure start in the correct order
     */

    /**
     * index of WS-C web app which initialises first in 1.0
     */
    public static final int WEBAPP_WSC10 = 0;

    /**
     * index of WS-T web app which initialises second in 1.0
     */
    public static final int WEBAPP_WST10 = WEBAPP_WSC10 + 1;

    /**
     * index of WSCF web app which intiialises third in 1.0
     */
    public static final int WEBAPP_WSCF10 = WEBAPP_WST10 + 1;

    /**
     * index of WSTX web app which initialises last in 1.0
     */
    public static final int WEBAPP_WSTX10 = WEBAPP_WSCF10 + 1;

    /**
     * there are four startup apps in the 1.0 sequence
     */
    public static final int WEBAPP_MAX10 = WEBAPP_WSTX10 + 1;

    /**
     * index of WS-C web app which initialises first in 1.1
     */
    public static final int WEBAPP_WSC11 = 0;

    /**
     * index of WS-T web app which initialises second in 1.1
     */
    public static final int WEBAPP_WST11 = WEBAPP_WSC11 + 1;

    /**
     * index of WSCF web app which intiialises third in 1.1
     */
    public static final int WEBAPP_WSCF11 = WEBAPP_WST11 + 1;

    /**
     * index of WSTX web app which intiialises fourth in 1.1
     */
    public static final int WEBAPP_WSTX11 = WEBAPP_WSCF11 + 1;

    /**
     * there are three startup apps in the 1.1 sequence
     */
    public static final int WEBAPP_MAX11 = WEBAPP_WSTX11 + 1;

    /**
     * notify end of callback registration for a web app associated with a given sequence
     * @param webapp
     */

    public static final void close(int sequence, int webapp)
    {
        SEQUENCERS[sequence].close(webapp);
    }

    /**
     * a callback class specialised by client web apps to execute startup code
     */
    public static abstract class Callback
    {
        /**
         * construct a callback associated with a web app in a given startup sequence and automatically register it
         * for subsequent execution
         * @param sequence the sequence in which the web app is contained either WSCOOR_1_0 or WSCOOR_1_1
         * @param webapp the web app with which this callback is associated
         */
        public Callback(int sequence, int webapp)
        {
            register(this, sequence, webapp);
        }

        /**
         * a callback method which is invoked invoked per callback in registration order per web app in web app
         * order per sequence as soon as all callback lists in the sequence have been closed
         */
        public abstract void run();
    }

    /**
     * undo the latch which initially delays running of any callback sequences. this is provided
     * to enable the Service start routine to configure any necessary parameters before the
     * listener callbacks are run. It is synchronized on the class so we can safely notify any
     * threads waiting to pass the latch which will be waiting on the Sequencer.class.
     */
    public static synchronized void unlatch()
    {
        latched = false;
        Sequencer.class.notifyAll();
    }

    // private implementation

    /**
     * a global latch used to delay running of callbacks until the XTS servcie is ready to
     * let them run
     */
    private static boolean latched = true;

    /**
     * a global list of all startup sequences
     */
    private static final Sequencer[] SEQUENCERS =
            {
                    new Sequencer(WEBAPP_MAX10),
                    new Sequencer(WEBAPP_MAX11)
            };

    /**
     * method called by the Callback constructor to append a startup callback to the list for a web
     * app in the appropriate startup sequence
     * @param callback a callback to add to the list for the web app
     * @param sequence the sequence in which the web app is contained either WSCOOR_1_0 or WSCOOR_1_1
     * @param webapp the web app with which this callback is associated
     */

    private static final void register(Callback callback, int sequence, int webapp)
    {
        SEQUENCERS[sequence].register(callback, webapp);
    }

    /**
     * each sequence contians an array of lists to hold callbacks registered per web app
     */
    private List<Callback>[] callbacks;

    /**
     * each sequence contains an array of flags per web app initially false and set to true when a web app has
     * registered all its callbacks and called close
     */
    private boolean[] closed;

    /**
     * counter incremented each time a callback list is closed. callback processing is triggered when this reaches the
     * sequence size count
     */
    private int closedCount;

    /**
     * count which sizes the callbacks list and closed array.
     */
    private int sequenceSize;

    /**
     * insert a callback into the list for a given web app associated with this sequence
     * @param callback the callback to be registerd
     * @param webapp the web app with which the callback is associated
     */
    private final void register(Callback callback, int webapp)
    {
        callbacks[webapp].add(callback);
    }

    /**
     * close the callback list associated with a given web app in this sequence and, if this is the last unclosed
     * list, trigger execution of all callbacks in sequence order
     * @param webapp the web app whose callback list is to be closed
     */
    private final void close(int webapp)
    {
        closed[webapp] = true;
        closedCount++;
        if (closedCount == sequenceSize) {
            runCallbacks();
        }
    }

    private void runCallbacks()
    {
        // we cannot run the callbacks until the sequencer has been unlatched
        passLatch();

        for (int i = 0; i < sequenceSize; i++) {
            Iterator<Callback> iter = callbacks[i].iterator();
            while (iter.hasNext()) {
                Callback cb = iter.next();
                cb.run();
            }
        }
    }

    /**
     * do not return until the latch has been lifted
     */
    private static synchronized void passLatch()
    {
        while (latched) {
            try {
                Sequencer.class.wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * construct a Sequencer to manage registration and execution of callbacks associated with an ordered sequence
     * of web apps
     * @param sequenceSize count of the number of web apps for which callbacks are to be registered
     */
    private Sequencer(int sequenceSize)
    {
        this.sequenceSize = sequenceSize;
        callbacks = new ArrayList[sequenceSize];
        for (int i = 0; i < sequenceSize; i++) {
            callbacks[i] = new ArrayList<Callback>();
        }
        closed = new boolean[sequenceSize];
        closedCount = 0;
    }
}
