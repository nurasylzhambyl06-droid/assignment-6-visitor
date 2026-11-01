package com.inspection.report;

/** One security problem found by SecurityAuditVisitor. */
public class SecurityFinding {

    private final Severity severity;
    private final String location;
    private final String message;

    public SecurityFinding(Severity severity, String location, String message) {
        this.severity = severity;
        this.location = location;
        this.message = message;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + location + " - " + message;
    }
}
