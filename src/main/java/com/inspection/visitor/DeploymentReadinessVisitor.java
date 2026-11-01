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
import com.inspection.report.DeploymentDecision;
import com.inspection.report.ReportFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether the infrastructure is ready for production.
 *
 * SEVEN CHECKS
 *   1. A production deployment configuration exists          (blocking)
 *   2. HTTPS is enabled in production                        (blocking)
 *   3. Debug mode is disabled in production                  (blocking)
 *   4. All database connections are encrypted                (blocking)
 *   5. All databases have backups enabled                    (warning only)
 *   6. All API endpoints require authentication              (blocking)
 *   7. All API endpoints have rate limiting                  (warning only)
 *
 * Checks 2 and 3 fail when there is no production configuration, because nothing can be confirmed.
 * Checks 4 to 7 pass when the project has no databases / no API endpoints.
 *
 * SCORE    = passed checks * 100 / 7, rounded.
 * DECISION = READY   if all checks pass,
 *            BLOCKED if at least one blocking check fails,
 *            WARNING if only warning-only checks fail.
 */
public class DeploymentReadinessVisitor implements ProjectVisitor {

    private static final int CHECK_COUNT = 7;

    private int productionCount;
    private int productionWithoutHttps;
    private int productionWithDebug;
    private int databaseCount;
    private int databasesUnencrypted;
    private int databasesWithoutBackup;
    private int apiCount;
    private int apisWithoutAuth;
    private int apisWithoutRateLimit;

    @Override
    public void visit(SourceCodeFile sourceCodeFile) {
        // Not relevant for deployment readiness.
    }

    @Override
    public void visit(TestFile testFile) {
        // Not relevant for deployment readiness.
    }

    @Override
    public void visit(ConfigurationFile configurationFile) {
        // Not relevant for deployment readiness.
    }

    @Override
    public void visit(DependencyFile dependencyFile) {
        // Not relevant for deployment readiness.
    }

    @Override
    public void visit(Directory directory) {
        // Not relevant for deployment readiness.
    }

    @Override
    public void visit(DatabaseElement databaseElement) {
        databaseCount++;
        if (!databaseElement.isEncryptedConnection()) {
            databasesUnencrypted++;
        }
        if (!databaseElement.isBackupEnabled()) {
            databasesWithoutBackup++;
        }
    }

    @Override
    public void visit(APIEndpointElement apiEndpointElement) {
        apiCount++;
        if (!apiEndpointElement.isAuthenticationRequired()) {
            apisWithoutAuth++;
        }
        if (!apiEndpointElement.isRateLimitingEnabled()) {
            apisWithoutRateLimit++;
        }
    }

    @Override
    public void visit(DeploymentConfigElement deploymentConfigElement) {
        if (!deploymentConfigElement.isProduction()) {
            return;
        }
        productionCount++;
        if (!deploymentConfigElement.isHttpsEnabled()) {
            productionWithoutHttps++;
        }
        if (deploymentConfigElement.isDebugMode()) {
            productionWithDebug++;
        }
    }

    private boolean productionExists() {
        return productionCount > 0;
    }

    private boolean httpsOk() {
        return productionExists() && productionWithoutHttps == 0;
    }

    private boolean debugOk() {
        return productionExists() && productionWithDebug == 0;
    }

    private boolean databasesEncrypted() {
        return databasesUnencrypted == 0;
    }

    private boolean backupsOk() {
        return databasesWithoutBackup == 0;
    }

    private boolean authOk() {
        return apisWithoutAuth == 0;
    }

    private boolean rateLimitOk() {
        return apisWithoutRateLimit == 0;
    }

    public List<CheckResult> getResults() {
        List<CheckResult> results = new ArrayList<>();
        results.add(new CheckResult("Production deployment configuration exists", productionExists(),
                productionCount + " production configuration(s)"));
        results.add(new CheckResult("HTTPS enabled in production", httpsOk(),
                productionExists() ? productionWithoutHttps + " production config(s) without HTTPS"
                        : "no production configuration to check"));
        results.add(new CheckResult("Debug mode disabled in production", debugOk(),
                productionExists() ? productionWithDebug + " production config(s) with debug mode"
                        : "no production configuration to check"));
        results.add(new CheckResult("Database connections encrypted", databasesEncrypted(),
                databasesUnencrypted + " of " + databaseCount + " database(s) not encrypted"));
        results.add(new CheckResult("Database backups enabled", backupsOk(),
                databasesWithoutBackup + " of " + databaseCount + " database(s) without backup"));
        results.add(new CheckResult("API authentication required", authOk(),
                apisWithoutAuth + " of " + apiCount + " endpoint(s) without authentication"));
        results.add(new CheckResult("API rate limiting enabled", rateLimitOk(),
                apisWithoutRateLimit + " of " + apiCount + " endpoint(s) without rate limiting"));
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

    public int getReadinessScore() {
        return (int) Math.round(getPassedCount() * 100.0 / CHECK_COUNT);
    }

    public DeploymentDecision getDecision() {
        boolean blockingFailed = !productionExists() || !httpsOk() || !debugOk()
                || !databasesEncrypted() || !authOk();
        if (blockingFailed) {
            return DeploymentDecision.BLOCKED;
        }
        if (!backupsOk() || !rateLimitOk()) {
            return DeploymentDecision.WARNING;
        }
        return DeploymentDecision.READY;
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ReportFormat.header("6. DEPLOYMENT READINESS REPORT"));
        for (CheckResult result : getResults()) {
            sb.append(result).append("\n");
        }
        sb.append("-".repeat(66)).append("\n");
        sb.append("Checks passed   : ").append(getPassedCount()).append(" / ").append(CHECK_COUNT).append("\n");
        sb.append("Readiness score: ").append(getReadinessScore()).append(" / 100\n");
        sb.append("Decision       : ").append(getDecision()).append("\n");
        return sb.toString();
    }
}
