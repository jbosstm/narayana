/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.tools.stats;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JMenuBar;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Second;
import com.arjuna.ats.arjuna.coordinator.TxStatsMBean;
import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBeanMBean;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import java.util.Date;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.TimeSeriesCollection;

public class TxPerfGraph extends javax.swing.JPanel { //JFrame {
    private final static int NUMBER_OF_SAMPLES = 100;

    private int POLL_PERIOD = 4000; // in ms

    public final static int NUMBER_OF_TRANSACTIONS_SERIES = 0;
    public final static int NUMBER_OF_INFLIGHT_SERIES = 1;
    public final static int NUMBER_OF_COMMITTED_SERIES = 2;
    public final static int NUMBER_OF_ABORTED_SERIES = 3;
    public final static int NUMBER_OF_HEURISTICS_SERIES = 4;
    public final static int NUMBER_OF_NESTED_SERIES = 5;
    public final static int NUMBER_OF_TIMEDOUT_SERIES = 6;

    private final static String[] SERIES_LABELS = {
        "Transactions Created",
        "In Flight Transactions",
        "Committed Transactions",
        "Aborted Transactions",
        "Heuristics Raised",
        "Nested Transactions Created",
        "Timed Out Transactions",
    };

    private final static String[] PIE_CHART_LABELS = {
        "Nested",
        "Heuristic",
        "Committed",
        "Aborted",
    };

    private final static int[] PIE_CHART_SERIES = new int[] {
         NUMBER_OF_NESTED_SERIES,
         NUMBER_OF_HEURISTICS_SERIES,
         NUMBER_OF_COMMITTED_SERIES,
         NUMBER_OF_ABORTED_SERIES,
    };


    private MBeanServerConnection server;
    private JFrame frame;
    private TxStatsMBean txMBean;
    private CoordinatorEnvironmentBeanMBean coordMBean;

    private TimeSeries[] _dataSeries = new TimeSeries[7];
    private TimeSeriesCollection _tsDS[] = new TimeSeriesCollection[7];
    private Second currPeriod;
    private Second tZero;
    private DefaultPieDataset pieDS;
 //   TimerTask timerTask;
 //   Timer timer = new Timer("TxPerf Sampling thread");
    javax.swing.Timer swingTimer;
    ActionListener taskPerformer;

