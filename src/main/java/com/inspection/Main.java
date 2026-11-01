package com.inspection;

import com.inspection.demo.FintechProjectFactory;
import com.inspection.element.Directory;
import com.inspection.visitor.ComplianceVisitor;
import com.inspection.visitor.DeploymentReadinessVisitor;
import com.inspection.visitor.DocumentationVisitor;
import com.inspection.visitor.ProjectStatisticsVisitor;
import com.inspection.visitor.QualityGateVisitor;
import com.inspection.visitor.SecurityAuditVisitor;
import com.inspection.visitor.TechnicalDebtVisitor;

/**
 * Runs all seven visitors on the same demo project tree and prints seven reports.
 * The project data is simulated; nothing real is scanned.
 */
public class Main {

    public static void main(String[] args) {
        Directory project = FintechProjectFactory.createProject();

        System.out.println("Inspecting project: " + project.getName());
        System.out.println();

        // The quality gate needs the numbers of these two visitors, so they run first.
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        project.accept(security);

        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        project.accept(statistics);

        QualityGateVisitor qualityGate = new QualityGateVisitor(statistics, security);
        project.accept(qualityGate);

        ComplianceVisitor compliance = new ComplianceVisitor();
        project.accept(compliance);

        TechnicalDebtVisitor technicalDebt = new TechnicalDebtVisitor();
        project.accept(technicalDebt);

        DeploymentReadinessVisitor deployment = new DeploymentReadinessVisitor();
        project.accept(deployment);

        DocumentationVisitor documentation = new DocumentationVisitor();
        project.accept(documentation);

        System.out.println(security.getReport());
        System.out.println(statistics.getReport());
        System.out.println(qualityGate.getReport());
        System.out.println(compliance.getReport());
        System.out.println(technicalDebt.getReport());
        System.out.println(deployment.getReport());
        System.out.println(documentation.getReport());
    }
}
