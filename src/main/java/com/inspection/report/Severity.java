package com.inspection.report;

/**
 * Severity of a security finding. Each level has a penalty that is
 * subtracted from the security score of 100.
 */
public enum Severity {
    LOW(3),
    MEDIUM(8),
    HIGH(15),
    CRITICAL(25);

    private final int penalty;

    Severity(int penalty) {
        this.penalty = penalty;
    }

    public int getPenalty() {
        return penalty;
    }
}
