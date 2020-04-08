package com.arjuna.ats.jta.logging;

public class RecoveryRequired {

    private static boolean recoveryProblems;

    public static boolean isRecoveryProblems() {
        return recoveryProblems;
    }

    public static void setRecoveryProblems(boolean recoveryProblems) {
        RecoveryRequired.recoveryProblems = recoveryProblems;
    }
}
