/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: InternalGraphFrame.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.perfgraph;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;

public class InternalGraphFrame extends JInternalFrame implements ActionListener, ItemListener
{
	private final static String TRANSACTIONS_MENU_ITEM_TEXT = "Transactions";
	private final static String COMMITTED_TRANSACTIONS_MENU_ITEM_TEXT = "Committed Transactions";
	private final static String ABORTED_TRANSACTIONS_MENU_ITEM_TEXT = "Aborted Transactions";
	private final static String NESTED_TRANSACTIONS_MENU_ITEM_TEXT = "Nested Transactions";
	private final static String HEURISTICS_MENU_ITEM_TEXT = "Heuristics";
	private final static String SAVE_TO_CSV_MENU_ITEM_TEXT = "Save to .csv";
	private final static String START_TEXT = "Start";
	private final static String STOP_TEXT = "Stop";

	private static int openFrameCount = 0;

	private DataSampler _sampler = null;
    private JMenuItem	_startMenuItem = null;
	private JMenuItem	_stopMenuItem = null;
	private TimeSeriesCollection 	_dataset = new TimeSeriesCollection();

	public InternalGraphFrame()
	{
		super("Graph " + (++openFrameCount),
			  true,
			  true,
			  true,
			  false);

		this.setSize(400,300);
		this.setPreferredSize(new Dimension(400,300));
		this.setMinimumSize(new Dimension(400,300));

		JFreeChart chart = createChart(_dataset);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		setContentPane(chartPanel);

		_sampler = new DataSampler(SettingsPanel.getInterval() * 1000);

		setupChart(_dataset);

		this.setJMenuBar(createMenu());

		show();
	}

	private JFreeChart createChart(XYDataset dataset)
	{
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
			"Transaction Service Performance Graph",
			"Time",
			"Value",
			dataset,
			true,
			true,
			false
		);

		XYPlot plot = chart.getXYPlot();

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setTickUnit(new DateTickUnit(DateTickUnit.SECOND, 10,
										  new SimpleDateFormat("hh:mm:ss")));
		axis.setVerticalTickLabels(true);

