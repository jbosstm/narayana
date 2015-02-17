package com.arjuna.ats.arjuna.coordinator.internal;

public class TxCommitStatistic {
    private final long numberOfCommittedTransactions, averageCommitTime;

    public TxCommitStatistic(long numberOfCommittedTransactions, long averageCommitTime) {
        this.numberOfCommittedTransactions = numberOfCommittedTransactions;
        this.averageCommitTime = averageCommitTime;
    }

    public long getNumberOfCommittedTransactions() {
        return numberOfCommittedTransactions;
    }

    public long getAverageCommitTime() {
        return averageCommitTime;
    }
}
