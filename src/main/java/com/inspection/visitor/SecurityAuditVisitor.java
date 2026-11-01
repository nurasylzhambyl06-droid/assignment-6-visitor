package com.inspection.visitor;

import com.inspection.element.ConfigurationFile;
import com.inspection.element.DependencyFile;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import com.inspection.report.ReportFormat;
import com.inspection.report.SecurityFinding;
import com.inspection.report.Severity;

import java.util.ArrayList;
import java.util.List;

/**
 * Looks for security problems and calculates a security score.
 *
 * SCORING RULES
 *   score = 100 - (sum of penalties of all findings), but never below 0.
 *   Penalties: LOW = 3, MEDIUM = 8, HIGH = 15, CRITICAL = 25.
 *
 * SEVERITY RULES
 *   CRITICAL: hardcoded credentials in source, exposed credentials in config
 *   HIGH:     vulnerable dependency, high-risk source file (complexity above 20)
 *   MEDIUM:   insecure configuration
 */
public class SecurityAuditVisitor implements ProjectVisitor {

    public static final int HIGH_RISK_COMPLEXITY = 20;

    private final List<SecurityFinding> findings = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        String name = sourceCodeFile.getFileName();
        if (sourceCodeFile.hasHardcodedCredentials()) {
            addFinding(Severity.CRITICAL, name, "Hardcoded credentials found in source code");
        }
        if (sourceCodeFile.getComplexity() > HIGH_RISK_COMPLEXITY) {
            addFinding(Severity.HIGH, name, "High-risk source file (complexity "
                    + sourceCodeFile.getComplexity() + " is above " + HIGH_RISK_COMPLEXITY + ")");
        }
    }

    @Override
    public void visit(TestFile testFile) {
        // Test files are not part of the security audit.
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        String name = configurationFile.getFileName();
        if (configurationFile.hasExposedCredentials()) {
            addFinding(Severity.CRITICAL, name, "Credentials are exposed in the configuration file");
        }
        if (configurationFile.isInsecureConfiguration()) {
            addFinding(Severity.MEDIUM, name, "Insecure configuration");
        }
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        if (dependencyFile.isVulnerable()) {
            addFinding(Severity.HIGH, dependencyFile.getDependencyName(),
                    "Vulnerable dependency (version " + dependencyFile.getVersion() + ")");
        }
    }

    @Override
    public void visit(Directory directory) {
        // Directory itself has nothing to check; its children are visited by Directory.accept().
    }

    private void addFinding(Severity severity, String location, String message) {
        findings.add(new SecurityFinding(severity, location, message));
    }

    public List<SecurityFinding> getFindings() {
        return new ArrayList<>(findings);
    }

    public int getFindingCount(Severity severity) {
        int count = 0;
        for (SecurityFinding finding : findings) {
            if (finding.getSeverity() == severity) {
                count++;
            }
        }
        return count;
    }

    public int getCriticalCount() {
        return getFindingCount(Severity.CRITICAL);
    }

    public int getSecurityScore() {
        int score = 100;
        for (SecurityFinding finding : findings) {
            score = score - finding.getSeverity().getPenalty();
        }
        if (score < 0) {
            score = 0;
        }
        return score;
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("1. SECURITY AUDIT REPORT"));
        if (findings.isEmpty()) {
            sb.append("No security findings.\n");
        }
        for (SecurityFinding finding : findings) {
            sb.append(finding).append("\n");
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append("CRITICAL: ").append(getFindingCount(Severity.CRITICAL));
        sb.append("  HIGH: ").append(getFindingCount(Severity.HIGH));
        sb.append("  MEDIUM: ").append(getFindingCount(Severity.MEDIUM));
        sb.append("  LOW: ").append(getFindingCount(Severity.LOW)).append("\n");
        sb.append("Critical issues : ").append(getCriticalCount()).append("\n");
        sb.append("Security score  : ").append(getSecurityScore()).append(" / 100\n");
        return sb.toString();
    }
}
