package com.inspection;

import com.inspection.element.Directory;
import com.inspection.element.TestFile;
import com.inspection.report.CheckResult;
import com.inspection.visitor.ProjectStatisticsVisitor;
import com.inspection.visitor.QualityGateVisitor;
import com.inspection.visitor.SecurityAuditVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.inspection.TestProjects.dirWith;
import static com.inspection.TestProjects.source;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityGateVisitorTest {

    /** Runs the two measuring visitors first, then the gate, all on the same tree. */
    private QualityGateVisitor gateFor(Directory root) {
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        root.accept(statistics);
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        root.accept(security);
        QualityGateVisitor gate = new QualityGateVisitor(statistics, security);
        root.accept(gate);
        return gate;
    }

    @Test
    void secureProjectPassesTheGate() {
        QualityGateVisitor gate = gateFor(TestProjects.secureProject());

        assertTrue(gate.isPassed());
        assertEquals(3, gate.getPassedCount());
        assertEquals(100, gate.getQualityScore());
        for (CheckResult result : gate.getResults()) {
            assertTrue(result.isPassed(), result.toString());
        }
    }

    @Test
    void highAverageComplexityFailsTheGate() {
        QualityGateVisitor gate = gateFor(dirWith(
                source("Complex.java", 100, 15, false, true),
                new TestFile("ComplexTest.java", 2)));

        List<CheckResult> results = gate.getResults();
        assertFalse(gate.isPassed());
        assertFalse(results.get(0).isPassed(), "complexity rule");
        assertTrue(results.get(1).isPassed(), "ratio rule");
        assertTrue(results.get(2).isPassed(), "critical rule");
        assertEquals(67, gate.getQualityScore());
    }

    @Test
    void lowTestSourceRatioFailsTheGate() {
        QualityGateVisitor gate = gateFor(dirWith(
                source("A.java", 10, 2, false, true),
                source("B.java", 10, 2, false, true),
                source("C.java", 10, 2, false, true),
                new TestFile("ATest.java", 1)));

        List<CheckResult> results = gate.getResults();
        assertFalse(gate.isPassed());
        assertTrue(results.get(0).isPassed());
        assertFalse(results.get(1).isPassed(), "1 test file for 3 source files is 0.33");
        assertTrue(results.get(2).isPassed());
    }

    @Test
    void criticalSecurityIssueFailsTheGate() {
        QualityGateVisitor gate = gateFor(dirWith(
                source("Leaky.java", 10, 2, true, true),
                new TestFile("LeakyTest.java", 1)));

        List<CheckResult> results = gate.getResults();
        assertFalse(gate.isPassed());
        assertFalse(results.get(2).isPassed());
        assertEquals("actual 1", results.get(2).getDetails());
    }

    @Test
    void limitsAreInclusive() {
        // average complexity exactly 10 and ratio exactly 0.40 (2 tests for 5 sources)
        Directory root = new Directory("root");
        for (int i = 0; i < 5; i++) {
            root.add(source("S" + i + ".java", 10, 10, false, true));
        }
        root.add(new TestFile("S0Test.java", 1));
        root.add(new TestFile("S1Test.java", 1));

        QualityGateVisitor gate = gateFor(root);

        assertTrue(gate.isPassed(), gate.getReport());
    }

    @Test
    void projectWithoutSourceFilesFailsOnlyTheRatioRule() {
        QualityGateVisitor gate = gateFor(new Directory("empty"));

        List<CheckResult> results = gate.getResults();
        assertTrue(results.get(0).isPassed());
        assertFalse(results.get(1).isPassed(), "ratio is 0.00 when there is no source code");
        assertTrue(results.get(2).isPassed());
        assertFalse(gate.isPassed());
    }

    @Test
    void demoProjectFailsBecauseOfCriticalIssues() {
        QualityGateVisitor gate = gateFor(TestProjects.demoProject());

        assertFalse(gate.isPassed());
        assertEquals(2, gate.getPassedCount());
        assertTrue(gate.getReport().contains("FINAL RESULT  : FAIL"));
    }
}
