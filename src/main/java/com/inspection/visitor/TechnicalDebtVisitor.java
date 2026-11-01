package com.inspection.visitor;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import com.inspection.report.DebtStatus;
import com.inspection.report.ReportFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Estimates technical debt with simple "debt points". More points = more debt.
 *
 * RULES (points)
 *   Source file complexity above 10 ........ +5   (above 20: +10 instead)
 *   Source file with more than 400 lines ... +5   (large file)
 *   Source file with hardcoded credentials . +5
 *   Source file without documentation ...... +2
 *   Vulnerable dependency .................. +3 each
 *   Insecure configuration file ............ +2
 *   Database without backups ............... +3
 *   Insufficient testing (project level) ... +20 if there are source files but no test files,
 *                                            +10 if the test/source ratio is below 0.40
 *
 * STATUS
 *   0 - 15 points  = LOW
 *   16 - 40 points = MODERATE
 *   41+ points     = HIGH
 */
public class TechnicalDebtVisitor implements ProjectVisitor {

    public static final int COMPLEXITY_LIMIT = 10;
    public static final int VERY_HIGH_COMPLEXITY_LIMIT = 20;
    public static final int LARGE_FILE_LINES = 400;
    public static final int LOW_LIMIT = 15;
    public static final int MODERATE_LIMIT = 40;

    private final List<String> findings = new ArrayList<>();
    private int elementPoints;
    private int sourceFileCount;
    private int testFileCount;

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        sourceFileCount++;
        String name = sourceCodeFile.getFileName();
        int complexity = sourceCodeFile.getComplexity();

        if (complexity > VERY_HIGH_COMPLEXITY_LIMIT) {
            addDebt(10, name + ": very high complexity (" + complexity + ")");
        } else if (complexity > COMPLEXITY_LIMIT) {
            addDebt(5, name + ": high complexity (" + complexity + ")");
        }
        if (sourceCodeFile.getLinesOfCode() > LARGE_FILE_LINES) {
            addDebt(5, name + ": large file (" + sourceCodeFile.getLinesOfCode() + " lines)");
        }
        if (sourceCodeFile.hasHardcodedCredentials()) {
            addDebt(5, name + ": hardcoded credentials");
        }
        if (!sourceCodeFile.isDocumented()) {
            addDebt(2, name + ": no documentation");
        }
    }

    @Override
    public void visit(TestFile testFile) {
        testFileCount++;
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        if (configurationFile.isInsecureConfiguration()) {
            addDebt(2, configurationFile.getFileName() + ": insecure configuration");
        }
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        if (dependencyFile.isVulnerable()) {
            addDebt(3, dependencyFile.getDependencyName() + ": vulnerable dependency");
        }
    }

    @Override
    public void visit(Directory directory) {
        // Directories do not add technical debt.
    }

    @Override
    public void visit(DatabaseElement databaseElement) {
        if (!databaseElement.isBackupEnabled()) {
            addDebt(3, databaseElement.getDatabaseName() + ": backups are not enabled");
        }
    }

    @Override
    public void visit(APIEndpointElement apiEndpointElement) {
        // API endpoints do not add technical debt in this version.
    }

    @Override
    public void visit(DeploymentConfigElement deploymentConfigElement) {
        // Deployment settings do not add technical debt in this version.
    }

    private void addDebt(int points, String message) {
        elementPoints += points;
        findings.add("+" + points + "  " + message);
    }

    /** Points for insufficient testing. They need the final counts, so they are calculated on demand. */
    private int getTestingPoints() {
        if (sourceFileCount == 0) {
            return 0;
        }
        if (testFileCount == 0) {
            return 20;
        }
        double ratio = (double) testFileCount / sourceFileCount;
        if (ratio < QualityGateVisitor.MIN_TEST_SOURCE_RATIO) {
            return 10;
        }
        return 0;
    }

    public int getDebtPoints() {
        return elementPoints + getTestingPoints();
    }

    public List<String> getFindings() {
        List<String> all = new ArrayList<>(findings);
        int testingPoints = getTestingPoints();
        if (testingPoints > 0) {
            all.add("+" + testingPoints + "  project: insufficient testing ("
                    + testFileCount + " test file(s) for " + sourceFileCount + " source file(s))");
        }
        return all;
    }

    public DebtStatus getStatus() {
        int points = getDebtPoints();
        if (points <= LOW_LIMIT) {
            return DebtStatus.LOW;
        }
        if (points <= MODERATE_LIMIT) {
            return DebtStatus.MODERATE;
        }
        return DebtStatus.HIGH;
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("5. TECHNICAL DEBT REPORT"));
        List<String> all = getFindings();
        if (all.isEmpty()) {
            sb.append("No technical debt findings.\n");
        }
        for (String finding : all) {
            sb.append(finding).append("\n");
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append("Debt points : ").append(getDebtPoints()).append("\n");
        sb.append("Status      : ").append(getStatus()).append("\n");
        return sb.toString();
    }
}