    /** Creates new form TxPerfGraph */
    public TxPerfGraph(JFrame frame) {
        this.frame = frame;
        tZero = new Second(new Date());
        initComponents();

        for (int count = 0; count < _dataSeries.length; count++) {
            _dataSeries[count] = new TimeSeries(SERIES_LABELS[count]);//, Second.class);
            //_dataSeries[count].setMaximumItemCount(NUMBER_OF_SAMPLES);
            _tsDS[count] = new TimeSeriesCollection(_dataSeries[count]);
        }

        chart1.setDataset(_tsDS[NUMBER_OF_TRANSACTIONS_SERIES]);
        chart1.setTitle(SERIES_LABELS[NUMBER_OF_TRANSACTIONS_SERIES]);
        chart1.setSubtitle("");

        allTxnBtn.setSelected(true);

        pieDS = new DefaultPieDataset();

        txnPieChart.setDataset(pieDS);
        txnPieChart.setTitle("All Transactions");
        txnPieChart.setSubtitle("(during last time slices)");

        periodSelectSlider.setMajorTickSpacing(10);
        periodSelectSlider.setToolTipText(
			"Select the number of (" + POLL_PERIOD + "ms) time slices over which to show the pie chart");

        SpinnerModel sm1 = new SpinnerNumberModel(POLL_PERIOD / 1000, 1, 10000, 1);
        SpinnerModel sm2 = new SpinnerNumberModel(NUMBER_OF_SAMPLES, 10, 1000, 1);

        pollIntervalSpinner.setModel(sm1);
        pollIntervalSpinner.setEditor(new JSpinner.NumberEditor(pollIntervalSpinner, "#"));
        sampleSizeSpinner.setModel(sm2);
        sampleSizeSpinner.setEditor(new JSpinner.NumberEditor(sampleSizeSpinner, "#"));

        pollIntervalSpinner.setVisible(false);
        sampleSizeSpinner.setVisible(false);
        enableStatsCB.setSelected(false);
        resetStatsBtn.setVisible(false);
		pollIntervalBtn.setVisible(false);
		sampleSizeBtn1.setVisible(false);

        taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newSwingWorker().execute();
            }
        };

        swingTimer = new javax.swing.Timer(POLL_PERIOD, taskPerformer);

        chartsPane.setSelectedIndex(1);
    }

    public void setMBeanServerConnection(MBeanServerConnection mbs) {
        this.server = mbs;
        try {
            txMBean = JMX.newMBeanProxy(server,
                    new ObjectName("jboss.jta:name=TransactionStatistics"), TxStatsMBean.class);
            coordMBean = JMX.newMBeanProxy(server,
                    new ObjectName("jboss.jta:name=CoordinatorEnvironmentBean"),
                        CoordinatorEnvironmentBeanMBean.class);

            coordMBean.setEnableStatistics(true);
            enableStatsCB.setSelected(true);

//        } catch (javax.management.InstanceNotFoundException e) {
//            System.out.println("Transaction statistics MBean is not available.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetStats() {
        //int cindex = _dataSeries[0].getIndex(now);
    }

    private void sample() {
        try {
            int secs = Calendar.getInstance().get(Calendar.SECOND) - tZero.getSecond();
            Date d1 = new Date(secs);
            Date d2 = new Date();
            Second now = new Second(d2);

            long [] stats = new long[7];

            stats[NUMBER_OF_TRANSACTIONS_SERIES] = txMBean.getNumberOfTransactions();
            stats[NUMBER_OF_INFLIGHT_SERIES] = txMBean.getNumberOfInflightTransactions();
            stats[NUMBER_OF_COMMITTED_SERIES] = txMBean.getNumberOfCommittedTransactions();
            stats[NUMBER_OF_ABORTED_SERIES] = txMBean.getNumberOfAbortedTransactions();
            stats[NUMBER_OF_HEURISTICS_SERIES] = txMBean.getNumberOfHeuristics();
            stats[NUMBER_OF_NESTED_SERIES] = txMBean.getNumberOfNestedTransactions();
            stats[NUMBER_OF_TIMEDOUT_SERIES] = txMBean.getNumberOfTimedOutTransactions();

            for (int i = 0; i < 7; i++) {
                _dataSeries[i].addOrUpdate(now, stats[i]);
            }

            if (currPeriod != null) {
                long [] pstats = new long[PIE_CHART_SERIES.length];

                int cindex = _dataSeries[0].getIndex(now);
                int slices = periodSelectSlider.getValue();
                int lb = cindex < slices ? 0 : cindex - slices;

                for (int i = 0; i < PIE_CHART_SERIES.length; i++) {
                    TimeSeries ts = _dataSeries[PIE_CHART_SERIES[i]];

                    pieDS.setValue(PIE_CHART_LABELS[i], pstats[i] =
                            ts.getValue(cindex).longValue() - ts.getValue(lb).longValue());
                }

                txnPieChart.setSubtitle("(during last " + // (cindex - lb + 1) * POLL_PERIOD / 1000 + " seconds) - " +
                        (cindex - lb + 1) + " poll intervals");
            }

            currPeriod = now;
        } catch (Exception e) {
            System.err.println("MBean property failure: " + e);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        seriesSelectBtnGroup = new javax.swing.ButtonGroup();
        configBtnGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        btnPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        allTxnBtn = new javax.swing.JRadioButton();
        inFlightTxnBtn = new javax.swing.JRadioButton();
        committedTxnBtn = new javax.swing.JRadioButton();
        abortedTxnBtn = new javax.swing.JRadioButton();
        heuristicTxnBtn = new javax.swing.JRadioButton();
        nestedTxnBtn = new javax.swing.JRadioButton();
        timedoutTxnBtn = new javax.swing.JRadioButton();
        chartsPane = new javax.swing.JTabbedPane();
        configTab = new javax.swing.JPanel();
        resetStatsBtn = new javax.swing.JButton();
        pollIntervalSpinner = new javax.swing.JSpinner();
        pollIntervalBtn = new javax.swing.JButton();
        enableStatsCB = new javax.swing.JCheckBox();
        sampleSizeBtn1 = new javax.swing.JButton();
        sampleSizeSpinner = new javax.swing.JSpinner();
        chart1 = new org.jfree.beans.JLineChart();
        txnPieChart = new org.jfree.beans.JPieChart();
        periodSelectPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        periodSelectSlider = new javax.swing.JSlider();
        menuBar = new javax.swing.JMenuBar();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setText("Select Series");

        seriesSelectBtnGroup.add(allTxnBtn);
        allTxnBtn.setText("Transactions");
        allTxnBtn.setToolTipText("Show all transactions");
        allTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allTxnBtnActionPerformed(evt);
            }
        });

        seriesSelectBtnGroup.add(inFlightTxnBtn);
        inFlightTxnBtn.setText("In Flight");
        inFlightTxnBtn.setToolTipText("Show transactions that have not yet been committed/aborted");
        inFlightTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inFlightTxnBtnActionPerformed(evt);
            }
        });

        seriesSelectBtnGroup.add(committedTxnBtn);
        committedTxnBtn.setText("Committed");
        committedTxnBtn.setToolTipText("Show successfully committed transactions");
        committedTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                committedTxnBtnActionPerformed(evt);
            }
        });

        seriesSelectBtnGroup.add(abortedTxnBtn);
        abortedTxnBtn.setText("Aborted");
        abortedTxnBtn.setToolTipText("Show stats for rolled back transactions");
        abortedTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abortedTxnBtnActionPerformed(evt);
            }
        });

        seriesSelectBtnGroup.add(heuristicTxnBtn);
        heuristicTxnBtn.setText("Heuristics");
        heuristicTxnBtn.setToolTipText("Show stats for transactions that terminated with a heuristic outcome");
        heuristicTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heuristicTxnBtnActionPerformed(evt);
            }
        });

        seriesSelectBtnGroup.add(nestedTxnBtn);
        nestedTxnBtn.setText("Nested");
        nestedTxnBtn.setToolTipText("Stats for nested transactions");
        nestedTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nestedTxnBtnActionPerformed(evt);
            }
        });

        seriesSelectBtnGroup.add(timedoutTxnBtn);
        timedoutTxnBtn.setText("Timed Out");
        timedoutTxnBtn.setToolTipText("Stats for transactions which exceeded there time to live");
        timedoutTxnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timedoutTxnBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout btnPanelLayout = new javax.swing.GroupLayout(btnPanel);
        btnPanel.setLayout(btnPanelLayout);
        btnPanelLayout.setHorizontalGroup(
            btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(btnPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(btnPanelLayout.createSequentialGroup()
                        .addGroup(btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inFlightTxnBtn)
                            .addComponent(allTxnBtn)
                            .addComponent(abortedTxnBtn)
                            .addComponent(committedTxnBtn))
                        .addGap(35, 35, 35)
                        .addGroup(btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nestedTxnBtn)
                            .addComponent(heuristicTxnBtn)
                            .addComponent(timedoutTxnBtn)))
                    .addComponent(jLabel1))
                .addContainerGap(188, Short.MAX_VALUE))
        );
        btnPanelLayout.setVerticalGroup(
            btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, btnPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(btnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(btnPanelLayout.createSequentialGroup()
                        .addComponent(heuristicTxnBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nestedTxnBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timedoutTxnBtn))
                    .addGroup(btnPanelLayout.createSequentialGroup()
                        .addComponent(allTxnBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inFlightTxnBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(committedTxnBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(abortedTxnBtn))))
        );

        chartsPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chartsPaneStateChanged(evt);
            }
        });

        resetStatsBtn.setText("Reset Stats");
        resetStatsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetStatsBtnActionPerformed(evt);
            }
        });

        pollIntervalSpinner.setToolTipText("Change polling interval (in seconds)");

        pollIntervalBtn.setText("Set Poll Interval");
        pollIntervalBtn.setToolTipText("Change polling interval (in seconds)");
        pollIntervalBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollIntervalBtnActionPerformed(evt);
            }
        });

        enableStatsCB.setText("Enable Statistics");
        enableStatsCB.setToolTipText("Stop data collection by disabling the stats MBean in the target JVM");
        enableStatsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableStatsCBActionPerformed(evt);
            }
        });

        sampleSizeBtn1.setText("Set Sample Size");
        sampleSizeBtn1.setToolTipText("Change polling interval (in seconds)");
        sampleSizeBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleSizeBtn1ActionPerformed(evt);
            }
        });

        sampleSizeSpinner.setToolTipText("Change Number of Data Samples ");

        javax.swing.GroupLayout configTabLayout = new javax.swing.GroupLayout(configTab);
        configTab.setLayout(configTabLayout);
        configTabLayout.setHorizontalGroup(
            configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configTabLayout.createSequentialGroup()
                .addGroup(configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(configTabLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enableStatsCB)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configTabLayout.createSequentialGroup()
                                .addGroup(configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(resetStatsBtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                                    .addGroup(configTabLayout.createSequentialGroup()
                                        .addComponent(pollIntervalBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(pollIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(152, 152, 152))))
                    .addGroup(configTabLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(sampleSizeBtn1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sampleSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        configTabLayout.setVerticalGroup(
            configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configTabLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(enableStatsCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sampleSizeBtn1)
                    .addComponent(sampleSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollIntervalBtn)
                    .addComponent(pollIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetStatsBtn)
                .addContainerGap(73, Short.MAX_VALUE))
        );

        chartsPane.addTab("Settings", configTab);

        chart1.setXAxisLabel("Time of Day");
        chart1.setXAxisScale(org.jfree.beans.AxisScale.INTEGER);
        chart1.setYAxisLabel("Number of Txns");
        chart1.setYAxisScale(org.jfree.beans.AxisScale.INTEGER);

        javax.swing.GroupLayout chart1Layout = new javax.swing.GroupLayout(chart1);
        chart1.setLayout(chart1Layout);
        chart1Layout.setHorizontalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 418, Short.MAX_VALUE)
        );
        chart1Layout.setVerticalGroup(
            chart1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 235, Short.MAX_VALUE)
        );

        chartsPane.addTab("Transactions", null, chart1, "View Transaction Statistics");

        javax.swing.GroupLayout txnPieChartLayout = new javax.swing.GroupLayout(txnPieChart);
        txnPieChart.setLayout(txnPieChartLayout);
        txnPieChartLayout.setHorizontalGroup(
            txnPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 418, Short.MAX_VALUE)
        );
        txnPieChartLayout.setVerticalGroup(
            txnPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 235, Short.MAX_VALUE)
        );

        chartsPane.addTab("Pie Chart", null, txnPieChart, "View Transactions as a Pie Chart");

        jLabel2.setText("Time Slices");
        jLabel2.setToolTipText("Select the number of polling periods to use for populating the pie chart");

        periodSelectSlider.setPaintLabels(true);
        periodSelectSlider.setPaintTicks(true);
        periodSelectSlider.setToolTipText("Select the number of time slices over which to show the pie chart");
        periodSelectSlider.setValue(30);

        javax.swing.GroupLayout periodSelectPanelLayout = new javax.swing.GroupLayout(periodSelectPanel);
        periodSelectPanel.setLayout(periodSelectPanelLayout);
        periodSelectPanelLayout.setHorizontalGroup(
            periodSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(periodSelectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(periodSelectSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
        );
        periodSelectPanelLayout.setVerticalGroup(
            periodSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(periodSelectPanelLayout.createSequentialGroup()
                .addGroup(periodSelectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(periodSelectPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                    .addComponent(periodSelectSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(periodSelectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chartsPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
                    .addComponent(btnPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(chartsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(periodSelectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void btnActionPerformed(int series) {
        chart1.setDataset(_tsDS[series]);
        chart1.setTitle(SERIES_LABELS[series]);
    }
    private void allTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_TRANSACTIONS_SERIES);
    }//GEN-LAST:event_allTxnBtnActionPerformed

    private void inFlightTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inFlightTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_INFLIGHT_SERIES);
    }//GEN-LAST:event_inFlightTxnBtnActionPerformed

    private void heuristicTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heuristicTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_HEURISTICS_SERIES);
    }//GEN-LAST:event_heuristicTxnBtnActionPerformed

    private void committedTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_committedTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_COMMITTED_SERIES);
    }//GEN-LAST:event_committedTxnBtnActionPerformed

    private void abortedTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abortedTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_ABORTED_SERIES);
    }//GEN-LAST:event_abortedTxnBtnActionPerformed

    private void nestedTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nestedTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_NESTED_SERIES);
    }//GEN-LAST:event_nestedTxnBtnActionPerformed

    private void timedoutTxnBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timedoutTxnBtnActionPerformed
        btnActionPerformed(NUMBER_OF_TIMEDOUT_SERIES);
    }//GEN-LAST:event_timedoutTxnBtnActionPerformed

    private void chartsPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chartsPaneStateChanged
        int tab = chartsPane.getSelectedIndex();

        btnPanel.setVisible(false);
        periodSelectPanel.setVisible(false);

        switch (tab) {
            case 1: btnPanel.setVisible(true); break;
            case 2: periodSelectPanel.setVisible(true); break;
            default:
                break;
        }
    }//GEN-LAST:event_chartsPaneStateChanged

    private void pollIntervalBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pollIntervalBtnActionPerformed
        Integer v = (Integer) pollIntervalSpinner.getValue();

        POLL_PERIOD = v * 1000;
        swingTimer.setDelay(POLL_PERIOD);
        startPolling();
    }//GEN-LAST:event_pollIntervalBtnActionPerformed

    private void resetStatsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetStatsBtnActionPerformed
        resetStats();
}//GEN-LAST:event_resetStatsBtnActionPerformed

    private void enableStatsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableStatsCBActionPerformed
        coordMBean.setEnableStatistics(enableStatsCB.isSelected());
    }//GEN-LAST:event_enableStatsCBActionPerformed

    private void sampleSizeBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleSizeBtn1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sampleSizeBtn1ActionPerformed

    private void startPolling() {
       // timer.cancel();       

        //timer.schedule(timerTask, 0, POLL_PERIOD);
        if (swingTimer.isRunning())
            swingTimer.restart();
        else
            swingTimer.start();
    }

    private static void createAndShowGUI(TxPerfGraph perfPanel) {
        JFrame frame = perfPanel.getFrame();
        // Create and set up the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        JComponent contentPane = (JComponent) frame.getContentPane();
        contentPane.add(perfPanel, BorderLayout.CENTER);
        contentPane.setOpaque(true); //content panes must be opaque
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        frame.setContentPane(contentPane);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private static MBeanServerConnection connect(String hostname, int port) {
        String urlPath = "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
        MBeanServerConnection server = null;

        try {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
            JMXConnector jmxc = JMXConnectorFactory.connect(url);
            server = jmxc.getMBeanServerConnection();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            System.err.println("Unable to get an MBean Server connection: " + e.getMessage());
            System.exit(1);
        }

        return server;
    }

    /*
     * SwingWorker for updating the TxPerf tab
     */
    public SwingWorker<TimeSeries[], Object> newSwingWorker() {
        return new Worker();
    }

    class Worker extends SwingWorker<TimeSeries[], Object> {

        @Override
        protected TimeSeries[] doInBackground() throws Exception {
            sample();
            return _dataSeries;
        }
    }

    public static void main(String args[]) throws InterruptedException, InvocationTargetException {
        final TxPerfGraph graphPanel = new TxPerfGraph(new JFrame("TxPerf"));
        String hostname = "localhost";
        int port = 1090;

        if (args.length > 0) {
            String[] opts = args[0].split(":");

            hostname = opts[0];

            if (opts.length > 1)
                port = Integer.parseInt(opts[1]);
        }

        System.out.println("Connecting to MBeanServer on endpoint " + hostname + ":" + port);

        MBeanServerConnection server = connect(hostname, port);
        graphPanel.setMBeanServerConnection(server);

        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                createAndShowGUI(graphPanel);
            }
        });

        graphPanel.startPolling();

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton abortedTxnBtn;
    private javax.swing.JRadioButton allTxnBtn;
    private javax.swing.JPanel btnPanel;
    private org.jfree.beans.JLineChart chart1;
    private javax.swing.JTabbedPane chartsPane;
    private javax.swing.JRadioButton committedTxnBtn;
    private javax.swing.ButtonGroup configBtnGroup;
    private javax.swing.JPanel configTab;
    private javax.swing.JCheckBox enableStatsCB;
    private javax.swing.JRadioButton heuristicTxnBtn;
    private javax.swing.JRadioButton inFlightTxnBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButton nestedTxnBtn;
    private javax.swing.JPanel periodSelectPanel;
    private javax.swing.JSlider periodSelectSlider;
    private javax.swing.JButton pollIntervalBtn;
    private javax.swing.JSpinner pollIntervalSpinner;
    private javax.swing.JButton resetStatsBtn;
    private javax.swing.JButton sampleSizeBtn1;
    private javax.swing.JSpinner sampleSizeSpinner;
    private javax.swing.ButtonGroup seriesSelectBtnGroup;
    private javax.swing.JRadioButton timedoutTxnBtn;
    private org.jfree.beans.JPieChart txnPieChart;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    private void setDefaultCloseOperation(int operation) {
        frame.setDefaultCloseOperation(operation);
    }

    private Container getContentPane() {
        return frame.getContentPane();
    }

    private void setJMenuBar(JMenuBar menuBar) {
        frame.setJMenuBar(menuBar);
    }

    private void pack() {
    }
}
