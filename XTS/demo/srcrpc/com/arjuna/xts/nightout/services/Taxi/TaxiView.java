/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * TaxiView.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: TaxiView.java,v 1.2 2004/04/21 13:09:20 jhalliday Exp $
 *
 */

package com.arjuna.xts.nightout.services.Taxi;

/**
 * The visual interface (GUI) for the Taxi Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.2 $
 */
public class TaxiView extends javax.swing.JFrame
{
    // Note: Some parts of this file were auto-generated
    // by NetBeans 3.3 FormEditor (http://www.netbeans.org)

    /**
     * Create a new TaxiView instance.
     *
     * @param tManager The {@link TaxiManager} instance to bind to
     */
    public TaxiView(TaxiManager tManager)
    {
        taxiManager = tManager;
        initComponents();
        updateFields();
        backButtonColor = jButtonConfirm.getBackground();
    }

    /**
     * Initialize the form.
     * This is called by the constructor
     *
     * WARNING: Automatically generated code, may be overwritten
     */
    private void initComponents()
    {//GEN-BEGIN:initComponents
        jPanel2 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jButtonChangeDefaultAnswer = new javax.swing.JButton();
        jLabelDefaultAnswer = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabelDisplayMode = new javax.swing.JLabel();
        jButtonChangeMode = new javax.swing.JButton();
        jLabelResponse = new javax.swing.JLabel();
        jButtonConfirm = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea = new javax.swing.JTextArea();

        setTitle("Taxi Service");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jPanel2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
        jLabel10.setText("Taxi                                                                    ");
        jLabel10.setForeground(java.awt.Color.red);
        jLabel10.setFont(new java.awt.Font("Dialog", 1, 24));
        jPanel2.add(jLabel10);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jLabel15.setText("Mode:");
        jPanel3.add(jLabel15);

        jLabelDisplayMode.setText("Automatic");
        jLabelDisplayMode.setForeground(java.awt.Color.blue);
        jLabelDisplayMode.setFont(new java.awt.Font("Dialog", 1, 18));
        jPanel3.add(jLabelDisplayMode);

        jButtonChangeMode.setText("Change mode");
        jButtonChangeMode.setPreferredSize(new java.awt.Dimension(119, 27));
        jButtonChangeMode.setMaximumSize(new java.awt.Dimension(119, 27));
        jButtonChangeMode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonChangeModeActionPerformed(evt);
            }
        });

        jPanel3.add(jButtonChangeMode);

        jLabelResponse.setText("Response:");
        jPanel3.add(jLabelResponse);

        jButtonConfirm.setText("Confirm");
        jButtonConfirm.setPreferredSize(new java.awt.Dimension(119, 27));
        jButtonConfirm.setMaximumSize(new java.awt.Dimension(119, 27));
        jButtonConfirm.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonConfirmActionPerformed(evt);
            }
        });

        jPanel3.add(jButtonConfirm);

        jButtonCancel.setText("Cancel");
        jButtonCancel.setPreferredSize(new java.awt.Dimension(119, 27));
        jButtonCancel.setMaximumSize(new java.awt.Dimension(119, 27));
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonCancelActionPerformed(evt);
            }
        });

        jPanel3.add(jButtonCancel);

        getContentPane().add(jPanel3, java.awt.BorderLayout.WEST);

        jScrollPane1.setAutoscrolls(true);
        jTextArea.setEditable(false);
        jTextArea.setRows(10);
        jTextArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jScrollPane1.setViewportView(jTextArea);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    /**
     * Cancel event handler.
     */
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonCancelActionPerformed
        if (taxiManager.getIsPreparationWaiting())
        {
            Object preparation = taxiManager.getPreparation();
            try
            {
                taxiManager.setCommit(false);
                synchronized (preparation)
                {
                    preparation.notify();
                }
            }
            catch (Exception e)
            {
                System.err.println("TaxiView.jButtonCancelActionPerformed(): Unable to notify preparation.");
            }
        }
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Confirm event handler.
     */
    private void jButtonConfirmActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonConfirmActionPerformed
        if (taxiManager.getIsPreparationWaiting())
        {
            Object preparation = taxiManager.getPreparation();
            try
            {
                taxiManager.setCommit(true);
                synchronized (preparation)
                {
                    preparation.notify();
                }
            }
            catch (Exception e)
            {
                System.err.println("TaxiView.jButtonCancelActionPerformed(): Unable to notify preparation.");
            }
        }
    }//GEN-LAST:event_jButtonConfirmActionPerformed

    /**
     * ChangeMode event handler.
     */
    private void jButtonChangeModeActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonChangeModeActionPerformed
        if (taxiManager.isAutoCommitMode())
        {
            taxiManager.setAutoCommitMode(false);
        }
        else
        {
            taxiManager.setAutoCommitMode(true);
        }
        updateFields();
    }//GEN-LAST:event_jButtonChangeModeActionPerformed

    /**
     * Exit the application.
     */
    private void exitForm(java.awt.event.WindowEvent evt)
    {//GEN-FIRST:event_exitForm
        //System.exit(0); // disabled for embedding in application servers.
    }//GEN-LAST:event_exitForm

    /**
     * Add status (highlighted) text to the central jTextArea.
     *
     * @param text The String to add
     */
    public void addMessage(java.lang.String text)
    {
        jButtonConfirm.setBackground(backButtonColor);
        jButtonCancel.setBackground(backButtonColor);
        jTextArea.append(text + "\n");
        jScrollPane1.getVerticalScrollBar().setValue(jScrollPane1.getVerticalScrollBar().getMaximum());
    }

    /**
     * Add status (highlighted) text to the central jTextArea.
     *
     * @param text The String to add
     */
    public void addPrepareMessage(java.lang.String text)
    {
        jButtonConfirm.setBackground(java.awt.Color.red);
        jButtonCancel.setBackground(java.awt.Color.red);
        jTextArea.append(text + "\n");
        jScrollPane1.getVerticalScrollBar().setValue(jScrollPane1.getVerticalScrollBar().getMaximum());
    }

    /**
     * Synchronise the GUI with the underlying state.
     */
    public void updateFields()
    {
        //update fields related to interactive mode.
        if (taxiManager.isAutoCommitMode())
        {
            jLabelResponse.setVisible(false);
            jButtonConfirm.setVisible(false);
            jButtonCancel.setVisible(false);
            jLabelDisplayMode.setText("automatic");
        }
        else
        {
            jLabelResponse.setVisible(true);
            jButtonConfirm.setVisible(true);
            jButtonCancel.setVisible(true);
            jLabelDisplayMode.setText("interactive");
        }
    }

    /**
     * Allow use of a singleton model for web services demo.
     */
    public static TaxiView getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new TaxiView(TaxiManager.getSingletonInstance());
        }
        singletonInstance.show();
        return singletonInstance;
    }

    /**
     * A singleton instance of this class.
     */
    private static TaxiView singletonInstance;

    // Variables declaration - automatically generated - do not modify
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JButton jButtonChangeDefaultAnswer;
    private javax.swing.JLabel jLabelDefaultAnswer;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabelDisplayMode;
    private javax.swing.JButton jButtonChangeMode;
    private javax.swing.JLabel jLabelResponse;
    private javax.swing.JButton jButtonConfirm;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea;

    // End of automatically generated variables declarations
    /**
     * The  {@link TaxiManager} instance this view is bound to.
     */
    private TaxiManager taxiManager;
    /**
     * The current color of the back button.
     */
    private java.awt.Color backButtonColor;
}
