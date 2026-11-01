package com.inspection;

import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.Directory;
import com.inspection.element.TestFile;
import com.inspection.report.DebtStatus;
import com.inspection.visitor.TechnicalDebtVisitor;
import org.junit.jupiter.api.Test;

import static com.inspection.TestProjects.dirWith;
import static com.inspection.TestProjects.source;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnicalDebtVisitorTest {

    private TechnicalDebtVisitor estimate(Directory root) {
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        root.accept(visitor);
        return visitor;
    }

    @Test
    void secureProjectHasNoDebt() {
        TechnicalDebtVisitor visitor = estimate(TestProjects.secureProject());

        assertEquals(0, visitor.getDebtPoints());
        assertEquals(DebtStatus.LOW, visitor.getStatus());
        assertTrue(visitor.getFindings().isEmpty());
    }

    @Test
    void demoProjectDebtIsModerate() {
        TechnicalDebtVisitor visitor = estimate(TestProjects.demoProject());

        // complexity 5 + credentials 5 + undocumented 2 + insecure config 2 + vulnerable dependency 3 + no backup 3
        assertEquals(20, visitor.getDebtPoints());
        assertEquals(DebtStatus.MODERATE, visitor.getStatus());
        assertEquals(6, visitor.getFindings().size());
    }

    @Test
    void badSourceFileWithoutTestsAddsUpPoints() {
        // very high complexity 10 + large file 5 + undocumented 2 + no tests at all 20
        TechnicalDebtVisitor visitor = estimate(dirWith(source("Big.java", 500, 25, false, false)));

        assertEquals(37, visitor.getDebtPoints());
        assertEquals(DebtStatus.MODERATE, visitor.getStatus());
        assertTrue(visitor.getFindings().get(visitor.getFindings().size() - 1).contains("insufficient testing"));
    }

    @Test
    void manyVulnerableDependenciesPushDebtToHigh() {
        TechnicalDebtVisitor visitor = estimate(dirWith(
                source("Big.java", 500, 25, false, false),
                new DependencyFile("a", "1", true, false),
                new DependencyFile("b", "1", true, false)));

        assertEquals(43, visitor.getDebtPoints()); // 37 + 2 * 3
        assertEquals(DebtStatus.HIGH, visitor.getStatus());
    }

    @Test
    void lowTestRatioAddsTenPoints() {
        TechnicalDebtVisitor visitor = estimate(dirWith(
                source("A.java", 10, 1, false, true),
                source("B.java", 10, 1, false, true),
                source("C.java", 10, 1, false, true),
                new TestFile("ATest.java", 1)));

        assertEquals(10, visitor.getDebtPoints());
        assertEquals(DebtStatus.LOW, visitor.getStatus());
    }

    @Test
    void statusBoundariesAreFifteenAndFourty() {
        Directory fifteen = new Directory("fifteen");
        for (int i = 0; i < 5; i++) {
            fifteen.add(new DependencyFile("lib" + i, "1", true, false)); // 5 * 3 = 15
        }
        Directory eighteen = new Directory("eighteen");
        for (int i = 0; i < 6; i++) {
            eighteen.add(new DependencyFile("lib" + i, "1", true, false)); // 6 * 3 = 18
        }

        assertEquals(DebtStatus.LOW, estimate(fifteen).getStatus());
        assertEquals(DebtStatus.MODERATE, estimate(eighteen).getStatus());
    }

    @Test
    void projectWithoutSourceFilesHasNoTestingDebt() {
        TechnicalDebtVisitor visitor = estimate(dirWith(new DatabaseElement("db", true, false, false)));

        assertEquals(3, visitor.getDebtPoints(), "only the missing backup counts");
    }
}
