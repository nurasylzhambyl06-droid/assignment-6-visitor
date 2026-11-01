package com.inspection;

import com.inspection.element.Directory;
import com.inspection.visitor.ComplianceVisitor;
import com.inspection.visitor.DeploymentReadinessVisitor;
import com.inspection.visitor.DocumentationVisitor;
import com.inspection.visitor.ProjectStatisticsVisitor;
import com.inspection.visitor.QualityGateVisitor;
import com.inspection.visitor.SecurityAuditVisitor;
import com.inspection.visitor.TechnicalDebtVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs all seven visitors on exactly the same tree object, like Main does. */
class DemoProjectTest {

    @Test
    void allSevenVisitorsInspectTheSameTreeAndTheTreeStaysUnchanged() {
        Directory project = TestProjects.demoProject();

        RecordingVisitor before = new RecordingVisitor();
        project.accept(before);

        SecurityAuditVisitor security = new SecurityAuditVisitor();
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(security);
        project.accept(statistics);
        QualityGateVisitor gate = new QualityGateVisitor(statistics, security);
        ComplianceVisitor compliance = new ComplianceVisitor();
        TechnicalDebtVisitor debt = new TechnicalDebtVisitor();
        DeploymentReadinessVisitor deployment = new DeploymentReadinessVisitor();
        DocumentationVisitor documentation = new DocumentationVisitor();
        project.accept(gate);
        project.accept(compliance);
        project.accept(debt);
        project.accept(deployment);
        project.accept(documentation);

        RecordingVisitor after = new RecordingVisitor();
        project.accept(after);

        assertEquals(before.getCalls(), after.getCalls(), "visitors must not change the tree");
        assertEquals(26, security.getSecurityScore());
        assertEquals(11, statistics.getTotalFiles());
        assertEquals(2, gate.getPassedCount());
        assertEquals(8, compliance.getViolationCount());
        assertEquals(20, debt.getDebtPoints());
        assertEquals(29, deployment.getReadinessScore());
        assertEquals(54, documentation.getDocumentationScore());
    }

    @Test
    void rootDirectoryIsCalledSmartFintechPlatform() {
        Directory project = TestProjects.demoProject();

        assertEquals("smart-fintech-platform", project.getName());
        assertEquals(7, project.getElements().size());
    }

    @Test
    void allReportsContainTheirTitles() {
        Directory project = TestProjects.demoProject();
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        project.accept(security);
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(statistics);
        ComplianceVisitor compliance = new ComplianceVisitor();
        project.accept(compliance);

        assertTrue(security.getReport().contains("SECURITY AUDIT REPORT"));
        assertTrue(statistics.getReport().contains("PROJECT STATISTICS REPORT"));
        assertTrue(compliance.getReport().contains("COMPLIANCE REPORT"));
    }
}