		return chart;
	}


	/**
	 * Makes this internal frame
	 * invisible, unselected, and closed.
	 * If the frame is not already closed,
	 * this method fires an
	 * <code>INTERNAL_FRAME_CLOSED</code> event.
	 * The results of invoking this method are similar to
	 * <code>setClosed(true)</code>,
	 * but <code>dispose</code> always succeeds in closing
	 * the internal frame and does not fire
	 * an <code>INTERNAL_FRAME_CLOSING</code> event.
	 *
	 * @see InternalFrameEvent#INTERNAL_FRAME_CLOSED
	 * @see #setVisible
	 * @see #setSelected
	 * @see #setClosed
	 */
	public void dispose()
	{
		super.dispose();

		if ( _sampler != null )
		{
			_sampler.stopSampling();
		}
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();

		if ( actionCommand.equals(SAVE_TO_CSV_MENU_ITEM_TEXT) )
		{
			saveToCSV();
		}
		else
		if ( actionCommand.equals(STOP_TEXT) )
		{
			setSamplingState(false);
		}
		else
		if ( actionCommand.equals(START_TEXT) )
		{
			setSamplingState(true);
		}
	}

	private void setSamplingState(boolean sampling)
	{
		if ( sampling )
		{
			if ( !_sampler.isSampling() )
			{
				_startMenuItem.setEnabled(false);
				_stopMenuItem.setEnabled(true);

				_sampler.startSampling();
			}
		}
		else
		{
			if ( _sampler.isSampling() )
			{
				_startMenuItem.setEnabled(true);
				_stopMenuItem.setEnabled(false);

				_sampler.stopSampling();
			}
		}
	}
	private void saveToCSV()
	{
		setSamplingState(false);

		JFileChooser choose = new JFileChooser();

		if ( choose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION )
		{
			try
			{
				_sampler.saveToCSV(choose.getSelectedFile());
			}
			catch (java.io.IOException e)
			{
				e.printStackTrace(System.err);

				JOptionPane.showMessageDialog(this,"Failed to save data to CSV file '"+choose.getSelectedFile()+"'");
			}
		}

		setSamplingState(true);
	}

	/**
	 * Setup the graph's series'
	 */
	private void setupChart(TimeSeriesCollection collection)
	{
		// Do nothing
	}

	/**
	 * Invoked when an item has been selected or deselected by the user.
	 * The code written for this method performs the operations
	 * that need to occur when an item is selected (or deselected).
	 */
	public void itemStateChanged(ItemEvent e)
	{
		JCheckBoxMenuItem selected = (JCheckBoxMenuItem)e.getItemSelectable();

		boolean itemSelected = selected.isSelected();

		if ( selected.getText().equals( TRANSACTIONS_MENU_ITEM_TEXT ) )
		{
			if ( itemSelected )
			{
				_dataset.addSeries(_sampler.getSeries(DataSampler.NUMBER_OF_TRANSACTIONS_SERIES));
			}
			else
			{
				_dataset.removeSeries(_sampler.getSeries(DataSampler.NUMBER_OF_TRANSACTIONS_SERIES));
			}
		}
		else
		if ( selected.getText().equals( COMMITTED_TRANSACTIONS_MENU_ITEM_TEXT ) )
		{
			if ( itemSelected )
			{
				_dataset.addSeries(_sampler.getSeries(DataSampler.NUMBER_OF_COMMITTED_TRANSACTIONS_SERIES));
			}
			else
			{
				_dataset.removeSeries(_sampler.getSeries(DataSampler.NUMBER_OF_COMMITTED_TRANSACTIONS_SERIES));
			}
		}
		else
		if ( selected.getText().equals( ABORTED_TRANSACTIONS_MENU_ITEM_TEXT ) )
		{
			if ( itemSelected )
			{
				_dataset.addSeries(_sampler.getSeries(DataSampler.NUMBER_OF_ABORTED_TRANSACTIONS_SERIES));
			}
			else
			{
				_dataset.removeSeries(_sampler.getSeries(DataSampler.NUMBER_OF_ABORTED_TRANSACTIONS_SERIES));
			}
		}
		else
		if ( selected.getText().equals( NESTED_TRANSACTIONS_MENU_ITEM_TEXT ) )
		{
			if ( itemSelected )
			{
				_dataset.addSeries(_sampler.getSeries(DataSampler.NUMBER_OF_NESTED_TRANSACTIONS_SERIES));
			}
			else
			{
				_dataset.removeSeries(_sampler.getSeries(DataSampler.NUMBER_OF_NESTED_TRANSACTIONS_SERIES));
			}
		}
		else
		if ( selected.getText().equals( HEURISTICS_MENU_ITEM_TEXT ) )
		{
			if ( itemSelected )
			{
				_dataset.addSeries(_sampler.getSeries(DataSampler.NUMBER_OF_HEURISTICS_SERIES));
			}
			else
			{
				_dataset.removeSeries(_sampler.getSeries(DataSampler.NUMBER_OF_HEURISTICS_SERIES));
			}
		}
	}

	private JMenuBar createMenu()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu menu = null;
		JCheckBoxMenuItem item = null;

		menubar.add( menu = new JMenu("Series") );

        menu.add( item = new JCheckBoxMenuItem(TRANSACTIONS_MENU_ITEM_TEXT) );
		item.addItemListener(this);
		menu.add( item = new JCheckBoxMenuItem(COMMITTED_TRANSACTIONS_MENU_ITEM_TEXT) );
		item.addItemListener(this);
		menu.add( item = new JCheckBoxMenuItem(ABORTED_TRANSACTIONS_MENU_ITEM_TEXT) );
		item.addItemListener(this);
		menu.add( item = new JCheckBoxMenuItem(NESTED_TRANSACTIONS_MENU_ITEM_TEXT) );
		item.addItemListener(this);
		menu.add( item = new JCheckBoxMenuItem(HEURISTICS_MENU_ITEM_TEXT) );
		item.addItemListener(this);

		JMenuItem menuItem;

		menubar.add( menu = new JMenu("Data") );
		menu.add( menuItem = new JMenuItem(SAVE_TO_CSV_MENU_ITEM_TEXT));
		menuItem.addActionListener(this);

		menubar.add( menu = new JMenu("Sampling") );
		menu.add( _startMenuItem = new JMenuItem(START_TEXT));
		_startMenuItem.setEnabled(false);
		_startMenuItem.addActionListener(this);

		menu.add( _stopMenuItem = new JMenuItem(STOP_TEXT));
		_stopMenuItem.addActionListener(this);
        return menubar;
	}
}
