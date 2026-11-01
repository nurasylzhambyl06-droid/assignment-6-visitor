package com.inspection;

import com.inspection.element.Directory;
import com.inspection.health.HealthStatus;
import com.inspection.health.InfrastructureHealthScore;
import com.inspection.visitor.ComplianceVisitor;
import com.inspection.visitor.DeploymentReadinessVisitor;
import com.inspection.visitor.ProjectStatisticsVisitor;
import com.inspection.visitor.QualityGateVisitor;
import com.inspection.visitor.SecurityAuditVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfrastructureHealthScoreTest {

    private InfrastructureHealthScore scoreFor(Directory root) {
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        root.accept(security);
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        root.accept(statistics);
        QualityGateVisitor gate = new QualityGateVisitor(statistics, security);
        root.accept(gate);
        ComplianceVisitor compliance = new ComplianceVisitor();
        root.accept(compliance);
        DeploymentReadinessVisitor deployment = new DeploymentReadinessVisitor();
        root.accept(deployment);
        return new InfrastructureHealthScore(security, gate, compliance, deployment);
    }

    @Test
    void weightsAddUpToOneHundredPercent() {
        double sum = InfrastructureHealthScore.SECURITY_WEIGHT + InfrastructureHealthScore.QUALITY_WEIGHT
                + InfrastructureHealthScore.COMPLIANCE_WEIGHT + InfrastructureHealthScore.DEPLOYMENT_WEIGHT;
        assertEquals(1.0, sum, 0.0001);
    }

    @Test
    void secureProjectIsExcellent() {
        InfrastructureHealthScore score = scoreFor(TestProjects.secureProject());

        assertEquals(100, score.getOverallScore());
        assertEquals(HealthStatus.EXCELLENT, score.getStatus());
        assertTrue(score.getLossReasons().isEmpty());
    }

    @Test
    void demoProjectIsCritical() {
        InfrastructureHealthScore score = scoreFor(TestProjects.demoProject());

        // 26 * 0.35 + 67 * 0.25 + 13 * 0.20 + 29 * 0.20 = 9.1 + 16.75 + 2.6 + 5.8
        assertEquals(34.25, score.getExactScore(), 0.001);
        assertEquals(34, score.getOverallScore());
        assertEquals(HealthStatus.CRITICAL, score.getStatus());
    }

    @Test
    void lossReasonsAreSortedByBiggestLoss() {
        List<String> reasons = scoreFor(TestProjects.demoProject()).getLossReasons();

        assertEquals(4, reasons.size());
        assertTrue(reasons.get(0).startsWith("Security: -25.90"), reasons.get(0));
        assertTrue(reasons.get(1).startsWith("Compliance: -17.40"), reasons.get(1));
        assertTrue(reasons.get(2).startsWith("Deployment: -14.20"), reasons.get(2));
        assertTrue(reasons.get(3).startsWith("Quality: -8.25"), reasons.get(3));
    }

    @Test
    void lostPointsUseScoreAndWeight() {
        assertEquals(25.9, InfrastructureHealthScore.lostPoints(26, 0.35), 0.0001);
        assertEquals(0.0, InfrastructureHealthScore.lostPoints(100, 0.35), 0.0001);
    }

    @Test
    void reportShowsScoreAndStatus() {
        String report = scoreFor(TestProjects.demoProject()).getReport();

        assertTrue(report.contains("Overall score : 34 / 100"));
        assertTrue(report.contains("Health status : CRITICAL"));
    }
}
