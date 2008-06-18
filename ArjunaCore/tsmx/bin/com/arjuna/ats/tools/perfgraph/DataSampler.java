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
 * $Id: DataSampler.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tools.perfgraph;

import com.arjuna.ats.tsmx.TransactionServiceMX;

import javax.management.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.Date;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Second;

public class DataSampler implements Runnable
{
	private final static String THREAD_NAME = "DataSamplerThread";

    private final static String[] SERIES_NAME = { "Transactions Created", "Nested Transactions Created", "Heuristics Raised", "Committed Transactions", "Aborted Transactions" };

	public final static int NUMBER_OF_TRANSACTIONS_SERIES = 0;
	public final static int NUMBER_OF_NESTED_TRANSACTIONS_SERIES = 1;
	public final static int NUMBER_OF_HEURISTICS_SERIES = 2;
	public final static int NUMBER_OF_COMMITTED_TRANSACTIONS_SERIES = 3;
	public final static int NUMBER_OF_ABORTED_TRANSACTIONS_SERIES = 4;

	private boolean 		_sampling = true;
	private TimeSeries[]	_dataSeries = new TimeSeries[5];
	private long			_interval;
	private Thread			_samplingThread = null;


	public DataSampler(long interval)
	{
		_interval = interval;

		for (int count=0;count<_dataSeries.length;count++)
		{
			_dataSeries[count] = new TimeSeries(SERIES_NAME[count], Second.class);
			_dataSeries[count].setMaximumItemCount(SettingsPanel.getNumberOfSamples());
		}

		startSampling();
	}

	public TimeSeries getSeries(int seriesId)
	{
		return _dataSeries[seriesId];
	}

	public void stopSampling()
	{
		if ( _samplingThread != null )
		{
			try
			{
				_sampling = false;
				_samplingThread.interrupt();
				_samplingThread.join();
			}
			catch (Exception e)
			{
				// Ignore
			}

			_samplingThread = null;
		}
	}

	public void startSampling()
	{
		if ( _samplingThread == null )
		{
			_sampling = true;
			_samplingThread = new Thread(this);
			_samplingThread.setName( THREAD_NAME );
			_samplingThread.start();
		}
	}

	public void run()
	{
		String objectName = TransactionServiceMX.getTransactionServiceMX().getObjectName("performancestatistics");

        while (_sampling)
		{
			try
			{
				Second now = new Second(new Date());
				Integer numberOfTransactions = (Integer)TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfTransactions");
	            _dataSeries[NUMBER_OF_TRANSACTIONS_SERIES].add(now, numberOfTransactions.intValue());

				Integer numberOfNestedTransactions = (Integer)TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfNestedTransactions");
				_dataSeries[NUMBER_OF_NESTED_TRANSACTIONS_SERIES].add(now, numberOfNestedTransactions.intValue());

				Integer numberOfHeuristics = (Integer)TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfHeuristics");
				_dataSeries[NUMBER_OF_HEURISTICS_SERIES].add(now, numberOfHeuristics.intValue());

				Integer numberOfCommittedTransactions = (Integer)TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfCommittedTransactions");
				_dataSeries[NUMBER_OF_COMMITTED_TRANSACTIONS_SERIES].add(now, numberOfCommittedTransactions.intValue());

				Integer numberOfAbortedTransactions = (Integer)TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfAbortedTransactions");
				_dataSeries[NUMBER_OF_ABORTED_TRANSACTIONS_SERIES].add(now, numberOfAbortedTransactions.intValue());
			}
			catch (Exception e)
			{
				System.err.println("Failed to retrieve data: "+e);
			}

			try
			{
				Thread.sleep(_interval);
			}
			catch (InterruptedException e)
			{
				// Ignore it
			}
		}
	}

	public boolean isSampling()
	{
		return _samplingThread != null;
	}

	public void saveToCSV(File selectedFile) throws java.io.IOException
	{
		PrintWriter out = new PrintWriter(new FileOutputStream(selectedFile));

		/** Output column names **/
		for (int count=0;count<_dataSeries.length-1;count++)
		{
			out.print(_dataSeries[count].getKey()+",");
		}

        out.println(_dataSeries[_dataSeries.length-1].getKey());

		for (int count=0;count<_dataSeries[0].getItemCount();count++)
		{
			for (int series=0;series<_dataSeries.length;series++)
			{
				out.print(_dataSeries[series].getDataItem(count).getValue().longValue());

				if ( series < (_dataSeries.length-1) )
				{
					out.print(", ");
				}
			}

			out.println();
		}

		out.close();
	}
}
