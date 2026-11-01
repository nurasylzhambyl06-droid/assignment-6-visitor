package com.inspection.report;

/** The result of one named check: PASS or FAIL plus a short explanation. */
public class CheckResult {

    private final String name;
    private final boolean passed;
    private final String details;

    public CheckResult(String name, boolean passed, String details) {
        this.name = name;
        this.passed = passed;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        String status = passed ? "PASS" : "FAIL";
        return "[" + status + "] " + name + " - " + details;
    }
}
