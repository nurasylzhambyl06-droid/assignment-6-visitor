package com.inspection;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.report.CheckResult;
import com.inspection.visitor.ComplianceVisitor;
import org.junit.jupiter.api.Test;

import static com.inspection.TestProjects.cleanSource;
import static com.inspection.TestProjects.dirWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplianceVisitorTest {

    private ComplianceVisitor check(Directory root) {
        ComplianceVisitor visitor = new ComplianceVisitor();
        root.accept(visitor);
        return visitor;
    }

    private CheckResult resultFor(ComplianceVisitor visitor, String rule) {
        for (CheckResult result : visitor.getResults()) {
            if (result.getName().equals(rule)) {
                return result;
            }
        }
        throw new AssertionError("rule not found: " + rule);
    }

    @Test
    void secureProjectHasNoViolations() {
        ComplianceVisitor visitor = check(TestProjects.secureProject());

        assertEquals(0, visitor.getViolationCount());
        assertEquals(8, visitor.getPassedCount());
        assertEquals(100, visitor.getComplianceScore());
    }

    @Test
    void demoProjectViolatesSevenOfEightRules() {
        ComplianceVisitor visitor = check(TestProjects.demoProject());

        assertEquals(8, visitor.getViolationCount());
        assertEquals(1, visitor.getPassedCount());
        assertTrue(resultFor(visitor, ComplianceVisitor.RULE_HTTPS).isPassed());
        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_CREDENTIALS).isPassed());
        assertEquals(13, visitor.getComplianceScore()); // 1 of 8 rules = 12.5, rounded
    }

    @Test
    void sourceFileWithoutMatchingTestFileIsAViolation() {
        ComplianceVisitor visitor = check(dirWith(cleanSource("UserService.java")));

        CheckResult result = resultFor(visitor, ComplianceVisitor.RULE_TESTS);
        assertFalse(result.isPassed());
        assertTrue(result.getDetails().contains("UserServiceTest.java"));
        assertEquals(1, visitor.getViolationCount());
    }

    @Test
    void testFileNameConventionAddsTestBeforeDotJava() {
        assertEquals("PaymentServiceTest.java", ComplianceVisitor.expectedTestFileName("PaymentService.java"));
    }

    @Test
    void projectWithoutSourceFilesHasNoMissingTestViolation() {
        ComplianceVisitor visitor = check(new Directory("empty"));

        assertEquals(0, visitor.getViolationCount());
        assertEquals(100, visitor.getComplianceScore());
    }

    @Test
    void dependencyWithoutVersionIsAViolation() {
        ComplianceVisitor visitor = check(dirWith(
                new DependencyFile("no-version", null, false, false),
                new DependencyFile("blank-version", "   ", false, false),
                new DependencyFile("fine", "1.0", false, false)));

        CheckResult result = resultFor(visitor, ComplianceVisitor.RULE_VERSIONS);
        assertFalse(result.isPassed());
        assertTrue(result.getDetails().startsWith("2 violation(s)"));
    }

    @Test
    void prohibitedDependencyIsAViolation() {
        ComplianceVisitor visitor = check(dirWith(new DependencyFile("old-crypto-lib", "0.9", false, true)));

        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_PROHIBITED).isPassed());
        assertEquals(1, visitor.getViolationCount());
    }

    @Test
    void infrastructureRulesAreChecked() {
        ComplianceVisitor visitor = check(dirWith(
                new DatabaseElement("db", false, true, true),                    // 2 violations
                new DeploymentConfigElement("production", true, false, false),   // 2 violations
                new APIEndpointElement("/open", "GET", false, true)));           // 1 violation

        assertEquals(5, visitor.getViolationCount());
        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_CREDENTIALS).isPassed());
        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_DB_ENCRYPTION).isPassed());
        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_HTTPS).isPassed());
        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_DEBUG).isPassed());
        assertFalse(resultFor(visitor, ComplianceVisitor.RULE_API_AUTH).isPassed());
        assertEquals(3, visitor.getPassedCount());
    }

    @Test
    void developmentDeploymentIsNotChecked() {
        ComplianceVisitor visitor = check(dirWith(new DeploymentConfigElement("development", true, false, true)));

        assertEquals(0, visitor.getViolationCount());
    }
}
