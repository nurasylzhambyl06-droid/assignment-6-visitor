package com.inspection;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.report.DeploymentDecision;
import com.inspection.visitor.DeploymentReadinessVisitor;
import org.junit.jupiter.api.Test;

import static com.inspection.TestProjects.dirWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentReadinessVisitorTest {

    private DeploymentReadinessVisitor evaluate(Directory root) {
        DeploymentReadinessVisitor visitor = new DeploymentReadinessVisitor();
        root.accept(visitor);
        return visitor;
    }

    private DeploymentConfigElement safeProduction() {
        return new DeploymentConfigElement("production", false, true, false);
    }

    @Test
    void secureProjectIsReady() {
        DeploymentReadinessVisitor visitor = evaluate(TestProjects.secureProject());

        assertEquals(7, visitor.getPassedCount());
        assertEquals(100, visitor.getReadinessScore());
        assertEquals(DeploymentDecision.READY, visitor.getDecision());
    }

    @Test
    void demoProjectIsBlocked() {
        DeploymentReadinessVisitor visitor = evaluate(TestProjects.demoProject());

        assertEquals(2, visitor.getPassedCount());
        assertEquals(29, visitor.getReadinessScore()); // 2 of 7 = 28.57
        assertEquals(DeploymentDecision.BLOCKED, visitor.getDecision());
    }

    @Test
    void missingBackupOnlyGivesAWarning() {
        DeploymentReadinessVisitor visitor = evaluate(dirWith(
                safeProduction(),
                new DatabaseElement("db", true, false, false)));

        assertEquals(6, visitor.getPassedCount());
        assertEquals(86, visitor.getReadinessScore()); // 6 of 7 = 85.71
        assertEquals(DeploymentDecision.WARNING, visitor.getDecision());
    }

    @Test
    void missingRateLimitingOnlyGivesAWarning() {
        DeploymentReadinessVisitor visitor = evaluate(dirWith(
                safeProduction(),
                new APIEndpointElement("/api", "GET", true, false)));

        assertEquals(DeploymentDecision.WARNING, visitor.getDecision());
    }

    @Test
    void missingAuthenticationBlocksDeployment() {
        DeploymentReadinessVisitor visitor = evaluate(dirWith(
                safeProduction(),
                new APIEndpointElement("/api", "GET", false, true)));

        assertEquals(DeploymentDecision.BLOCKED, visitor.getDecision());
    }

    @Test
    void noProductionConfigurationBlocksDeployment() {
        DeploymentReadinessVisitor visitor = evaluate(dirWith(
                new DeploymentConfigElement("development", false, true, false)));

        assertFalse(visitor.getResults().get(0).isPassed(), "production config must exist");
        assertFalse(visitor.getResults().get(1).isPassed(), "HTTPS cannot be confirmed");
        assertFalse(visitor.getResults().get(2).isPassed(), "debug mode cannot be confirmed");
        assertEquals(4, visitor.getPassedCount());
        assertEquals(DeploymentDecision.BLOCKED, visitor.getDecision());
    }

    @Test
    void debugModeInProductionBlocksDeployment() {
        DeploymentReadinessVisitor visitor = evaluate(dirWith(
                new DeploymentConfigElement("production", true, true, false)));

        assertFalse(visitor.getResults().get(2).isPassed());
        assertEquals(DeploymentDecision.BLOCKED, visitor.getDecision());
    }

    @Test
    void developmentSettingsDoNotAffectReadiness() {
        DeploymentReadinessVisitor visitor = evaluate(dirWith(
                safeProduction(),
                new DeploymentConfigElement("development", true, false, true)));

        assertTrue(visitor.getResults().get(1).isPassed());
        assertEquals(DeploymentDecision.READY, visitor.getDecision());
    }
}
