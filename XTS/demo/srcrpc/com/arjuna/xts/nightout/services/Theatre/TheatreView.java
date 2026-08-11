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
 * TheatreView.java
 *
 * Copyright (c) 2003 Arjuna Technologies Ltd.
 *
 * $Id: TheatreView.java,v 1.3 2004/04/21 13:09:21 jhalliday Exp $
 *
 */

package com.arjuna.xts.nightout.services.Theatre;

/**
 * The visual interface (GUI) for the Theatre Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@arjuna.com)
 * @version $Revision: 1.3 $
 */
public class TheatreView extends javax.swing.JFrame
{

    // Note: Some parts of this file were auto-generated
    // by NetBeans 3.3 FormEditor (http://www.netbeans.org)

    /**
     * Create a new TheatreView instance.
     *
     * @param tManager The {@link TheatreManager} instance to bind to
     */
    public TheatreView(TheatreManager tManager)
    {
        theatreManager = tManager;
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabelNBookedSeatsCircle = new javax.swing.JLabel();
        jLabelNConfirmedSeatsCircle = new javax.swing.JLabel();
        jLabelNFreeSeatsCircle = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabelNBookedSeatsStalls = new javax.swing.JLabel();
        jLabelNConfirmedSeatsStalls = new javax.swing.JLabel();
        jLabelNFreeSeatsStalls = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabelNBookedSeatsBalcony = new javax.swing.JLabel();
        jLabelNConfirmedSeatsBalcony = new javax.swing.JLabel();
        jLabelNFreeSeatsBalcony = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jButtonResetFields = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabelNTotalSeatsCircle = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabelNTotalSeatsStalls = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabelNTotalSeatsBalcony = new javax.swing.JLabel();
        jTextFieldNewNTotalSeats = new javax.swing.JTextField();
        jButtonSetCircle = new javax.swing.JButton();
        jButtonSetSalls = new javax.swing.JButton();
        jButtonSetBalcony = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea = new javax.swing.JTextArea();
        jPanelLeft = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabelDisplayMode = new javax.swing.JLabel();
        jButtonChangeMode = new javax.swing.JButton();
        jLabelResponse = new javax.swing.JLabel();
        jButtonConfirm = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setTitle("Theatre Service");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitForm(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
        jLabel1.setText("SEATS");
        jLabel1.setForeground(java.awt.Color.darkGray);
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel1);

        jLabel3.setText("(booked, ");
        jLabel3.setForeground(java.awt.Color.gray);
        jPanel1.add(jLabel3);

        jLabel29.setText("confirmed, ");
        jLabel29.setForeground(new java.awt.Color(0, 51, 204));
        jPanel1.add(jLabel29);

        jLabel30.setText("free)       ");
        jLabel30.setForeground(new java.awt.Color(0, 153, 0));
        jPanel1.add(jLabel30);

        jLabel25.setText("Circle (");
        jLabel25.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel25);

