/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.stats;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;
import com.sun.tools.jconsole.JConsolePlugin;

public class TxPerfPlugin extends JConsolePlugin implements PropertyChangeListener
{
    private TxPerfGraph graph;
    private Map<String, JPanel> tabs;

    public TxPerfPlugin() {
        // register itself as a listener
        addContextPropertyChangeListener(this);
    }

    /*
     * Returns a TxPerf tab to be added in JConsole.
     */
    public synchronized Map<String, JPanel> getTabs() {
        if (tabs == null) {
            graph = new TxPerfGraph(new JFrame("TxPerf"));
            
            graph.setMBeanServerConnection(
                getContext().getMBeanServerConnection());
            // use LinkedHashMap if you want a predictable order
            // of the tabs to be added in JConsole
            tabs = new LinkedHashMap<String, JPanel>();
            tabs.put("TxPerf", graph);
        }
        return tabs;
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();
        if (prop == null ? JConsoleContext.CONNECTION_STATE_PROPERTY == null : prop.equals(JConsoleContext.CONNECTION_STATE_PROPERTY)) {
            ConnectionState oldState = (ConnectionState)ev.getOldValue();
            ConnectionState newState = (ConnectionState)ev.getNewValue();
            // JConsole supports disconnection and reconnection
            // The MBeanServerConnection will become invalid when
            // disconnected. Need to use the new MBeanServerConnection object
            // created at reconnection time.
            if (newState == ConnectionState.CONNECTED && graph != null) {
                graph.setMBeanServerConnection(
                    getContext().getMBeanServerConnection());
            }
        }
    }

    @Override
    public void dispose() {
        graph.dispose();
        super.dispose();
    }

    @Override
    public SwingWorker<?,?> newSwingWorker() {
        return graph.newSwingWorker();
    }
}