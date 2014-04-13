package io.narayana.perf;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class X {

   public static BigInteger pfactorial(long n) {
        int threadCount = 10;
        long batchSize = (n + threadCount - 1) / threadCount;

        ExecutorService service = Executors.newFixedThreadPool(threadCount);
        try {
            List<Future<BigInteger>> results = new ArrayList<Future<BigInteger>>();
            for (long i = 1; i <= n; i += batchSize) {
                final long start = i;
                final long end = Math.min(n + 1, i + batchSize);
                results.add(service.submit(new Callable<BigInteger>() {
                    @Override
                    public BigInteger call() throws Exception {
                        BigInteger n = BigInteger.valueOf(start);
                        for (long j = start + 1; j < end; j++)
                            n = n.multiply(BigInteger.valueOf(j));
                        return n;
                    }
                }));
            }
            BigInteger result = BigInteger.ONE;
            for (Future<BigInteger> future : results) {
                result = result.multiply(future.get());
            }
            return result;
        } catch (Exception e) {
            throw new AssertionError(e);
        } finally {
            service.shutdown();
        }
    }
}