        jLabelNBookedSeatsCircle.setText(Integer.toString(theatreManager.getNBookedSeats(theatreManager.CIRCLE)));
        jLabelNBookedSeatsCircle.setForeground(java.awt.Color.gray);
        jLabelNBookedSeatsCircle.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNBookedSeatsCircle);

        jLabelNConfirmedSeatsCircle.setText(Integer.toString(theatreManager.getNCommittedSeats(theatreManager.CIRCLE)));
        jLabelNConfirmedSeatsCircle.setForeground(new java.awt.Color(0, 51, 204));
        jLabelNConfirmedSeatsCircle.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNConfirmedSeatsCircle);

        jLabelNFreeSeatsCircle.setText(Integer.toString(theatreManager.getNFreeSeats(theatreManager.CIRCLE)));
        jLabelNFreeSeatsCircle.setForeground(new java.awt.Color(0, 153, 0));
        jLabelNFreeSeatsCircle.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNFreeSeatsCircle);

        jLabel35.setText("),  Stalls (");
        jLabel35.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel35);

        jLabelNBookedSeatsStalls.setText(Integer.toString(theatreManager.getNBookedSeats(theatreManager.STALLS)));
        jLabelNBookedSeatsStalls.setForeground(java.awt.Color.gray);
        jLabelNBookedSeatsStalls.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNBookedSeatsStalls);

        jLabelNConfirmedSeatsStalls.setText(Integer.toString(theatreManager.getNCommittedSeats(theatreManager.STALLS)));
        jLabelNConfirmedSeatsStalls.setForeground(new java.awt.Color(0, 51, 204));
        jLabelNConfirmedSeatsStalls.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNConfirmedSeatsStalls);

        jLabelNFreeSeatsStalls.setText(Integer.toString(theatreManager.getNFreeSeats(theatreManager.STALLS)));
        jLabelNFreeSeatsStalls.setForeground(new java.awt.Color(0, 153, 0));
        jLabelNFreeSeatsStalls.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNFreeSeatsStalls);

        jLabel28.setText("),  Balcony (");
        jLabel28.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel28);

        jLabelNBookedSeatsBalcony.setText(Integer.toString(theatreManager.getNBookedSeats(theatreManager.BALCONY)));
        jLabelNBookedSeatsBalcony.setForeground(java.awt.Color.gray);
        jLabelNBookedSeatsBalcony.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNBookedSeatsBalcony);

        jLabelNConfirmedSeatsBalcony.setText(Integer.toString(theatreManager.getNCommittedSeats(theatreManager.BALCONY)));
        jLabelNConfirmedSeatsBalcony.setForeground(new java.awt.Color(51, 0, 204));
        jLabelNConfirmedSeatsBalcony.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNConfirmedSeatsBalcony);

        jLabelNFreeSeatsBalcony.setText(Integer.toString(theatreManager.getNFreeSeats(theatreManager.BALCONY)));
        jLabelNFreeSeatsBalcony.setForeground(new java.awt.Color(0, 153, 0));
        jLabelNFreeSeatsBalcony.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel1.add(jLabelNFreeSeatsBalcony);

        jLabel34.setText(")     ");
        jLabel34.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel1.add(jLabel34);

        jButtonResetFields.setText("Reset Fields");
        jButtonResetFields.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonResetFieldsActionPerformed(evt);
            }
        });

        jPanel1.add(jButtonResetFields);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
        jLabel10.setText("Theatre      ");
        jLabel10.setForeground(java.awt.Color.red);
        jLabel10.setFont(new java.awt.Font("Dialog", 1, 24));
        jPanel2.add(jLabel10);

        jLabel13.setText("TOTAL SEATS   ");
        jLabel13.setForeground(java.awt.Color.darkGray);
        jLabel13.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel2.add(jLabel13);

        jLabel18.setText("Circle:");
        jLabel18.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel2.add(jLabel18);

        jLabelNTotalSeatsCircle.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.CIRCLE)));
        jLabelNTotalSeatsCircle.setForeground(java.awt.Color.darkGray);
        jLabelNTotalSeatsCircle.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel2.add(jLabelNTotalSeatsCircle);

        jLabel19.setText("Stalls:");
        jLabel19.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel2.add(jLabel19);

        jLabelNTotalSeatsStalls.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.STALLS)));
        jLabelNTotalSeatsStalls.setForeground(java.awt.Color.darkGray);
        jLabelNTotalSeatsStalls.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel2.add(jLabelNTotalSeatsStalls);

        jLabel20.setText("Balcony:");
        jLabel20.setFont(new java.awt.Font("Dialog", 1, 14));
        jPanel2.add(jLabel20);

        jLabelNTotalSeatsBalcony.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.BALCONY)));
        jLabelNTotalSeatsBalcony.setForeground(java.awt.Color.darkGray);
        jLabelNTotalSeatsBalcony.setFont(new java.awt.Font("Dialog", 0, 14));
        jPanel2.add(jLabelNTotalSeatsBalcony);

        jTextFieldNewNTotalSeats.setFont(new java.awt.Font("Dialog", 0, 18));
        jTextFieldNewNTotalSeats.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.CIRCLE)));
        jPanel2.add(jTextFieldNewNTotalSeats);

        jButtonSetCircle.setFont(new java.awt.Font("Dialog", 0, 14));
        jButtonSetCircle.setText("Set Circle");
        jButtonSetCircle.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSetCircleActionPerformed(evt);
            }
        });

        jPanel2.add(jButtonSetCircle);

        jButtonSetSalls.setFont(new java.awt.Font("Dialog", 0, 14));
        jButtonSetSalls.setText(" Set Stalls");
        jButtonSetSalls.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSetSallsActionPerformed(evt);
            }
        });

        jPanel2.add(jButtonSetSalls);

        jButtonSetBalcony.setFont(new java.awt.Font("Dialog", 0, 14));
        jButtonSetBalcony.setText("Set Balcony");
        jButtonSetBalcony.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSetBalconyActionPerformed(evt);
            }
        });

        jPanel2.add(jButtonSetBalcony);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jScrollPane1.setAutoscrolls(true);
        jTextArea.setEditable(false);
        jTextArea.setRows(10);
        jTextArea.setMargin(new java.awt.Insets(5, 5, 5, 5));
        jScrollPane1.setViewportView(jTextArea);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanelLeft.setLayout(new javax.swing.BoxLayout(jPanelLeft, javax.swing.BoxLayout.Y_AXIS));

        jLabel15.setText("Mode:");
        jPanelLeft.add(jLabel15);

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

        pack();
    }//GEN-END:initComponents

    /**
     * Reset event handler.
     */
    private void jButtonResetFieldsActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonResetFieldsActionPerformed
        theatreManager.setToDefault();
        updateFields();
    }//GEN-LAST:event_jButtonResetFieldsActionPerformed

    /**
     * Cancel event handler.
     */
    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonCancelActionPerformed
        if (theatreManager.getIsPreparationWaiting())
        {
            Object preparation = theatreManager.getPreparation();
            try
            {
                theatreManager.setCommit(false);
                synchronized (preparation)
                {
                    preparation.notify();
                }
            }
            catch (Exception e)
            {
                System.err.println("TheatreView.jButtonCancelActionPerformed(): Unable to notify preparation.");
            }
        }
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * Confirm event handler.
     */
    private void jButtonConfirmActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonConfirmActionPerformed
        if (theatreManager.getIsPreparationWaiting())
        {
            Object preparation = theatreManager.getPreparation();
            try
            {
                theatreManager.setCommit(true);
                synchronized (preparation)
                {
                    preparation.notify();
                }
            }
            catch (Exception e)
            {
                System.err.println("TheatreView.jButtonCancelActionPerformed(): Unable to notify preparation.");
            }
        }
    }//GEN-LAST:event_jButtonConfirmActionPerformed

    /**
     * ChangeMode event handler.
     */
    private void jButtonChangeModeActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonChangeModeActionPerformed
        if (theatreManager.isAutoCommitMode())
            theatreManager.setAutoCommitMode(false);
        else
            theatreManager.setAutoCommitMode(true);
        updateFields();
    }//GEN-LAST:event_jButtonChangeModeActionPerformed

    /**
     * Seat reservation event handler.
     */
    private void jButtonSetCircleActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonSetCircleActionPerformed
        String strNSeats = jTextFieldNewNTotalSeats.getText();

        theatreManager.newCapacity(theatreManager.CIRCLE, Integer.parseInt(strNSeats));
        int nFreeSeats = theatreManager.getNFreeSeats(theatreManager.CIRCLE);

        jLabelNTotalSeatsCircle.setText(strNSeats);
        jLabelNFreeSeatsCircle.setText(Integer.toString(nFreeSeats));
    }//GEN-LAST:event_jButtonSetCircleActionPerformed

    /**
     * Seat reservation event handler.
     */
    private void jButtonSetSallsActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonSetSallsActionPerformed
        String strNSeats = jTextFieldNewNTotalSeats.getText();

        theatreManager.newCapacity(theatreManager.STALLS, Integer.parseInt(strNSeats));
        int nFreeSeats = theatreManager.getNFreeSeats(theatreManager.STALLS);

        jLabelNTotalSeatsStalls.setText(strNSeats);
        jLabelNFreeSeatsStalls.setText(Integer.toString(nFreeSeats));
    }//GEN-LAST:event_jButtonSetSallsActionPerformed

    /**
     * Seat reservation event handler.
     */
    private void jButtonSetBalconyActionPerformed(java.awt.event.ActionEvent evt)
    {//GEN-FIRST:event_jButtonSetBalconyActionPerformed
        String strNSeats = jTextFieldNewNTotalSeats.getText();

        theatreManager.newCapacity(theatreManager.BALCONY, Integer.parseInt(strNSeats));
        int nFreeSeats = theatreManager.getNFreeSeats(theatreManager.BALCONY);

        jLabelNTotalSeatsBalcony.setText(strNSeats);
        jLabelNFreeSeatsBalcony.setText(Integer.toString(nFreeSeats));
    }//GEN-LAST:event_jButtonSetBalconyActionPerformed

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
        jLabelNTotalSeatsCircle.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.CIRCLE)));
        jLabelNTotalSeatsStalls.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.STALLS)));
        jLabelNTotalSeatsBalcony.setText(Integer.toString(theatreManager.getNTotalSeats(theatreManager.BALCONY)));
        jTextFieldNewNTotalSeats.setText(jLabelNTotalSeatsCircle.getText());
        jLabelNBookedSeatsCircle.setText(Integer.toString(theatreManager.getNBookedSeats(theatreManager.CIRCLE)));
        jLabelNBookedSeatsStalls.setText(Integer.toString(theatreManager.getNBookedSeats(theatreManager.STALLS)));
        jLabelNBookedSeatsBalcony.setText(Integer.toString(theatreManager.getNBookedSeats(theatreManager.BALCONY)));
        jLabelNConfirmedSeatsCircle.setText(Integer.toString(theatreManager.getNCommittedSeats(theatreManager.CIRCLE)));
        jLabelNConfirmedSeatsStalls.setText(Integer.toString(theatreManager.getNCommittedSeats(theatreManager.STALLS)));
        jLabelNConfirmedSeatsBalcony.setText(Integer.toString(theatreManager.getNCommittedSeats(theatreManager.BALCONY)));
        jLabelNFreeSeatsCircle.setText(Integer.toString(theatreManager.getNFreeSeats(theatreManager.CIRCLE)));
        jLabelNFreeSeatsStalls.setText(Integer.toString(theatreManager.getNFreeSeats(theatreManager.STALLS)));
        jLabelNFreeSeatsBalcony.setText(Integer.toString(theatreManager.getNFreeSeats(theatreManager.BALCONY)));


        //update fields related to interactive mode
        if (theatreManager.isAutoCommitMode())
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
    public static TheatreView getSingletonInstance()
    {
        if (singletonInstance == null)
        {
            singletonInstance = new TheatreView(TheatreManager.getSingletonInstance());
        }

        singletonInstance.show();
        return singletonInstance;
    }

    /**
     * A singleton instance of this class.
     */
    private static TheatreView singletonInstance;

    // Variables declaration - automatically generated - do not modify

    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabelNBookedSeatsCircle;
    private javax.swing.JLabel jLabelNConfirmedSeatsCircle;
    private javax.swing.JLabel jLabelNFreeSeatsCircle;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabelNBookedSeatsStalls;
    private javax.swing.JLabel jLabelNConfirmedSeatsStalls;
    private javax.swing.JLabel jLabelNFreeSeatsStalls;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabelNBookedSeatsBalcony;
    private javax.swing.JLabel jLabelNConfirmedSeatsBalcony;
    private javax.swing.JLabel jLabelNFreeSeatsBalcony;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JButton jButtonResetFields;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabelNTotalSeatsCircle;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabelNTotalSeatsStalls;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabelNTotalSeatsBalcony;
    private javax.swing.JTextField jTextFieldNewNTotalSeats;
    private javax.swing.JButton jButtonSetCircle;
    private javax.swing.JButton jButtonSetSalls;
    private javax.swing.JButton jButtonSetBalcony;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabelDisplayMode;
    private javax.swing.JButton jButtonChangeMode;
    private javax.swing.JLabel jLabelResponse;
    private javax.swing.JButton jButtonConfirm;
    private javax.swing.JButton jButtonCancel;

    // End of automatically generated variables declarations

    /**
     * The  {@link TheatreManager} instance this view is bound to.
     */
    private TheatreManager theatreManager;

    /**
     * The current color of the back button.
     */
    private java.awt.Color backButtonColor;
}
