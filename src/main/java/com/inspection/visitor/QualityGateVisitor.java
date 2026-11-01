package com.inspection.visitor;

import com.inspection.element.ConfigurationFile;
import com.inspection.element.DependencyFile;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import com.inspection.report.CheckResult;
import com.inspection.report.ReportFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether the project passes the quality gate.
 *
 * DESIGN NOTE
 * This visitor does not measure anything itself. It receives the two visitors
 * that already measured the project (statistics and security) in its constructor
 * and only compares their numbers with the limits. So nothing is calculated twice.
 * Because it is still a ProjectVisitor, it must implement all visit methods;
 * they are empty on purpose, since the numbers come from the two other visitors.
 * Run ProjectStatisticsVisitor and SecurityAuditVisitor on the tree BEFORE
 * asking this class for results.
 *
 * GATE RULES
 *   1. Average complexity must be <= 10
 *   2. Test/source ratio must be >= 0.40
 *   3. Critical security issues must be 0
 */
public class QualityGateVisitor implements ProjectVisitor {

    public static final double MAX_AVERAGE_COMPLEXITY = 10.0;
    public static final double MIN_TEST_SOURCE_RATIO = 0.40;

    private final ProjectStatisticsVisitor statistics;
    private final SecurityAuditVisitor security;

    public QualityGateVisitor(ProjectStatisticsVisitor statistics, SecurityAuditVisitor security) {
        this.statistics = statistics;
        this.security = security;
    }

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        // Metrics come from the constructor visitors.
    }

    @Override
    public void visit(TestFile testFile) {
        // Metrics come from the constructor visitors.
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        // Metrics come from the constructor visitors.
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        // Metrics come from the constructor visitors.
    }

    @Override
    public void visit(Directory directory) {
        // Metrics come from the constructor visitors.
    }

    public List<CheckResult> getResults() {
        List<CheckResult> results = new ArrayList<>();

        double complexity = statistics.getAverageComplexity();
        results.add(new CheckResult("Average complexity <= " + ReportFormat.number(MAX_AVERAGE_COMPLEXITY),
                complexity <= MAX_AVERAGE_COMPLEXITY,
                "actual " + ReportFormat.number(complexity)));

        double ratio = statistics.getTestSourceRatio();
        results.add(new CheckResult("Test/source ratio >= " + ReportFormat.number(MIN_TEST_SOURCE_RATIO),
                ratio >= MIN_TEST_SOURCE_RATIO,
                "actual " + ReportFormat.number(ratio)));

        int critical = security.getCriticalCount();
        results.add(new CheckResult("Critical security issues = 0",
                critical == 0,
                "actual " + critical));

        return results;
    }

    public int getPassedCount() {
        int passed = 0;
        for (CheckResult result : getResults()) {
            if (result.isPassed()) {
                passed++;
            }
        }
        return passed;
    }

    public boolean isPassed() {
        return getPassedCount() == getResults().size();
    }

    /** Quality score 0-100: the share of gate rules that passed. */
    public int getQualityScore() {
        List<CheckResult> results = getResults();
        return (int) Math.round(getPassedCount() * 100.0 / results.size());
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("3. QUALITY GATE REPORT"));
        for (CheckResult result : getResults()) {
            sb.append(result).append("\n");
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append("Quality score : ").append(getQualityScore()).append(" / 100\n");
        sb.append("FINAL RESULT  : ").append(isPassed() ? "PASS" : "FAIL").append("\n");
        return sb.toString();
    }
}
