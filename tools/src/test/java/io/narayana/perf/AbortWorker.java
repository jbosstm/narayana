package io.narayana.perf;

import java.util.concurrent.atomic.AtomicInteger;

public class AbortWorker implements Worker<String> {
    private static AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public String doWork(String context, int niters, Measurement<String> opts) {
        int sleep = callCount.incrementAndGet();

        if (sleep == 5) {
            opts.cancel(true);
            context = "cancelled";
        } else if (sleep > 10) {
            sleep = 10;
        }

        try {
            Thread.sleep(sleep * 10);
        } catch (InterruptedException e) {
        }

        return context;
    }

    @Override
    public String doWork(String context, int niters, Result<String> opts) {
        return null;
    }

    @Override
    public void init() {
    }

    @Override
    public void fini() {
    }
}
