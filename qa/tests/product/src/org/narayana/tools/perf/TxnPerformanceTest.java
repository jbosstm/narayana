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
package org.narayana.tools.perf;

import java.lang.reflect.Constructor;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Date;

public class TxnPerformanceTest
{
	private static final String syntax = "-p <product> | -i <iterations> -t <threads>: ";

	public static void main(String[] args) throws Exception {
		int iterations = 200000;
		int threads = 100;
		String opt;
		String products[] = {
			"org.narayana.tools.perf.BitronixWorkerTask",
			"org.narayana.tools.perf.AtomikosWorkerTask",
			"org.narayana.tools.perf.JotmWorkerTask",
			"org.narayana.tools.perf.NarayanaWorkerTask",
		};

		if (args.length != 0) {
			String[] opts = args[0].trim().split("\\s+");
/*
			for (int i = 0; i < opts.length; i++) {
				if (opts[i].startsWith("-")) {
					opt = opts[i++];

					if (i < opts.length) {
						if ("-p".equals(opt))
							products = opts[i].split(",");
						else if ("-i".equals(opt))
							iterations = Integer.valueOf(opts[i]);
						else if ("-t".equals(opt))
							threads = Integer.valueOf(opts[i]);
                        else
                            fatal("Syntax error: ");
					} else {
                         fatal("Syntax error: ");
                    }
				} else {
                    fatal("Syntax error: ");
                }
			}*/

			for (int i = 0; i < opts.length; i++) {
				if (opts[i].startsWith("-")) {
					opt = opts[i++];

					if (i < opts.length) {
						if ("-p".equals(opt)) {
							products = opts[i].split(",");
                            continue;
                        } else if ("-i".equals(opt)) {
							iterations = Integer.valueOf(opts[i]);
                            continue;
                        } else if ("-t".equals(opt)) {
							threads = Integer.valueOf(opts[i]);
                            continue;
                        }
                    }
				}

                System.out.println("Syntax error: " + syntax);
                System.exit(1);
			}
		}

		TaskResult results[] = new TaskResult[products.length];

        System.out.println(iterations+ " transactions, " + threads + " threads");

		for (int i = 0; i < products.length; i++)
			results[i] = testLoop(products[i], iterations, threads);

		for (int i = 0; i < products.length; i++)
			System.out.println(results[i].toString());
	}

	private static WorkerTask newWorker(
		String className, CyclicBarrier cyclicBarrier, AtomicInteger count, int batch_size) throws Exception {

		Class classDef = Class.forName(className);
		Constructor constructor = classDef.getDeclaredConstructor(
			new Class[]{CyclicBarrier.class, AtomicInteger.class, int.class});
		Object[] args = {cyclicBarrier, count, batch_size};

		return (WorkerTask) constructor.newInstance(args);
	}

	private static TaskResult testLoop(String workerClassName, int iterations, int threads) throws Exception {
		int NUM_TX = iterations;
		int BATCH_SIZE = 100;
		AtomicInteger count = new AtomicInteger(NUM_TX/BATCH_SIZE);
		final int nThreads = threads;
		CyclicBarrier cyclicBarrier = new CyclicBarrier(nThreads +1); // workers + self
		ExecutorService executorService = Executors.newCachedThreadPool();
		WorkerTask task = TxnPerformanceTest.newWorker(workerClassName, cyclicBarrier, count, BATCH_SIZE);

        task.init();

		for(int i = 0; i < nThreads; i++) {
			executorService.execute(task);
		}

		System.out.print(new Date() + " " + workerClassName);
		long start = System.nanoTime();
		cyclicBarrier.await();
		cyclicBarrier.await();

		long end = System.nanoTime();
		long duration_ms = (end - start) / 1000000L;

		executorService.shutdown();
		task.fini();

		return new TaskResult(workerClassName, iterations, threads, duration_ms);
	}

	static class TaskResult {
		String product;
		long duration_ms;
		int iterations;
		int threads;

		TaskResult(String product, int iterations, int threads, long duration_ms) {
			this.product = product;
			this.iterations = iterations;
			this.threads = threads;
			this.duration_ms =  duration_ms;
		}

		public StringBuilder toString(StringBuilder sb) {
			sb.append(product);
			sb.append("\n  total time (ms): ").append(duration_ms);
			sb.append("\naverage time (ms): ").append((1.0*duration_ms)/iterations);
			sb.append("\ntx / second: ").append((1000.0/((1.0*duration_ms)/iterations)));
			sb.append('\n').append(threads).append(" threads").append('\n');

			return sb;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(product).append(": ")
                    .append((1000.0/((1.0*duration_ms)/iterations))).append(" tx / second");

			return sb.toString();
		}
	}
}
