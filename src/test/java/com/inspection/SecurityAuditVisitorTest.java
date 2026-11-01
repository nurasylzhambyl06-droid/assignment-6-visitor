package com.inspection;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.report.SecurityFinding;
import com.inspection.report.Severity;
import com.inspection.visitor.SecurityAuditVisitor;
import org.junit.jupiter.api.Test;

import static com.inspection.TestProjects.dirWith;
import static com.inspection.TestProjects.source;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityAuditVisitorTest {

    private SecurityAuditVisitor audit(Directory root) {
        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        root.accept(visitor);
        return visitor;
    }

    @Test
    void hardcodedCredentialsInSourceAreCritical() {
        SecurityAuditVisitor visitor = audit(dirWith(source("Pay.java", 100, 5, true, true)));

        assertEquals(1, visitor.getFindings().size());
        SecurityFinding finding = visitor.getFindings().get(0);
        assertEquals(Severity.CRITICAL, finding.getSeverity());
        assertEquals("Pay.java", finding.getLocation());
        assertEquals(1, visitor.getCriticalCount());
        assertEquals(85, visitor.getSecurityScore());
    }

    @Test
    void exposedCredentialsAndInsecureConfigurationAreDetected() {
        SecurityAuditVisitor visitor = audit(dirWith(new ConfigurationFile("app.properties", true, true)));

        assertEquals(1, visitor.getFindingCount(Severity.CRITICAL));
        assertEquals(1, visitor.getFindingCount(Severity.MEDIUM));
        assertEquals(81, visitor.getSecurityScore()); // 100 - 15 - 4
    }

    @Test
    void vulnerableDependencyIsHigh() {
        SecurityAuditVisitor visitor = audit(dirWith(new DependencyFile("log4j-core", "2.14.0", true, false)));

        assertEquals(1, visitor.getFindingCount(Severity.HIGH));
        assertEquals(92, visitor.getSecurityScore());
    }

    @Test
    void highRiskSourceFileStartsAboveComplexityTwenty() {
        SecurityAuditVisitor risky = audit(dirWith(source("Risky.java", 100, 21, false, true)));
        SecurityAuditVisitor boundary = audit(dirWith(source("Border.java", 100, 20, false, true)));

        assertEquals(1, risky.getFindingCount(Severity.HIGH));
        assertTrue(risky.getFindings().get(0).getMessage().contains("High-risk"));
        assertTrue(boundary.getFindings().isEmpty(), "complexity 20 is not above the limit");
    }

    @Test
    void databaseProblemsAreDetected() {
        SecurityAuditVisitor visitor = audit(dirWith(new DatabaseElement("db", false, true, true)));

        assertEquals(1, visitor.getFindingCount(Severity.CRITICAL));
        assertEquals(1, visitor.getFindingCount(Severity.HIGH));
        assertEquals(77, visitor.getSecurityScore()); // 100 - 15 - 8
    }

    @Test
    void apiEndpointWithoutAuthenticationAndRateLimitingIsDetected() {
        SecurityAuditVisitor visitor = audit(dirWith(new APIEndpointElement("/open", "GET", false, false)));

        assertEquals(1, visitor.getFindingCount(Severity.HIGH));
        assertEquals(1, visitor.getFindingCount(Severity.MEDIUM));
        assertEquals(88, visitor.getSecurityScore()); // 100 - 8 - 4
    }

    @Test
    void insecureProductionDeploymentIsDetected() {
        SecurityAuditVisitor visitor = audit(dirWith(new DeploymentConfigElement("production", true, false, true)));

        assertEquals(1, visitor.getFindingCount(Severity.CRITICAL)); // no HTTPS
        assertEquals(1, visitor.getFindingCount(Severity.HIGH));     // debug mode
        assertEquals(1, visitor.getFindingCount(Severity.MEDIUM));   // auto deployment
        assertEquals(73, visitor.getSecurityScore());                // 100 - 15 - 8 - 4
    }

    @Test
    void insecureDevelopmentDeploymentIsIgnored() {
        SecurityAuditVisitor visitor = audit(dirWith(new DeploymentConfigElement("development", true, false, true)));

        assertTrue(visitor.getFindings().isEmpty());
        assertEquals(100, visitor.getSecurityScore());
    }

    @Test
    void secureProjectHasNoFindingsAndFullScore() {
        SecurityAuditVisitor visitor = audit(TestProjects.secureProject());

        assertTrue(visitor.getFindings().isEmpty());
        assertEquals(0, visitor.getCriticalCount());
        assertEquals(100, visitor.getSecurityScore());
    }

    @Test
    void securityScoreNeverGoesBelowZero() {
        Directory root = new Directory("root");
        for (int i = 0; i < 10; i++) {
            root.add(source("Bad" + i + ".java", 10, 1, true, true));
        }
        SecurityAuditVisitor visitor = audit(root);

        assertEquals(10, visitor.getCriticalCount());
        assertEquals(0, visitor.getSecurityScore());
    }

    @Test
    void demoProjectFindingsAndScore() {
        SecurityAuditVisitor visitor = audit(TestProjects.demoProject());

        assertEquals(9, visitor.getFindings().size());
        assertEquals(2, visitor.getCriticalCount());
        assertEquals(4, visitor.getFindingCount(Severity.HIGH));
        assertEquals(3, visitor.getFindingCount(Severity.MEDIUM));
        assertEquals(26, visitor.getSecurityScore()); // 100 - 2*15 - 4*8 - 3*4
    }
}
