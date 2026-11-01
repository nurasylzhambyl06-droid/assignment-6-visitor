package com.inspection.health;

import com.inspection.report.CheckResult;
import com.inspection.report.ReportFormat;
import com.inspection.visitor.ComplianceVisitor;
import com.inspection.visitor.DeploymentReadinessVisitor;
import com.inspection.visitor.QualityGateVisitor;
import com.inspection.visitor.SecurityAuditVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines four existing scores into one overall score from 0 to 100.
 * This is plain arithmetic on results that the visitors already calculated.
 * It is NOT a visitor and it does not replace any of them.
 *
 * FORMULA
 *   overall = security * 0.35 + quality * 0.25 + compliance * 0.20 + deployment * 0.20
 *
 *   security   = security score of SecurityAuditVisitor
 *   quality    = quality score of QualityGateVisitor (share of gate rules that passed)
 *   compliance = compliance score of ComplianceVisitor (share of rules that passed)
 *   deployment = readiness score of DeploymentReadinessVisitor
 *
 * STATUS
 *   85 - 100 = EXCELLENT
 *   70 - 84  = GOOD
 *   50 - 69  = MODERATE
 *   0 - 49   = CRITICAL
 */
public class InfrastructureHealthScore {

    public static final double SECURITY_WEIGHT = 0.35;
    public static final double QUALITY_WEIGHT = 0.25;
    public static final double COMPLIANCE_WEIGHT = 0.20;
    public static final double DEPLOYMENT_WEIGHT = 0.20;

    private final SecurityAuditVisitor security;
    private final QualityGateVisitor qualityGate;
    private final ComplianceVisitor compliance;
    private final DeploymentReadinessVisitor deployment;

    public InfrastructureHealthScore(SecurityAuditVisitor security, QualityGateVisitor qualityGate,
                                     ComplianceVisitor compliance, DeploymentReadinessVisitor deployment) {
        this.security = security;
        this.qualityGate = qualityGate;
        this.compliance = compliance;
        this.deployment = deployment;
    }

    /** Exact weighted score, for example 34.25. */
    public double getExactScore() {
        return security.getSecurityScore() * SECURITY_WEIGHT
                + qualityGate.getQualityScore() * QUALITY_WEIGHT
                + compliance.getComplianceScore() * COMPLIANCE_WEIGHT
                + deployment.getReadinessScore() * DEPLOYMENT_WEIGHT;
    }

    /** Overall score rounded to a whole number from 0 to 100. */
    public int getOverallScore() {
        return (int) Math.round(getExactScore());
    }

    public HealthStatus getStatus() {
        int score = getOverallScore();
        if (score >= 85) {
            return HealthStatus.EXCELLENT;
        }
        if (score >= 70) {
            return HealthStatus.GOOD;
        }
        if (score >= 50) {
            return HealthStatus.MODERATE;
        }
        return HealthStatus.CRITICAL;
    }

    /** Points lost because of one component: (100 - score) * weight. */
    public static double lostPoints(int score, double weight) {
        return (100 - score) * weight;
    }

    /** The reasons for lost points, biggest loss first. Components that lost nothing are not listed. */
    public List<String> getLossReasons() {
        String[] names = new String[4];
        double[] lost = new double[4];
        String[] details = new String[4];

        names[0] = "Security";
        lost[0] = lostPoints(security.getSecurityScore(), SECURITY_WEIGHT);
        details[0] = "score " + security.getSecurityScore() + "/100, "
                + security.getCriticalCount() + " critical issue(s)";

        names[1] = "Quality";
        lost[1] = lostPoints(qualityGate.getQualityScore(), QUALITY_WEIGHT);
        details[1] = "score " + qualityGate.getQualityScore() + "/100, failed: " + failedNames(qualityGate.getResults());

        names[2] = "Compliance";
        lost[2] = lostPoints(compliance.getComplianceScore(), COMPLIANCE_WEIGHT);
        details[2] = "score " + compliance.getComplianceScore() + "/100, "
                + compliance.getViolationCount() + " violation(s)";

        names[3] = "Deployment";
        lost[3] = lostPoints(deployment.getReadinessScore(), DEPLOYMENT_WEIGHT);
        details[3] = "score " + deployment.getReadinessScore() + "/100, decision " + deployment.getDecision();

        // Simple bubble sort: biggest loss first.
        for (int i = 0; i < names.length - 1; i++) {
            for (int j = 0; j < names.length - 1 - i; j++) {
                if (lost[j] < lost[j + 1]) {
                    double tempLost = lost[j];
                    lost[j] = lost[j + 1];
                    lost[j + 1] = tempLost;
                    String tempName = names[j];
                    names[j] = names[j + 1];
                    names[j + 1] = tempName;
                    String tempDetails = details[j];
                    details[j] = details[j + 1];
                    details[j + 1] = tempDetails;
                }
            }
        }

        List<String> reasons = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (lost[i] > 0) {
                reasons.add(names[i] + ": -" + ReportFormat.number(lost[i]) + " points (" + details[i] + ")");
            }
        }
        return reasons;
    }

    private String failedNames(List<CheckResult> results) {
        StringBuilder sb = new StringBuilder();
        for (CheckResult result : results) {
            if (!result.isPassed()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(result.getName());
            }
        }
        if (sb.length() == 0) {
            return "none";
        }
        return sb.toString();
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("INFRASTRUCTURE HEALTH SCORE"));
        sb.append("Security   ").append(security.getSecurityScore()).append(" x 35%\n");
        sb.append("Quality    ").append(qualityGate.getQualityScore()).append(" x 25%\n");
        sb.append("Compliance ").append(compliance.getComplianceScore()).append(" x 20%\n");
        sb.append("Deployment ").append(deployment.getReadinessScore()).append(" x 20%\n");
        sb.append("-".repeat(66)).append("\n");
        sb.append("Overall score : ").append(getOverallScore()).append(" / 100 (exact ")
                .append(ReportFormat.number(getExactScore())).append(")\n");
        sb.append("Health status : ").append(getStatus()).append("\n");
        List<String> reasons = getLossReasons();
        if (reasons.isEmpty()) {
            sb.append("No points were lost.\n");
        } else {
            sb.append("Main reasons for lost points:\n");
            for (String reason : reasons) {
                sb.append("  - ").append(reason).append("\n");
            }
        }
        return sb.toString();
    }
}
