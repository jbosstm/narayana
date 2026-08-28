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
 * RestaurantView.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: RestaurantView.java,v 1.2 2004/04/21 13:09:18 jhalliday Exp $
 *
 */

package com.jboss.jbosstm.xts.demo.services.restaurant;

import java.io.Serializable;

/**
 * The visual interface (GUI) for the Restaurant Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.2 $
 */
public class RestaurantView extends javax.swing.JFrame implements Serializable
{
    // Note: Some parts of this file were auto-generated
    // by NetBeans 3.3 FormEditor (http://www.netbeans.org)

    /**
     * Create a new RestaurantView instance.
     *
     * @param rManager The {@link RestaurantManager} instance to bind to
     */
    public RestaurantView(RestaurantManager rManager)
    {
        restManager = rManager;
        initComponents();
        updateFields();
        backButtonColor = jButtonConfirm.getBackground();
    }

    /**
     * Initialize the form.
     * This is called by the constructor
     * <p/>
     * WARNING: Automatically generated code, may be overwritten
     */
    private void initComponents()
    {//GEN-BEGIN:initComponents
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabelNBookedSeats = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabelNPreparedSeats = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelNFreeSeats = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButtonResetFields = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabelNTotalSeats = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldNewNTotalSeats = new javax.swing.JTextField();
        jButtonSetNTotalSeats = new javax.swing.JButton();
        jPanelLeft = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabelDisplayMode = new javax.swing.JLabel();
        jButtonChangeMode = new javax.swing.JButton();
        jLabelResponse = new javax.swing.JLabel();
        jButtonConfirm = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea = new javax.swing.JTextArea();

        setTitle("Restaurant Service");
        setName("restaurantView");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jPanel2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
        jLabel5.setText("Seats: ");
        jLabel5.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel2.add(jLabel5);

        jLabelNBookedSeats.setText(Integer.toString(restManager.getNBookedSeats()));
        jLabelNBookedSeats.setForeground(java.awt.Color.gray);
        jLabelNBookedSeats.setFont(new java.awt.Font("Dialog", 1, 18));
        jPanel2.add(jLabelNBookedSeats);

        jLabel9.setText("Booked,    ");
        jPanel2.add(jLabel9);

        jLabelNPreparedSeats.setText(Integer.toString(restManager.getNPreparedSeats()));
        jLabelNPreparedSeats.setForeground(java.awt.Color.darkGray);
        jLabelNPreparedSeats.setFont(new java.awt.Font("Dialog", 1, 18));
        jPanel2.add(jLabelNPreparedSeats);

        jLabel2.setText("Prepared,    ");
        jPanel2.add(jLabel2);

        jLabelNFreeSeats.setText(Integer.toString(restManager.getNFreeSeats()));
        jLabelNFreeSeats.setForeground(new java.awt.Color(0, 153, 0));
        jLabelNFreeSeats.setFont(new java.awt.Font("Dialog", 1, 18));
        jPanel2.add(jLabelNFreeSeats);

        jLabel7.setText("Free            ");
        jPanel2.add(jLabel7);

        jButtonResetFields.setText("Reset Fields");
        jButtonResetFields.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonResetFieldsActionPerformed(evt);
            }
        });

        jPanel2.add(jButtonResetFields);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel1.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
        jLabel8.setText("Restaurant              ");
        jLabel8.setForeground(java.awt.Color.red);
        jLabel8.setFont(new java.awt.Font("Dialog", 1, 24));
        jPanel1.add(jLabel8);

        jLabel1.setText("Capacity:");
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel1);

        jLabelNTotalSeats.setText(Integer.toString(restManager.getNTotalSeats()));
        jLabelNTotalSeats.setForeground(java.awt.Color.darkGray);
        jLabelNTotalSeats.setFont(new java.awt.Font("Dialog", 1, 18));
        jPanel1.add(jLabelNTotalSeats);

        jLabel4.setText("seats");
        jLabel4.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel4);

        jLabel3.setText("                   New capacity:");
        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel3);

        jTextFieldNewNTotalSeats.setFont(new java.awt.Font("Dialog", 0, 18));
        jTextFieldNewNTotalSeats.setText(Integer.toString(restManager.getNTotalSeats()));
        jPanel1.add(jTextFieldNewNTotalSeats);

        jButtonSetNTotalSeats.setFont(new java.awt.Font("Dialog", 1, 14));
        jButtonSetNTotalSeats.setText("Set");
        jButtonSetNTotalSeats.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSetNTotalSeatsActionPerformed(evt);
            }
        });

        jPanel1.add(jButtonSetNTotalSeats);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanelLeft.setLayout(new javax.swing.BoxLayout(jPanelLeft, javax.swing.BoxLayout.Y_AXIS));

        jLabel10.setText("Mode:");
        jPanelLeft.add(jLabel10);

        jLabelDisplayMode.setText("Automatic");
        jLabelDisplayMode.setForeground(java.awt.Color.blue);
        jLabelDisplayMode.setFont(new java.awt.Font("Dialog", 1, 18));
        jPanelLeft.add(jLabelDisplayMode);

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

        jPanelLeft.add(jButtonChangeMode);

        jLabelResponse.setText("Response:");
        jPanelLeft.add(jLabelResponse);

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

        jPanelLeft.add(jButtonConfirm);

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

        jPanelLeft.add(jButtonCancel);

        getContentPane().add(jPanelLeft, java.awt.BorderLayout.WEST);

        jScrollPane1.setAutoscrolls(true);
        jTextArea.setEditable(false);
        jTextArea.setRows(10);
        jTextArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jScrollPane1.setViewportView(jTextArea);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    /**
     * Reset event handler.
     */
    private void jButtonResetFieldsActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonResetFieldsActionPerformed
        restManager.reset();
        updateFields();
    }//GEN-LAST:event_jButtonResetFieldsActionPerformed

    /**
     * Cancel event handler.
     */
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonCancelActionPerformed
        if (restManager.getIsPreparationWaiting())
        {
            Object preparation = restManager.getPreparation();
            try
            {
                restManager.setCommit(false);
                synchronized (preparation)
                {
                    preparation.notify();
                }
            }
            catch (Exception e)
            {
                System.err.println("RestaurantView.jButtonCancelActionPerformed(): Unable to notify preparation.");
            }
        }
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Confirm event handler.
     */
    private void jButtonConfirmActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonConfirmActionPerformed
        if (restManager.getIsPreparationWaiting())
        {
            Object preparation = restManager.getPreparation();
            try
            {
                restManager.setCommit(true);
                synchronized (preparation)
                {
                    preparation.notify();
                }
            }
            catch (Exception e)
            {
                System.err.println("RestaurantView.jButtonCancelActionPerformed(): Unable to notify preparation.");
            }
        }
    }//GEN-LAST:event_jButtonConfirmActionPerformed

    /**
     * ChangeMode event handler.
     */
    private void jButtonChangeModeActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonChangeModeActionPerformed
        if (restManager.isAutoCommitMode())
        {
            restManager.setAutoCommitMode(false);
        }
        else
        {
            restManager.setAutoCommitMode(true);
        }
        updateFields();
    }//GEN-LAST:event_jButtonChangeModeActionPerformed

    /**
     * Capacity change event handler.
     */
    private void jButtonSetNTotalSeatsActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonSetNTotalSeatsActionPerformed
        String strNSeats = jTextFieldNewNTotalSeats.getText();

        restManager.reset();
        int nFreeSeats = restManager.getNFreeSeats();

        jLabelNTotalSeats.setText(strNSeats);
        jLabelNFreeSeats.setText(Integer.toString(nFreeSeats));
    }//GEN-LAST:event_jButtonSetNTotalSeatsActionPerformed

    /**
     * Exit the application.
     */
    private void exitForm(java.awt.event.WindowEvent evt)
    {//GEN-FIRST:event_exitForm
        //System.exit(0); // disabled for embedding in application servers.
    }//GEN-LAST:event_exitForm

    /**
     * Add regular text to the central jTextArea.
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
        jLabelNTotalSeats.setText(Integer.toString(restManager.getNTotalSeats()));
        jTextFieldNewNTotalSeats.setText(Integer.toString(restManager.getNTotalSeats()));
        jLabelNPreparedSeats.setText(Integer.toString(restManager.getNPreparedSeats()));
        jLabelNFreeSeats.setText(Integer.toString(restManager.getNFreeSeats()));
        jLabelNBookedSeats.setText(Integer.toString(restManager.getNBookedSeats()));

        //update fields related to interactive mode.
        if (restManager.isAutoCommitMode())
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
    public static RestaurantView getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new RestaurantView(RestaurantManager.getSingletonInstance());
        }

        singletonInstance.show();
        return singletonInstance;
    }

    /**
     * A singleton instance of this class.
     */
    private static RestaurantView singletonInstance;


    // Variables declaration - automatically generated - do not modify

    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelNBookedSeats;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelNPreparedSeats;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelNFreeSeats;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JButton jButtonResetFields;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelNTotalSeats;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextFieldNewNTotalSeats;
    private javax.swing.JButton jButtonSetNTotalSeats;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabelDisplayMode;
    private javax.swing.JButton jButtonChangeMode;
    private javax.swing.JLabel jLabelResponse;
    private javax.swing.JButton jButtonConfirm;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea;

    // End of automatically generated variables declarations

    /**
     * The  {@link RestaurantManager} instance this view is bound to.
     */
    private RestaurantManager restManager;

    /**
     * The current color of the back button.
     */
    private java.awt.Color backButtonColor;
}
