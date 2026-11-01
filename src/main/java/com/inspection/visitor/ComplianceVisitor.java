package com.inspection.visitor;

import com.inspection.element.APIEndpointElement;
import com.inspection.element.ConfigurationFile;
import com.inspection.element.DatabaseElement;
import com.inspection.element.DependencyFile;
import com.inspection.element.DeploymentConfigElement;
import com.inspection.element.Directory;
import com.inspection.element.SourceCodeFile;
import com.inspection.element.TestFile;
import com.inspection.report.CheckResult;
import com.inspection.report.ReportFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks the project against simple compliance rules.
 * Every rule is a PASS (no violations) or a FAIL (one or more violations).
 *
 * TEST FILE NAME CONVENTION
 *   Every source file is treated as a production module.
 *   PaymentService.java must have a test file called PaymentServiceTest.java.
 */
public class ComplianceVisitor implements ProjectVisitor {

    public static final String RULE_CREDENTIALS = "No exposed credentials";
    public static final String RULE_VERSIONS = "Every dependency has a version";
    public static final String RULE_TESTS = "Production modules have tests";
    public static final String RULE_PROHIBITED = "No prohibited dependencies";
    public static final String RULE_DB_ENCRYPTION = "Databases use encrypted connections";
    public static final String RULE_HTTPS = "Production deployment uses HTTPS";
    public static final String RULE_DEBUG = "Debug mode is off in production";
    public static final String RULE_API_AUTH = "API endpoints require authentication";

    private static final String[] RULES = {
            RULE_CREDENTIALS, RULE_VERSIONS, RULE_TESTS, RULE_PROHIBITED,
            RULE_DB_ENCRYPTION, RULE_HTTPS, RULE_DEBUG, RULE_API_AUTH
    };

    /** One broken rule: which rule and what exactly is wrong. */
    private static class Violation {
        private final String rule;
        private final String message;

        Violation(String rule, String message) {
            this.rule = rule;
            this.message = message;
        }
    }

    private final List<Violation> violations = new ArrayList<>();
    private final List<String> sourceFileNames = new ArrayList<>();
    private final List<String> testFileNames = new ArrayList<>();

    /** PaymentService.java -> PaymentServiceTest.java */
    public static String expectedTestFileName(String sourceFileName) {
        String baseName = sourceFileName;
        if (baseName.endsWith(".java")) {
            baseName = baseName.substring(0, baseName.length() - ".java".length());
        }
        return baseName + "Test.java";
    }

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        sourceFileNames.add(sourceCodeFile.getFileName());
        if (sourceCodeFile.hasHardcodedCredentials()) {
            addViolation(RULE_CREDENTIALS, sourceCodeFile.getFileName() + ": hardcoded credentials");
        }
    }

    @Override
    public void visit(TestFile testFile) {
        testFileNames.add(testFile.getFileName());
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        if (configurationFile.hasExposedCredentials()) {
            addViolation(RULE_CREDENTIALS, configurationFile.getFileName() + ": exposed credentials");
        }
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        String version = dependencyFile.getVersion();
        if (version == null || version.trim().isEmpty()) {
            addViolation(RULE_VERSIONS, dependencyFile.getDependencyName() + ": no version specified");
        }
        if (dependencyFile.isProhibited()) {
            addViolation(RULE_PROHIBITED, dependencyFile.getDependencyName() + ": prohibited dependency");
        }
    }

    @Override
    public void visit(Directory directory) {
        // Nothing to check in the directory itself.
    }

    @Override
    public void visit(DatabaseElement databaseElement) {
        String name = databaseElement.getDatabaseName();
        if (databaseElement.hasExposedCredentials()) {
            addViolation(RULE_CREDENTIALS, name + ": exposed database credentials");
        }
        if (!databaseElement.isEncryptedConnection()) {
            addViolation(RULE_DB_ENCRYPTION, name + ": connection is not encrypted");
        }
    }

    @Override
    public void visit(APIEndpointElement apiEndpointElement) {
        if (!apiEndpointElement.isAuthenticationRequired()) {
            addViolation(RULE_API_AUTH, apiEndpointElement.getLabel() + ": no authentication");
        }
    }

    @Override
    public void visit(DeploymentConfigElement deploymentConfigElement) {
        if (!deploymentConfigElement.isProduction()) {
            return;
        }
        if (!deploymentConfigElement.isHttpsEnabled()) {
            addViolation(RULE_HTTPS, "production: HTTPS is disabled");
        }
        if (deploymentConfigElement.isDebugMode()) {
            addViolation(RULE_DEBUG, "production: debug mode is enabled");
        }
    }

    private void addViolation(String rule, String message) {
        violations.add(new Violation(rule, message));
    }

    /** Stored violations plus the "missing test" violations, which can only be found after the whole tree was visited. */
    private List<Violation> collectAllViolations() {
        List<Violation> all = new ArrayList<>(violations);
        for (String sourceName : sourceFileNames) {
            if (!testFileNames.contains(expectedTestFileName(sourceName))) {
                all.add(new Violation(RULE_TESTS,
                        sourceName + ": missing " + expectedTestFileName(sourceName)));
            }
        }
        return all;
    }

    public int getViolationCount() {
        return collectAllViolations().size();
    }

    public List<CheckResult> getResults() {
        List<Violation> all = collectAllViolations();
        List<CheckResult> results = new ArrayList<>();
        for (String rule : RULES) {
            int count = 0;
            StringBuilder details = new StringBuilder();
            for (Violation violation : all) {
                if (violation.rule.equals(rule)) {
                    if (count > 0) {
                        details.append("; ");
                    }
                    details.append(violation.message);
                    count++;
                }
            }
            if (count == 0) {
                results.add(new CheckResult(rule, true, "no violations"));
            } else {
                results.add(new CheckResult(rule, false, count + " violation(s): " + details));
            }
        }
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

    /** Compliance score 0-100: the share of rules that passed. */
    public int getComplianceScore() {
        return (int) Math.round(getPassedCount() * 100.0 / RULES.length);
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("4. COMPLIANCE REPORT"));
        for (CheckResult result : getResults()) {
            sb.append(result).append("\n");
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append("Rules passed     : ").append(getPassedCount()).append(" / ").append(RULES.length).append("\n");
        sb.append("Violation count  : ").append(getViolationCount()).append("\n");
        sb.append("Compliance score : ").append(getComplianceScore()).append(" / 100\n");
        return sb.toString();
    }
}
